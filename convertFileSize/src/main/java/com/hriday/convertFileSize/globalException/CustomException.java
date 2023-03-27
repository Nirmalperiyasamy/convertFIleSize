package com.hriday.convertFileSize.globalException;

public class CustomException extends RuntimeException{
    public CustomException(String user) {
        super(user);
    }

}
