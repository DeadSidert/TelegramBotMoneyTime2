package com.nikita.botter.util;

public final class TelegramUtil {

    private TelegramUtil() {}

    public static String extractCommand(String text) {
        return text.split(" ")[0];
    }

    public static String extractArguments(String text) {
        return text.substring(text.indexOf(" ") + 1);
    }
}