package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
