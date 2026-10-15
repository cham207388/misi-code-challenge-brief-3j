package com.wsllclabs.memorialops.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wsllclabs.memorialops.exception.RequestValidationException;
import org.junit.jupiter.api.Test;

class VersionHeadersTest {

    @Test
    void parsesIfMatchAndBodyFallback() {
        assertThat(VersionHeaders.requireVersion("W/\"7\"", null)).isEqualTo(7);
        assertThat(VersionHeaders.requireVersion(null, 4L)).isEqualTo(4);
        assertThat(VersionHeaders.etag(3)).isEqualTo("\"3\"");
        assertThatThrownBy(() -> VersionHeaders.requireVersion("nope", null))
                .isInstanceOf(RequestValidationException.class);
        assertThatThrownBy(() -> VersionHeaders.requireVersion(null, null))
                .isInstanceOf(RequestValidationException.class);
    }
}
