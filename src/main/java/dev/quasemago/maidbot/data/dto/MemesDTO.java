package dev.quasemago.maidbot.data.dto;

import dev.quasemago.maidbot.data.models.GuildServer;

import java.sql.Date;

public record MemesDTO(String name, String url, Date dateCreated, Date dateUpdated, GuildServer guildServer) {
}
