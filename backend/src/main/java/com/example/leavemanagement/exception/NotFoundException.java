package com.example.leavemanagement.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends CustomException {

    public NotFoundException() {
        super("Leave request was not found");
        status = HttpStatus.NOT_FOUND;
    }
}
