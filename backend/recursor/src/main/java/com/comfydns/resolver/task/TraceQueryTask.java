package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.request.InternalRequest;
import com.comfydns.resolver.resolver.rfc1035.service.request.TracingInternalRequest;
import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskContext;
import com.comfydns.util.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        m.getQuestions().add(new Question(qname, QType.match(qtype), KnownRRClass.IN));
        m.getHeader().setQDCount(1);
        m.getHeader().setRD(true);
        m.getHeader().setIdRandomly();
        m.validateHeader();

        TracingInternalRequest req = new TracingInternalRequest(m, this::onAnswer);

        if(!(context instanceof ResolverTaskContext)) {
            throw new IllegalStateException("TraceQueryTask requires a ResolverTaskContext");
        }

        ResolverTaskContext ctx = (ResolverTaskContext) context;
        ctx.getResolver().resolve(req);
        log.info("Submitted trace query.");
    }

    private void onAnswer(Message m) {
        log.info("Trace query returned.");
    }

    @Override
    public TaskDefinition getDefinition() {
        return def;
    }
}
