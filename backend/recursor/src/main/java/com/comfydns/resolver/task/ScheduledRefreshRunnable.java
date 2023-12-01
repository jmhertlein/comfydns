package com.comfydns.resolver.task;

import com.comfydns.resolver.resolve.block.BlockList;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

import static com.comfydns.resolver.task.RefreshBlockListsTask.updateBlockList;

public class ScheduledRefreshRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ScheduledRefreshRunnable.class);
    private final HikariDataSource dbPool;

    public ScheduledRefreshRunnable(HikariDataSource dbPool) {
        this.dbPool = dbPool;
    }

    @Override
    public void run() {
        OffsetDateTime now = OffsetDateTime.now();
        log.debug("Starting block list refresh check.");

        try (Connection c = dbPool.getConnection()) {
            List<BlockList> lists = new ArrayList<>();
            Map<UUID, OffsetDateTime> lastUpdate = new HashMap<>();
            try(PreparedStatement ps = c.prepareStatement(
                    "select bl.id, bl.name, bl.url, bl.list_type, bl.auto_update, bl.update_frequency, bls.updated_at " +
                            "from block_list bl left outer join block_list_snapshot bls on (bl.id=bls.block_list_id)" +
                            "where auto_update");
                ResultSet rs = ps.executeQuery()
            ) {
                while(rs.next()) {
                    BlockList l = new BlockList(rs);
                    lists.add(l);
                    lastUpdate.put(l.getId(), rs.getObject("updated_at", OffsetDateTime.class));
                }
            }

            if(lists.isEmpty()) {
                log.debug("No block lists need refresh.");
                return;
            }


            c.setAutoCommit(false);
            Savepoint sp = c.setSavepoint();
            for (BlockList list : lists) {
                if(!needsUpdate(now, list, lastUpdate.get(list.getId()))) {
                    continue;
                }

                try {
                    updateBlockList(now, c, list);
                } catch (IOException | SQLException e) {
                    c.rollback(sp);
                    log.error(String.format("Error updating blocklist %s:%s", list.getId(), list.getName()), e);
                    continue;
                }

                c.releaseSavepoint(sp);
                sp = c.setSavepoint();
            }

            c.commit();
            log.debug("Finished block list refresh check.");
        } catch (SQLException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static boolean needsUpdate(OffsetDateTime now, BlockList list, OffsetDateTime lastUpdate) {
        if(lastUpdate == null) {
            return true;
        }

        OffsetDateTime updateBy = lastUpdate.plus(list.getUpdateFrequency());
        return updateBy.isBefore(now);
    }
}
