package dev.quasemago.maidbot.data.repositories;

import dev.quasemago.maidbot.data.models.GuildServer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuildServerRepository extends CrudRepository<GuildServer, Long> {
    Optional<GuildServer> findByGuildId(Long guildId);
}
