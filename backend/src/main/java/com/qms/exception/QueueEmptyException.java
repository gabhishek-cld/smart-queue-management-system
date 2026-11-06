package com.qms.exception;

public class QueueEmptyException extends RuntimeException {
    public QueueEmptyException() {
        super("No tickets are currently waiting in the queue.");
    }
}
