package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.data.dto.GuildServerDTO;
import dev.quasemago.maidbot.data.repositories.GuildServerRepository;
import discord4j.core.object.entity.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuildServerService {
    @Autowired
    private GuildServerRepository repository;

    public GuildServer getGuildServerByGuild(Guild guild) {
        if (guild == null) {
            return null;
        }
        return this.getGuildServerByGuildId(guild.getId().asLong());
    }

    public GuildServer getGuildServerByGuildId(Long id) {
        return this.repository.findByGuildId(id).orElse(null);
    }

    public GuildServer createGuildServer(GuildServerDTO data) {
        GuildServer server = new GuildServer(data);
        this.saveGuildServer(server);
        return server;
    }

    public void deleteGuildServerById(Long id) {
        this.repository.deleteById(id);
    }

    public void saveGuildServer(GuildServer server) {
        this.repository.save(server);
    }

    public void deleteGuildServer(GuildServer guildServer) {
        this.repository.delete(guildServer);
    }
}
