# DiscordMaidBot
DiscordMaidBot is a simple discord bot project made with java and spring boot.

## Commands
### Public commands
- `/ping` - Pong!
- `/about` - About the bot (author, version, etc)
- `/help` - List all commands.
- `/memes [add <url> <name>] [list] [get] [random]` - Memes commands.
  - `/memes add <url> <name>` - Add a new meme;
  - `/memes list` - List all memes;
  - `/memes get <id>` - Get a meme by id;
  - `/memes random` - Get a random meme.

### Admin commands
- `/stop` - Stop the bot.

## Dependencies
- Java JDK 17 (https://www.oracle.com/java/technologies/downloads/#java17)

## How to run
- Download the latest release ([here](https://github.com/quasemago/DiscordMaidBot/releases/latest)).
- Setup the `.env` file with:
  - Your bot credentials (``BOT_TOKEN``);
  - Your discord id (``BOT_OWNER``);
  - Your database credentials (``BOT_DATABASE_URL``).
- Run the jar file with the command `java -jar DiscordMaidBot.jar`.