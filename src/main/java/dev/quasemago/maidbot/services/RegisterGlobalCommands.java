package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.data.BotConfiguration;
import dev.quasemago.maidbot.helpers.Logger;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegisterGlobalCommands implements ApplicationRunner {
    @Autowired
    private RestClient client;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Create an ObjectMapper that supported Discord4J classes.
        final JacksonResources resourcesMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below.
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final ApplicationService applicationService = client.getApplicationService();
        final long applicationId = client.getApplicationId().block();

        // Get our commands from json file.
        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = resourcesMapper.getObjectMapper()
                            .readValue(resource.getInputStream(), ApplicationCommandRequest.class);
            commands.add(request);
        }

        // Bulk overwrite commands. This is now idempotent, so it is safe to use this even when only 1 command
        // is changed/added/removed.
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                .doOnNext(command -> Logger.log.info("Registered global command: " + command.name()))
                .doOnError(error -> Logger.log.error("Error registering global command: ", error))
                .subscribe();
    }
}
