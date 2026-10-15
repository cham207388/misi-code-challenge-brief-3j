package com.wsllclabs.memorialops.shared.reference;

import static org.assertj.core.api.Assertions.assertThat;

import com.wsllclabs.memorialops.support.AbstractIntegrationTest;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReferenceNumberIT extends AbstractIntegrationTest {

    @Autowired
    private ReferenceNumberService referenceNumberService;

    @Test
    void parallelApplicationReferencesAreUnique() throws Exception {
        int count = 20;
        Set<String> values = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(count);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executor.submit(() -> {
                    try {
                        start.await();
                        values.add(referenceNumberService.nextApplicationReference());
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(values).hasSize(count);
        assertThat(values).allMatch(value -> value.matches("APP-\\d{4}-\\d{5}"));
    }
}
