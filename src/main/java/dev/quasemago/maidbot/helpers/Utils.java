package dev.quasemago.maidbot.helpers;

import dev.quasemago.maidbot.data.BotConfiguration;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utils {
    public static boolean hasPermission(Guild guild, User user, Permission permission) {
        if (isBotOwner(user)) {
            return true;
        }

        if (Objects.isNull(guild)) {
            return false;
        }

        return (Objects.requireNonNull(Objects.requireNonNull(user.asMember(guild.getId())
                                .block())
                        .getBasePermissions()
                        .block())
                .contains(permission));
    }

    public static boolean isBotOwner(User user) {
        return user.getId().equals(BotConfiguration.getBotOwner());
    }

    public static List<String> readLastLine(File file, int numLastLineToRead) {
        List<String> result = new ArrayList<>();

        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null && result.size() < numLastLineToRead) {
                result.add(line);
            }
        } catch (IOException e) {
            Logger.log.error("", e);
        }

        return result;
    }
}