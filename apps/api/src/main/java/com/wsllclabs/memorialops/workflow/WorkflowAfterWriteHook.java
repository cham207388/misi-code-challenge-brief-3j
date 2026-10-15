package com.wsllclabs.memorialops.workflow;

import org.springframework.stereotype.Component;

@Component
public class WorkflowAfterWriteHook {

    public void afterPersist() {
        // Overridden in tests to prove status history and audit roll back together.
    }
}
