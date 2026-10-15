package com.wsllclabs.memorialops;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void moduleGraphIsValid() {
        ApplicationModules.of(MemorialOpsApplication.class).verify();
    }
}
