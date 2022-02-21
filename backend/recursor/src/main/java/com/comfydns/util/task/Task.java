package com.comfydns.util.task;

import com.comfydns.resolver.task.ResolverTaskContext;

public interface Task {
    public void run(ResolverTaskContext context) throws Exception;
    public TaskDefinition getDefinition();
}
