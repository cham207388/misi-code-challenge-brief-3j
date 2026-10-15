package com.wsllclabs.memorialops.application;

import com.wsllclabs.memorialops.exception.RequestValidationException;

public final class VersionHeaders {

    private VersionHeaders() {}

    public static String etag(long version) {
        return "\"" + version + "\"";
    }

    public static long requireVersion(String ifMatch, Long bodyVersion) {
        if (ifMatch != null && !ifMatch.isBlank()) {
            try {
                return Long.parseLong(
                        ifMatch.replace("W/", "").replace("\"", "").trim());
            } catch (NumberFormatException exception) {
                throw new RequestValidationException("If-Match must be the current row version");
            }
        }
        if (bodyVersion != null) {
            return bodyVersion;
        }
        throw new RequestValidationException("If-Match header or version is required");
    }
}
