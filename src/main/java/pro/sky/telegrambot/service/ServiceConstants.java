package pro.sky.telegrambot.service;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ServiceConstants {

    public final static Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\P{M}\\p{M}*+]+)");
    public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

}
