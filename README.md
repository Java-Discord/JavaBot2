# JavaBot2
The second iteration of the general-purpose bot for managing the Java Discord server.

**Currently very-much a work-in-progress. Do not commit to `main` without a pull request!**.

## Running the Bot
The bot requires a few dependencies, such as PostgreSQL and MongoDB. You can either install your own instances of these services, or use the included `docker-compose.yaml` file to boot up all of them in docker containers.

To do this, execute the following command from the terminal:
```bash
docker-compose -p javabot up
```

For your convenience, the docker-compose file also includes admin tools that can be useful for debugging.
- MongoExpress is available at [localhost:5050](http://localhost:5050)
- PgAdmin is available at [locahost:5051](http://localhost:5051)

Once those services are in order, you can run the bot as you would any Java program, and it will generate a `config` directory. Stop the bot, and set all the necessary properties in `systems.json`.

## Commands
Commands are defined in YAML files located in `src/main/resources/commands`. The data in these files are transformed into an array of `CommandConfig` objects using JSON deserialization. These commands are then used in `SlashCommandListener#registerSlashCommands` to register all slash commands.

**Each command MUST define a `handler` property whose value is the fully-qualified class name of a class implementing `SlashCommandHandler`**. When registering commands, the bot will look for such a class, and attempt to create a new instance of it using a no-args constructor. Therefore, make sure your handler class has a no-args constructor.

## Privileges
To specify that a command should only be allowed to be executed by certain people, you can specify a list of privileges. For example:
```yaml
- name: jam-admin
  description: Administrator actions for configuring the Java Jam.
  handler: com.javadiscord.javabot2.jam.JamAdminCommandHandler
  enabledByDefault: false
  privileges:
    - type: ROLE
      id: jam.adminRoleId
    - type: USER
      id: 235439851263098880
```
In this example, we define that the `jam-admin` command is first of all, *not enabled by default*, and also we say that anyone from the `jam.adminRoleId` role (as found using `Bot.config.getJam().getAdminRoleId()`). Additionally, we also say that the user whose id is `235439851263098880` is allowed to use this command. See `BotConfig#resolve(String)` for more information about how role names are resolved at runtime.

# Configuration
The bot's configuration consists of a collection of simple JSON files:
- `systems.json` contains global settings for the bot's core systems.
- For every guild, a `{guildId}.json` file exists, which contains any guild-specific configuration settings.

At startup, the bot will initially start by loading just the global settings, and then when the Discord ready event is received, the bot will add configuration for each guild it's in, loading it from the matching JSON file, or creating a new file if needed.
