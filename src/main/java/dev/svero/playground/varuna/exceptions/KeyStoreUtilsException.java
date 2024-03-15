package dev.svero.playground.helloworld.exceptions;

public class KeyStoreUtilsException extends RuntimeException{
    public KeyStoreUtilsException() {
    }

    public KeyStoreUtilsException(String message) {
        super(message);
    }

    public KeyStoreUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyStoreUtilsException(Throwable cause) {
        super(cause);
    }

    public KeyStoreUtilsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
