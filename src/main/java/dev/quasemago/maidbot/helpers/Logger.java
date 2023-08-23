package dev.quasemago.maidbot.helpers;

import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;

@Component
public class Logger {
    public static org.apache.logging.log4j.Logger log = LogManager.getLogger(Logger.class);
}