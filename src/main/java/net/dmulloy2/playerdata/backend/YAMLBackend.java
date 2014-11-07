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
import net.dmulloy2.playerdata.types.AbstractData;
import net.dmulloy2.playerdata.util.Util;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */

public class YAMLBackend implements Backend
{
	private static final String EXTENSION = ".dat";
	private File dataFolder;

	@Override
	public void initialize() throws Throwable
	{
		this.dataFolder = PlayerDataPlugin.getInstance().getDataFolder();
		if (! dataFolder.exists())
			dataFolder.mkdirs();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractData> T load(String type, String key, Plugin plugin, Class<T> clazz)
	{
		try
		{
			Validate.notEmpty(type, "type cannot be empty!");
			Validate.notEmpty(key, "key cannot be null or empty!");
			Validate.notNull(plugin, "plugin cannot be null!");
			Validate.notNull(clazz, "clazz cannot be null!");

			File folder = new File(dataFolder, type);
			if (! folder.exists())
				folder.mkdirs();

			File file = new File(folder, key + EXTENSION);
			if (! file.exists())
				return null;

			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
			Map<String, Object> values = yaml.getValues(false);
			Map<String, Object> args = (Map<String, Object>) values.get(Util.getDataKey(plugin));
			return (T) ConfigurationSerialization.deserializeObject(args);
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to load data for: " + key, ex);
			return null;
		}
	}

	@Override
	public <T extends AbstractData> void save(String type, String key, Plugin plugin, T instance)
	{
		try
		{
			Validate.notEmpty(type, "type cannot be empty!");
			Validate.notEmpty(key, "key cannot be null or empty!");
			Validate.notNull(plugin, "plugin cannot be null!");
			Validate.notNull(instance, "instance cannot be null!");

			File folder = new File(dataFolder, type);
			if (! folder.exists())
				folder.mkdirs();

			File file = new File(folder, key + EXTENSION);
			if (! file.exists())
				file.createNewFile();

			String dataKey = Util.getDataKey(plugin);
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
	public List<String> getKeys(String type)
	{
		List<String> keys = new ArrayList<>();

		File folder = new File(dataFolder, type);
		if (! folder.exists())
			return keys;

		for (String file : folder.list())
		{
			if (file.contains(EXTENSION))
				keys.add(Util.trimFileExtension(file, EXTENSION));
		}

		return keys;
	}

	@Override
	public String getName()
	{
		return "YAML";
	}
}