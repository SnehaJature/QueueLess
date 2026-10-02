package com.queueless.queueservice.exception;

public class AlreadyJoinedQueueException extends RuntimeException {
    public AlreadyJoinedQueueException(String message) { super(message); }
}
