package ru.practicum.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleDateTimeFormatter {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final LocalDateTime CURRENT_TIME = LocalDateTime.now();

    public static String toString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(PATTERN));
    }

    public static LocalDateTime parse(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(PATTERN));
    }
}