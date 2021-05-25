package com.comfydns.resolver.task;

import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskCreator;
import com.comfydns.util.task.TaskDefinition;

public class ResolverTaskCreator implements TaskCreator {
    @Override
    public Task create(TaskDefinition d) {
        switch(d.getAction()) {
            case "RELOAD_ZONES":
                return new ReloadZonesTask(d);
            case "RELOAD_ADBLOCK_CONFIG":
                return new ReloadAdblockingStateTask(d);
            default:
                throw new IllegalArgumentException("ResolverTaskCreator does not support task type " + d.getAction());
        }
    }
}