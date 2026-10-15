package com.wsllclabs.memorialops.shared.reference;

import java.time.Clock;
import java.time.Year;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceNumberService {

    public static final String APPLICATION = "APP";
    public static final String PERMIT = "PER";

    private final ReferenceCounterRepository referenceCounterRepository;
    private final Clock clock;

    public ReferenceNumberService(ReferenceCounterRepository referenceCounterRepository, Clock clock) {
        this.referenceCounterRepository = referenceCounterRepository;
        this.clock = clock;
    }

    @Transactional
    public String nextApplicationReference() {
        return next(APPLICATION);
    }

    @Transactional
    public String nextPermitReference() {
        return next(PERMIT);
    }

    private String next(String kind) {
        int year = Year.now(clock).getValue();
        ReferenceCounter counter =
                referenceCounterRepository.lockByKindAndYear(kind, year).orElseGet(() -> create(kind, year));
        return "%s-%d-%05d".formatted(kind, year, counter.nextValue());
    }

    private ReferenceCounter create(String kind, int year) {
        try {
            return referenceCounterRepository.saveAndFlush(new ReferenceCounter(kind, year));
        } catch (DataIntegrityViolationException ignored) {
            return referenceCounterRepository.lockByKindAndYear(kind, year).orElseThrow();
        }
    }
}
