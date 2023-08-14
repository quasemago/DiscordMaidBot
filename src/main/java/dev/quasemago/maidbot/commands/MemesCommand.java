package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.models.SlashCommand;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.models.Memes;
import dev.quasemago.maidbot.data.repository.MemesRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Component
public class MemesCommand extends SlashCommand<ChatInputInteractionEvent> {
    @Autowired
    private MemesRepository memesRepository;
    private final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final MessageChannel channel = event.getInteraction().
                getChannel().
                block();

        if (!(channel instanceof PrivateChannel)) {
            final Snowflake guild = event.getInteraction()
                    .getGuildId()
                    .orElse(null);

            if (guild == null) {
                Logger.log.error("Failed to get guild id from event: {}", event);
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Failed to get memes list.");
            }

            final var options = event.getOptions().get(0);
            final String optionName = options.getName();
            switch (optionName) {
                case "add" -> {
                    return addMeme(event, options, guild);
                }
                case "list" -> {
                    return listMemes(event, guild);
                }
                case "get" -> {
                    return getMeme(event, options, guild);
                }
                case "random" -> {
                    return randomMeme(event, guild);
                }
            }

            Logger.log.error("Failed to get command options: {}", event);
            return event.reply("Failed get command options.")
                    .withEphemeral(true);
        } else {
            return event.reply("This command can only be used in a server channel.")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> addMeme(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option, Snowflake guildId) {
        final String url = option.getOption("url")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asString();

        final String name = option.getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asString();

        final Date date = new Date(System.currentTimeMillis());
        final Memes meme = new Memes(date, date, guildId.asLong(),name, url);

        // Save to database.
        // TODO: Improve this!
        memesRepository.save(meme);

        // Send embed to channel.
        event.getInteraction()
                .getChannel()
                .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                .title("["+ meme.getId() +"] " + meme.getName())
                                .image(meme.getUrl())
                        .build()))
                .block();

        // Reply to user.
        return event.reply()
                .withEphemeral(true)
                .withContent("Meme added!");
    }

    private Mono<Void> listMemes(ChatInputInteractionEvent event, Snowflake guildId) {
        // Try get bot user.
        final User bot = event.getInteraction()
                .getClient()
                .getSelf()
                .block();

        if (bot == null) {
            Logger.log.error("Failed to get bot user from event: {}", event);
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Failed to get memes list.");
        }

        // Get memes list from database.
        final Iterable<Memes> memesList = memesRepository.findAllByGuildId(guildId.asLong());

        // Create embed to reply.
        final var spec = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title(bot.getUsername() + " Memes")
                .timestamp(Instant.now())
                .footer(bot.getUsername(), null);

        final StringBuilder description = new StringBuilder();
        memesList.forEach(meme -> description.append("**["+meme.getId()+"]** " + meme.getName() + "\n"));

        final var embed = spec.description(description.toString()).build();
        return event.reply()
                .withEphemeral(true)
                .withEmbeds(embed);
    }

    private Mono<Void> getMeme(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option, Snowflake guildId) {
        final String search = option.getOption("id")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asString();

        // Get meme from database.
        final Memes meme;
        if (!numericPattern.matcher(search).matches()) {
            meme = memesRepository.findByNameContainingIgnoreCaseAndGuildId(search, guildId.asLong());
        } else {
            meme = memesRepository.findByIdAndGuildId(Long.parseLong(search), guildId.asLong());
        }

        if (meme == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Meme not found!");
        }

        // Send embed to user.
        return event.reply()
                .withEphemeral(false)
                .withEmbeds(EmbedCreateSpec.builder()
                        .title("["+ meme.getId() +"] " + meme.getName())
                        .image(meme.getUrl())
                        .build());
    }

    private Mono<Void> randomMeme(ChatInputInteractionEvent event, Snowflake guildId) {
        // Get memes list from database.
        final Iterable<Memes> memesList = memesRepository.findAllByGuildId(guildId.asLong());

        // Convert iterable to list and pick a random meme.
        final List<Memes> memes = StreamSupport.stream(memesList.spliterator(), false).toList();
        final Memes meme = memes.get(new Random().nextInt(memes.size()));

        // Send embed to user.
        return event.reply()
                .withEphemeral(false)
                .withEmbeds(EmbedCreateSpec.builder()
                        .title("["+ meme.getId() +"] " + meme.getName())
                        .image(meme.getUrl())
                        .build());
    }

    @Override
    public String getName() {
        return "memes";
    }

    @Override
    public Permission getPermission() {
        return Permission.SEND_MESSAGES;
    }
}