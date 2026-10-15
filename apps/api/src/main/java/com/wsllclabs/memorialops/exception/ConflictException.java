package com.wsllclabs.memorialops.exception;

public class ConflictException extends RuntimeException {

    private final long version;
    private final String etag;
    private final Object current;

    public ConflictException(String message, long version, Object current) {
        super(message);
        this.version = version;
        this.etag = "\"" + version + "\"";
        this.current = current;
    }

    public long getVersion() {
        return version;
    }

    public String getEtag() {
        return etag;
    }

    public Object getCurrent() {
        return current;
    }
}
