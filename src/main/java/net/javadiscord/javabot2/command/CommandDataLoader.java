package net.javadiscord.javabot2.command;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collection;

/**
 * Simple helper class that loads an array of {@link CommandConfig} instances
 * from the commands.yaml file.
 */
public class CommandDataLoader {
	public static CommandConfig[] load(String... resources) {
		Yaml yaml = new Yaml();
		InputStream is = CommandDataLoader.class.getClassLoader().getResourceAsStream("commands.yaml");
		return yaml.loadAs(is, CommandConfig[].class);
	}
}
