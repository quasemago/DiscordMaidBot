package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.domains.models.GuildServer;
import dev.quasemago.maidbot.dto.GuildServerDTO;
import dev.quasemago.maidbot.repositories.GuildServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuildServerService {
    @Autowired
    private GuildServerRepository repository;

    public GuildServer getGuildServerByGuildId(Long id) {
        return this.repository.findByGuildId(id).orElse(null);
    }

    public GuildServer createGuildServer(GuildServerDTO data) {
        GuildServer server = new GuildServer(data);
        this.repository.save(server);
        return server;
    }

    public void save(GuildServer server) {
        this.repository.save(server);
    }
}
