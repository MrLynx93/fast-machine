package com.agh.fastmachine.server.api.exception;

public class ObjectDeletedException extends UnsupportedOperationException {

    public ObjectDeletedException() {
        super("Object is deleted. Let it be garbage collected.");
    }
}
