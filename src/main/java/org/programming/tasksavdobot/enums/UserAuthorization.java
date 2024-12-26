package org.programming.tasksavdobot.enums;

import lombok.Getter;

@Getter
public enum UserAuthorization {

    START("START"),

    WAITING_FOR_LANGUAGE("WAITING_FOR_LANGUAGE"),

    WAITING_FOR_NAME("WAITING_FOR_NAME"),

    WAITING_FOR_PHONE("WAITING_FOR_PHONE");

    private final String name;

    UserAuthorization(String name) {
        this.name = name;
    }
}
