package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.trace.*;
import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskContext;
import com.comfydns.util.task.TaskDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class TraceQueryTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(TraceQueryTask.class);
    private final TaskDefinition def;

    public TraceQueryTask(TaskDefinition def) {
        this.def = def;
    }

    @Override
    public void run(TaskContext context) throws Exception {
        String qname = def.getArgs().get("qname").getAsString();
        int qtype = def.getArgs().get("qtype").getAsInt();

        Message m = new Message();
        m.setHeader(new Header());
        m.getQuestions().add(new Question(qname, QType.match(qtype), KnownRRClass.IN));
        m.getHeader().setQDCount(1);
        m.getHeader().setRD(true);
        m.getHeader().setIdRandomly();
        TracingInternalRequest req = new TracingInternalRequest(m);
        req.getMessage().validateHeader();

        if(!(context instanceof ResolverTaskContext)) {
            throw new IllegalStateException("TraceQueryTask requires a ResolverTaskContext");
        }

        ResolverTaskContext ctx = (ResolverTaskContext) context;
        req.setOnAnswer(resp -> this.onAnswer(resp, req, ctx));

        ctx.getResolver().resolve(req);
        log.info("Submitted trace query.");
    }

    private void onAnswer(Message m, TracingInternalRequest req, ResolverTaskContext ctx) {
        log.info("Trace query returned.");

        Tracer tracer = req.getTracer();

        try(Connection c = ctx.getDbPool().getConnection().get()) {
            c.setAutoCommit(false);
            UUID traceId;
            try(PreparedStatement ps = c.prepareStatement(
                    "insert into trace " +
                            "(id, task_id, qname, qtype, qclass, created_at, updated_at) " +
                            "values (DEFAULT, ?, ?, ?, ?, now(), now()) returning id",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, def.getId());
                ps.setString(2, def.getArgs().get("qname").getAsString());
                ps.setInt(3, def.getArgs().get("qtype").getAsInt());
                ps.setInt(4, KnownRRClass.IN.getIntValue());
                int rows = ps.executeUpdate();
                try(ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    traceId = rs.getObject("id", UUID.class);
                }
                log.debug("Inserted {} rows into trace", rows);
            }

            Gson gson = (new GsonBuilder())
                    .setPrettyPrinting()
                    .registerTypeAdapter(Throwable.class, new ThrowableSerializer())
                    .registerTypeAdapter(Message.class, new MessageCodec())
                    .registerTypeAdapter(RR.class, new RRCodec())
                    .registerTypeAdapter(Header.class, new HeaderCodec())
                    .create();

            try(PreparedStatement ps = c.prepareStatement(
                    "insert into trace_event" +
                            "(id, event_index, trace_id, event_type, event, created_at, updated_at) " +
                            "values (DEFAULT, ?, ?, ?, ?::jsonb, now(), now())")) {
                for (TraceEntry e : tracer.getEntries()) {
                    ps.setInt(1, e.getIndex());
                    ps.setObject(2, traceId);
                    ps.setString(3, e.getType().name());
                    ps.setString(4, gson.toJson(e));
                    ps.addBatch();
                }

                int[] updates = ps.executeBatch();
                log.debug("Inserted {} rows into trace_event", IntStream.of(updates).sum());
            }

            c.commit();
            log.info("COMMIT trace {}", traceId);
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            log.error("Error persisting trace", throwables);
        }
    }

    @Override
    public TaskDefinition getDefinition() {
        return def;
    }
}
