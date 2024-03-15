package dev.svero.playground.helloworld.exceptions;

public class SSLUtilsException extends RuntimeException {
    public SSLUtilsException() {
    }

    public SSLUtilsException(String message) {
        super(message);
    }

    public SSLUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSLUtilsException(Throwable cause) {
        super(cause);
    }

    public SSLUtilsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
