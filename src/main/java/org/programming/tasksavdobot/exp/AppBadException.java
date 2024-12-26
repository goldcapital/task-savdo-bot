package org.programming.tasksavdobot.exp;

public class AppBadException extends RuntimeException {
    public AppBadException(String fileNotFound) {
        super(fileNotFound);
    }
}
