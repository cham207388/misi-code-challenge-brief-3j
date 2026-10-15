package com.wsllclabs.memorialops.mocks.common;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class StatusCaptureResponse extends HttpServletResponseWrapper {

    private int status = HttpServletResponse.SC_OK;

    public StatusCaptureResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
        super.setStatus(sc);
    }

    public int status() {
        return status;
    }
}
