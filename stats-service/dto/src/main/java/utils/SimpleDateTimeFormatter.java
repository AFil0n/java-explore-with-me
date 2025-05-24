package utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class SimpleDateTimeFormatter {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String toString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(PATTERN));
    }
}