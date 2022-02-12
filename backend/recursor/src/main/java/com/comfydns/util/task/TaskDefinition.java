package com.comfydns.util.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class TaskDefinition {
    private final UUID id;
    private final String action;
    private final UUID targetServerId;
    private final boolean started, done, failed;
    private final JsonObject args;

    public TaskDefinition(ResultSet rs) throws SQLException {
        Gson gson = new Gson();
        this.id = rs.getObject("id", UUID.class);
        this.action = rs.getString("action");
        this.targetServerId = rs.getObject("server_id", UUID.class);
        this.started = rs.getBoolean("started");
        this.done = rs.getBoolean("done");
        this.failed = rs.getBoolean("failed");
        String argsStr = rs.getString("args");
        this.args = argsStr == null ? null : gson.fromJson(argsStr, JsonObject.class);
    }

    public TaskDefinition(UUID id, String action, UUID targetServerId, boolean started, boolean done, boolean failed, JsonObject args) {
        this.id = id;
        this.action = action;
        this.targetServerId = targetServerId;
        this.started = started;
        this.done = done;
        this.failed = failed;
        this.args = args;
    }

    public UUID getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public Optional<UUID> getTargetServerId() {
        return Optional.ofNullable(targetServerId);
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isFailed() {
        return failed;
    }

    public JsonObject getArgs() {
        return args;
    }

}
