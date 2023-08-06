package dev.quasemago.maidbot.data.repository;

import dev.quasemago.maidbot.models.Servers;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServersRepository extends CrudRepository<Servers, Long> {
    Servers findByGuildId(Long guildId);
}
