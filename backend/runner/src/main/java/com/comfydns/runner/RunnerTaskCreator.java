package com.comfydns.runner;

import com.comfydns.resolver.task.ReloadZonesTask;
import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskCreator;
import com.comfydns.util.task.TaskDefinition;

public class RunnerTaskCreator implements TaskCreator {
    @Override
    public Task create(TaskDefinition d) {
        switch(d.getAction()) {
            case "REFRESH_BLOCK_LIST":
                return new RefreshBlockListsTask(d);
            default:
                throw new IllegalArgumentException("RunnerTaskCreator does not support task type " + d.getAction());
        }
    }
}
