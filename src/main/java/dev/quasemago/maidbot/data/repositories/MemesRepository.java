package dev.quasemago.maidbot.data.repositories;

import dev.quasemago.maidbot.data.models.Memes;
import dev.quasemago.maidbot.data.models.GuildServer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemesRepository extends CrudRepository<Memes, Long> {
    Optional<Memes> findByIdAndGuild(Long id, GuildServer guild);
    Optional<Memes> findByNameContainingIgnoreCaseAndGuild(String name, GuildServer guild);
    Iterable<Memes> findAllByGuild(GuildServer guild);
}
