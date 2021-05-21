package com.springernature.sndeals.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class InvalidEmailDomainException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public InvalidEmailDomainException() {
        super(ErrorConstants.INVALID_EMAIL_DOMAIN_TYPE, "Invalid email domain", Status.BAD_REQUEST);
    }
}
