package com.example.EpidemicSimulator.exception;

public class ExceptionResponse {

    private String errorCode;
    private String errorMessage;

    public ExceptionResponse() {
    }


    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }



    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}