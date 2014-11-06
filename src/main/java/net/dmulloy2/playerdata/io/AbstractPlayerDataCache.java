/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import net.dmulloy2.playerdata.PlayerDataPlugin;
import net.dmulloy2.playerdata.types.AbstractPlayerData;
import net.dmulloy2.playerdata.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dmulloy2
 */

public class AbstractPlayerDataCache<T extends AbstractPlayerData>
{
	private final Class<T> type;
	private final ConcurrentMap<String, T> cache;

	private final JavaPlugin plugin;
	public AbstractPlayerDataCache(JavaPlugin plugin, Class<T> type)
	{
		this.cache = new ConcurrentHashMap<>(64, 0.75F, 64);
		this.plugin = plugin;
		this.type = type;
	}

	// ---- Data Getters

	private final T getData(String key)
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

	public final T getData(Player player)
	{
		T data = getData(getKey(player));

		// Online players always have data
		if (data == null)
			data = newData(player);

		// Update last known by
		data.setLastKnownBy(player.getName());

		// Return
		return data;
	}

	public final T getData(OfflinePlayer player)
	{
		// Slightly different handling for Players
		if (player.isOnline())
			return getData(player.getPlayer());

		// Attempt to get by name
		return getData(getKey(player));
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

	public final T newData(Player player)
	{
		return newData(getKey(player));
	}

	private final T loadData(String key)
	{
		try
		{
			T data = PlayerDataPlugin.getBackend().load(key, plugin, type);
			return data;
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to load player data for: " + key, ex);
			return null;
		}
	}

	public final void save()
	{
		long start = System.currentTimeMillis();
		plugin.getLogger().info("Saving players to disk...");

		for (Entry<String, T> entry : getAllLoadedPlayerData().entrySet())
		{
			PlayerDataPlugin.getBackend().save(entry.getKey(), plugin, entry.getValue());
		}

		plugin.getLogger().info("Players saved! Took " + (System.currentTimeMillis() - start) + " ms!");
	}

	public final void cleanupData()
	{
		// Get all online players into an array list
		List<String> online = new ArrayList<>();
		for (Player player : Util.getOnlinePlayers())
			online.add(player.getName());

		// Actually cleanup the data
		for (String key : getAllLoadedPlayerData().keySet())
			if (! online.contains(key))
				cache.remove(key);

		// Clear references
		online.clear();
		online = null;
	}

	// ---- Mass Getters

	public final Map<String, T> getAllLoadedPlayerData()
	{
		return Collections.unmodifiableMap(cache);
	}

	public final Map<String, T> getAllPlayerData()
	{
		Map<String, T> data = new HashMap<>();
		data.putAll(cache);

		List<String> keys = PlayerDataPlugin.getBackend().getAllDataKeys();

		for (String key : keys)
		{
			if (! isFileLoaded(key))
				data.put(key, loadData(key));
		}

		return Collections.unmodifiableMap(data);
	}

	// ---- Util

	private final String getKey(OfflinePlayer player)
	{
		return player.getUniqueId().toString();
	}

	private final boolean isFileLoaded(String fileName)
	{
		return cache.keySet().contains(fileName);
	}
}