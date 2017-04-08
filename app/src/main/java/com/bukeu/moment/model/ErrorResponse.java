package com.bukeu.moment.model;

/**
 * Created by Max on 2015/4/3.
 */
public class ErrorResponse extends BaseModel {
    private String error;

    public ErrorResponse() {

    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
