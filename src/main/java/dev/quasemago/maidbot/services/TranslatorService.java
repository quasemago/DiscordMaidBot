package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.data.BotConfiguration;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class TranslatorService {
    private final ResourceBundleMessageSource source;

    public TranslatorService() {
        final var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("lang/messages");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultEncoding(StandardCharsets.ISO_8859_1.name());
        source = messageSource;
    }

    public String translate(Locale locale, String key, Object... args) {
        try {
            return source.getMessage(key, args, locale);
        } catch (Exception e) {
            Logger.log.error("Failed to translate message: " + key + " | locale: " + locale);
            return "Failed to translate message: " + key;
        }
    }

    public String translate(GuildServer guildServer, String key, Object... args) {
        if (guildServer == null || guildServer.getLocale() == null) {
            return translate(BotConfiguration.getBotDefaultLanguage(), key, args);
        }
        return translate(guildServer.getLocale(), key, args);
    }
}
