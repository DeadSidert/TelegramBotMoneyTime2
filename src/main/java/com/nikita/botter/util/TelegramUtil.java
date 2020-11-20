package com.nikita.botter.util;

public final class TelegramUtil {

    private TelegramUtil() {}

    /**
     * метод возвращает команду пользователя
     *
     * @param text - текст
     * @return command - все до разделения
     */
    public static String extractCommand(String text) {
        return text.split("_")[0];
    }

    /**
     * метод возвращает аргументы пользователя
     *
     * @param text - текст
     * @return command - все после разделения
     */
    public static String extractArguments(String text) {
        return text.substring(text.indexOf("_") + 1);
    }
}