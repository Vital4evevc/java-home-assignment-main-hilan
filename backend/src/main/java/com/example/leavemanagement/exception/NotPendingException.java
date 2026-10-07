package com.example.leavemanagement.exception;

import org.springframework.http.HttpStatus;

public class NotPendingException extends CustomException {

    public NotPendingException() {
        super("Leave request is not pending");
        status = HttpStatus.BAD_REQUEST;
    }
}
