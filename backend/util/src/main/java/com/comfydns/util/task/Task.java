package com.comfydns.util.task;

public interface Task {
    public void run(TaskContext context) throws Exception;
    public TaskDefinition getDefinition();
}
