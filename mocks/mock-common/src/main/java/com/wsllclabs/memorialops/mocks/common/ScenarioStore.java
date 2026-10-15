package com.wsllclabs.memorialops.mocks.common;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class ScenarioStore {

    private final AtomicReference<ScenarioMode> mode = new AtomicReference<>(ScenarioMode.SUCCESS);

    public ScenarioMode current() {
        return mode.get();
    }

    public void set(ScenarioMode scenarioMode) {
        mode.set(scenarioMode);
    }
}
