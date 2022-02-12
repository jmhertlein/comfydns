package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.block.BlockList;
import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskContext;
import com.comfydns.util.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.IntStream;

public class RefreshBlockListsTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(RefreshBlockListsTask.class);
    private final TaskDefinition def;

    public RefreshBlockListsTask(TaskDefinition d) {
        def = d;
    }

    public static void updateBlockList(OffsetDateTime now, Connection c, BlockList l) throws IOException, SQLException {
        try (PreparedStatement ps = c.prepareStatement("delete from block_list_snapshot where block_list_id=?")) {
            ps.setObject(1, l.getId());
            ps.executeUpdate();
        }

        UUID snapshotId;
        try (PreparedStatement ps = c.prepareStatement("insert into block_list_snapshot (id, block_list_id, created_at, updated_at) values (DEFAULT, ?, ?, ?) returning id",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, l.getId());
            ps.setObject(2, now);
            ps.setObject(3, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                snapshotId = rs.getObject("id", UUID.class);
            }
        }

        int batchCount = 0;
        int rowsAffected = 0;
        try (InputStream in = l.getUrl().openStream();
             PreparedStatement ps = c.prepareStatement("insert into blocked_name (id, name, block_list_snapshot_id, created_at, updated_at) values (DEFAULT, ?, ?, ?, ?)")) {
            Scanner scan = new Scanner(in);
            while (scan.hasNextLine()) {
                Optional<String> name = l.getListType().extractNameFromLine(scan.nextLine().strip());
                if(name.isEmpty()) {
                    continue;
                }
                ps.setString(1, name.get());
                ps.setObject(2, snapshotId);
                ps.setObject(3, now);
                ps.setObject(4, now);
                ps.addBatch();
                batchCount++;

                if(batchCount > 500) {
                    batchCount = 0;
                    rowsAffected += IntStream.of(ps.executeBatch()).sum();
                }
            }
            if(batchCount > 0) {
                batchCount = 0;
                rowsAffected += IntStream.of(ps.executeBatch()).sum();
            }
            log.info("Update for block list {}: {} names", l.getName(), rowsAffected);
        }
    }

    @Override
    public void run(TaskContext ctx) throws SQLException, MalformedURLException {
        OffsetDateTime now = OffsetDateTime.now();
        Connection c = ctx.getConnection();

        UUID listId = UUID.fromString(def.getArgs().get("block_list_id").getAsString());

        log.debug("Starting manual block list refresh.");

        BlockList list;
        try (PreparedStatement ps = c.prepareStatement(
                "select bl.id, bl.name, bl.url, bl.list_type, bl.auto_update, bl.update_frequency " +
                        "from block_list bl " +
                        "where id=?")
        ) {
            ps.setObject(1, listId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    log.error("No such block list with id={}", listId);
                    throw new RuntimeException("No such block list id: " + listId);
                }
                list = new BlockList(rs);
            }
        }

        c.setAutoCommit(false);
        try {
            updateBlockList(now, c, list);
        } catch (IOException | SQLException e) {
            log.error(String.format("Error updating blocklist %s:%s", list.getId(), list.getName()), e);
            throw new RuntimeException(String.format("Error updating blocklist %s:%s", list.getId(), list.getName()));
        }


        c.commit();
        log.info("Finished manual block list refresh.");
    }

    @Override
    public TaskDefinition getDefinition() {
        return def;
    }
}
