package dev.quasemago.maidbot.repository;

import dev.quasemago.maidbot.models.MemesModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemesRepository extends CrudRepository<MemesModel, Long> {
    MemesModel findMemeById(Long id);
    MemesModel findMemeByName(String name);
    MemesModel findByIdAndGuildId(Long id, Long guildId);
    Iterable<MemesModel> findAllByNameContaining(String name);
    Iterable<MemesModel> findAllByGuildId(Long guildId);
}
