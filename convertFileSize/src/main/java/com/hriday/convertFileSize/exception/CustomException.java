package com.hriday.convertFileSize.exception;

import com.hriday.convertFileSize.utils.ErrorMessage;

public class CustomException extends RuntimeException {


    public CustomException(ErrorMessage user) {
        super(String.valueOf(user));
    }

    public CustomException(String message) {
        super(message);
    }
}
