package com.paymentplatform.embeddedpayments.shared.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final List<String> details;

    public DomainException(String message) {
        this(HttpStatus.BAD_REQUEST, "DOMAIN_ERROR", message, List.of());
    }

    public DomainException(HttpStatus status, String errorCode, String message, List<String> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<String> getDetails() {
        return details;
    }
}

