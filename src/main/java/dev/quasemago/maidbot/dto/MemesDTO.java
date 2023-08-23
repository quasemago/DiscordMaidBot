package dev.quasemago.maidbot.dto;

import dev.quasemago.maidbot.domains.models.GuildServer;

import java.sql.Date;

public record MemesDTO(String name, String url, Date dateCreated, Date dateUpdated, GuildServer guildServer) {
}
