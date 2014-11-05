/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.dmulloy2.playerdata.PlayerDataPlugin;
import net.dmulloy2.playerdata.types.AbstractPlayerData;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */

public class YAMLBackend implements Backend
{
	private final String extension = ".dat";
	private final String fileName = "players";
	private final File folder;

	public YAMLBackend()
	{
		this.folder = new File(PlayerDataPlugin.getInstance().getDataFolder(), fileName);
		if (! folder.exists())
			folder.mkdirs();
	}

	@Override
	public void initialize() throws Throwable {
		//
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractPlayerData> T load(String key, Plugin plugin, Class<T> clazz)
	{
		try
		{
			Validate.notEmpty(key, "key cannot be null or empty!");
			Validate.notNull(plugin, "plugin cannot be null!");
			Validate.notNull(clazz, "clazz cannot be null!");

			File file = new File(folder, key + extension);
			if (! file.exists())
				return null;

			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
			Map<String, Object> values = yaml.getValues(false);
			Map<String, Object> args = (Map<String, Object>) values.get(getDataKey(plugin));
			return (T) ConfigurationSerialization.deserializeObject(args);
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to load data for: " + key, ex);
			return null;
		}
	}

	@Override
	public <T extends AbstractPlayerData> void save(String key, Plugin plugin, T instance)
	{
		try
		{
			Validate.notEmpty(key, "key cannot be null or empty!");
			Validate.notNull(plugin, "plugin cannot be null!");
			Validate.notNull(instance, "instance cannot be null!");

			File file = new File(folder, key + extension);
			if (! file.exists())
				file.createNewFile();

			String dataKey = getDataKey(plugin);
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
			for (Entry<String, Object> entry : instance.serialize().entrySet())
			{
				yaml.set(dataKey + "." + entry.getKey(), entry.getValue());
			}

			yaml.save(file);
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to save data for: " + key, ex);
		}
	}

	@Override
	public List<String> getAllDataKeys()
	{
		List<String> keys = new ArrayList<>();

		for (String file : folder.list())
		{
			if (file.contains(extension))
				keys.add(PlayerDataPlugin.trimFileExtension(file, extension));
		}

		return keys;
	}

	private final String getDataKey(Plugin plugin)
	{
		String name = plugin.getName();
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	@Override
	public String getName()
	{
		return "YAML";
	}
}