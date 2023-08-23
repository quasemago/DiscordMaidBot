package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.domains.models.GuildServer;
import dev.quasemago.maidbot.domains.models.Memes;
import dev.quasemago.maidbot.dto.MemesDTO;
import dev.quasemago.maidbot.repositories.MemesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemesService {
    @Autowired
    private MemesRepository repository;

    public Memes getMemeByIdAndGuild(Long id, GuildServer guild) {
        return this.repository.findByIdAndGuild(id, guild).orElse(null);
    }

    public Memes getMemeByNameAndGuild(String name, GuildServer guild) {
        return this.repository.findByNameContainingIgnoreCaseAndGuild(name, guild).orElse(null);
    }

    public Iterable<Memes> getAllMemesByGuild(GuildServer guild) {
        return this.repository.findAllByGuild(guild);
    }

    public Memes createMeme(MemesDTO data) {
        final Memes newMeme = new Memes(data);
        this.repository.save(newMeme);
        return newMeme;
    }

    public void save(Memes meme) {
        this.repository.save(meme);
    }
}
