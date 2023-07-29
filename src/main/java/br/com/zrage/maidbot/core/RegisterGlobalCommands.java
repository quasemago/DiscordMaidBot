package br.com.zrage.maidbot.core;

import br.com.zrage.maidbot.MaidbotApplication;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RegisterGlobalCommands implements ApplicationRunner {
    private final RestClient client;

    public RegisterGlobalCommands(RestClient client) {
        this.client = client;
    }

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
                .doOnNext(command -> MaidbotApplication.log.info("Registered global command: " + command.name()))
                .doOnError(e -> MaidbotApplication.log.error("Error registering global command: ", e))
                .subscribe();
    }
}
