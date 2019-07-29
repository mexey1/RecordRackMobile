package com.geckosolutions.recordrack.exceptions;

/**
 * Created by anthony1 on 9/16/17.
 */

public class ThreadException extends Exception
{
    private String errorMessage;

    public void setErrorMessage(String message)
    {
        errorMessage = message;
    }

    @Override
    public String getMessage()
    {
        return errorMessage;
    }
}
