package com.wsllclabs.memorialops.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void clear() {
        MDC.clear();
    }

    @Test
    void copiesMdcOntoTheDecoratedThread() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "async-1");
        AtomicReference<String> seen = new AtomicReference<>();
        decorator.decorate(() -> seen.set(MDC.get(CorrelationIdFilter.MDC_KEY))).run();
        assertThat(seen.get()).isEqualTo("async-1");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
