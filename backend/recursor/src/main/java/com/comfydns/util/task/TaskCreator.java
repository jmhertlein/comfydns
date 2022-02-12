package com.comfydns.util.task;

@FunctionalInterface
public interface TaskCreator {
    public Task create(TaskDefinition d);
}
