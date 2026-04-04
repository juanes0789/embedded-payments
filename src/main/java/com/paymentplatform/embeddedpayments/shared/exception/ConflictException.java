package com.paymentplatform.embeddedpayments.shared.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ConflictException extends DomainException {

    public ConflictException(String errorCode, String message, List<String> details) {
        super(HttpStatus.CONFLICT, errorCode, message, details);
    }
}

