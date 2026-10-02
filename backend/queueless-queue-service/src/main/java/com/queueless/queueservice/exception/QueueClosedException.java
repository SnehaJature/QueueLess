package com.queueless.queueservice.exception;

public class QueueClosedException extends RuntimeException {
    public QueueClosedException(String message) { super(message); }
}
