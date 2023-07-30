package br.com.zrage.maidbot;

import br.com.zrage.maidbot.core.BotConfiguration;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;

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
}
