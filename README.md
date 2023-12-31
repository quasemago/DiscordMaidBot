# DiscordMaidBot
DiscordMaidBot is a simple discord bot project made with java and spring boot.

## Commands
### Public commands
- `/ping` - Pong!
- `/about` - About the bot (author, version, etc)
- `/help` - List all commands.

### Admin commands
- `/logs [status] [toggle <status> <channel>]` - Log System.
  - `/logs status` - Get the current log channel and log types.
  - `/logs toggle <status> <channel>` - Configure log system (channel and types).

### Owner commands
- `/superadmin [stop] [logs]` - Super Admin (owner) commands.
  - `/superadmin stop` - Stop the bot.
  - `/superadmin logs` - Get the current last 150 lines of bot logs (for debug purpose).

## Dependencies
- Java JDK 17 (https://www.oracle.com/java/technologies/downloads/#java17)

## How to run
- Download the latest release ([here](https://github.com/quasemago/DiscordMaidBot/releases/latest)).
- Setup the `.env` file with:
  - Your discord id (``BOT_OWNER``);
  - Your bot credentials (``BOT_TOKEN``);
  - Bot default presence status (``BOT_DEFAULT_PRESENCE``);
    - Presence types: 0: playing, 1: streaming, 2: listening, 3: watching, 4: custom, 5: competing
  - Bot default language (``BOT_DEFAULT_LANGUAGE``);
    - Language types: en-US, pt-BR
  - Your database credentials (``BOT_DATABASE_URL``).
- Run the jar file with the command `java -jar DiscordMaidBot.jar`.