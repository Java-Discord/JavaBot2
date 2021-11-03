package net.javadiscord.javabot2.command.data;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple helper class that loads an array of {@link CommandConfig} instances
 * from the commands.yaml file.
 */
public class CommandDataLoader {
	private CommandDataLoader() {}

	/**
	 * Loads an array of {@link CommandConfig} from the given set of classpath
	 * resources.
	 * @param resources The list of resources to read from.
	 * @return An array of slash command data objects.
	 */
	public static CommandConfig[] load(String... resources) {
		Yaml yaml = new Yaml();
		Set<CommandConfig> commands = new HashSet<>();
		for (var resource : resources) {
			InputStream is = CommandDataLoader.class.getClassLoader().getResourceAsStream(resource);
			if (is == null) {
				System.err.println("Could not load commands from resource: " + resource);
				continue;
			}
			CommandConfig[] cs = yaml.loadAs(is, CommandConfig[].class);
			commands.addAll(Arrays.stream(cs).toList());
		}
		return commands.toArray(new CommandConfig[0]);
	}
}
