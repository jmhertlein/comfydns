package com.comfydns.resolver.task;

public interface Task {
    public void run(TaskContext context) throws Exception;
    public TaskDefinition getDefinition();
}
