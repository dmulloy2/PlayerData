/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import net.dmulloy2.playerdata.core.PlayerData;
import net.dmulloy2.playerdata.types.AbstractData;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dmulloy2
 */

public abstract class AbstractDataCache<T extends AbstractData>
{
	protected final Class<T> type;
	protected final ConcurrentMap<String, T> cache;

	protected final PlayerData main;
	protected final JavaPlugin plugin;
	public AbstractDataCache(JavaPlugin plugin, Class<T> type)
	{
		this.main = PlayerData.getInstance();
		this.cache = new ConcurrentHashMap<>(64, 0.75F, 64);
		this.plugin = plugin;
		this.type = type;
	}

	// ---- Data Getters

	public final T getData(String key)
	{
		// Check cache first
		T data = cache.get(key);
		if (data == null)
		{
			// Attempt to load it
			data = loadData(key);
			if (data == null)
			{
				// Corrupt data :(
				return null;
			}

			// Cache it
			cache.put(key, data);
		}

		return data;
	}

	// ---- Data Management

	public final T newData(String key)
	{
		// Construct
		T data;

		try
		{
			data = type.newInstance();
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to create new data for " + key, ex);
			return null;
		}

		// Default values
		data.setDefaults();

		// Cache and return
		cache.put(key, data);
		return data;
	}

	private final T loadData(String key)
	{
		try
		{
			T data = main.getBackend().load(key, plugin, type);
			return data;
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to load data for: " + key, ex);
			return null;
		}
	}

	public final void save()
	{
		long start = System.currentTimeMillis();

		plugin.getLogger().info("Saving players to disk...");

		for (Entry<String, T> entry : getAllLoadedData().entrySet())
		{
			main.getBackend().save(entry.getKey(), plugin, entry.getValue());
		}

		plugin.getLogger().info("Players saved! Took " + (System.currentTimeMillis() - start) + " ms.");
	}

	public abstract void cleanupData();

	// ---- Mass Getters

	public final Map<String, T> getAllLoadedData()
	{
		return Collections.unmodifiableMap(cache);
	}

	public final Map<String, T> getAllData()
	{
		Map<String, T> data = new HashMap<>();
		data.putAll(cache);

		List<String> keys = main.getBackend().getKeys();
		for (String key : keys)
		{
			if (! isFileLoaded(key))
				data.put(key, loadData(key));
		}

		return Collections.unmodifiableMap(data);
	}

	// ---- Util

	private final boolean isFileLoaded(String fileName)
	{
		return cache.keySet().contains(fileName);
	}
}