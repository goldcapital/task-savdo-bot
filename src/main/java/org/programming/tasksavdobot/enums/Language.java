package org.programming.tasksavdobot.enums;

public enum Language {
    UZ,EN,RU;



    public static Language fromInt(int i) {
        return switch (i) {
            case 2 -> RU;
            case 1 -> EN;
            case 0 -> UZ;
            default -> throw new IllegalArgumentException("Invalid language selection: " + i);
        };
    }
}
