package dev.quasemago.maidbot.data.dto;

import java.sql.Date;
import java.util.Locale;

public record GuildServerDTO(Long guildId, Long logFlags, Long logChannelId, Locale locale, Date lastUpdated) {
}
