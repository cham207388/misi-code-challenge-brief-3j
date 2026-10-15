package com.wsllclabs.memorialops.mocks.common;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/__admin")
public class AdminController {

    public record ScenarioRequest(@NotBlank String mode) {}

    public record ScenarioResponse(String mode) {}

    public record CallsResponse(long count, List<CallLog.CallRecord> recent) {}

    private final ScenarioStore scenarioStore;
    private final CallLog callLog;

    public AdminController(ScenarioStore scenarioStore, CallLog callLog) {
        this.scenarioStore = scenarioStore;
        this.callLog = callLog;
    }

    @PostMapping("/scenario")
    public ScenarioResponse setScenario(@RequestBody ScenarioRequest scenarioRequest) {
        ScenarioMode mode = ScenarioMode.fromToken(scenarioRequest.mode());
        scenarioStore.set(mode);
        return new ScenarioResponse(mode.name());
    }

    @GetMapping("/scenario")
    public ScenarioResponse current() {
        return new ScenarioResponse(scenarioStore.current().name());
    }

    @GetMapping("/calls")
    public CallsResponse calls() {
        return new CallsResponse(callLog.count(), callLog.recent(50));
    }

    @PostMapping("/calls/reset")
    public CallsResponse reset() {
        callLog.reset();
        return new CallsResponse(0, List.of());
    }
}
