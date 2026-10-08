package com.panacea.backend.Exceptions;

public class UnAuthorisedException extends RuntimeException {
    public UnAuthorisedException(String s) {
        super(s);
    }
}
