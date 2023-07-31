package dev.quasemago.maidbot.data.repository;

import dev.quasemago.maidbot.models.Memes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemesRepository extends CrudRepository<Memes, Long> {
    Memes findByIdAndGuildId(Long id, Long guildId);
    Memes findByNameAndGuildId(String name, Long guildId);
    Iterable<Memes> findAllByIdAndGuildId(Long id, Long guildId);
    Iterable<Memes> findAllByNameContainingAndGuildId(String name, Long guildId);
    Iterable<Memes> findAllByGuildId(Long guildId);
}
