/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.cache;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.playerdata.types.AbstractPlayerData;
import net.dmulloy2.playerdata.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dmulloy2
 */

public abstract class AbstractPlayerDataCache<T extends AbstractPlayerData> extends AbstractDataCache<T>
{
	public AbstractPlayerDataCache(JavaPlugin plugin, Class<T> type)
	{
		super(plugin, type);
	}

	// ---- Data Getters

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

	public final T newData(Player player)
	{
		return newData(getKey(player));
	}

	@Override
	public final void cleanupData()
	{
		// Get all online players into an array list
		List<String> online = new ArrayList<>();
		for (Player player : Util.getOnlinePlayers())
			online.add(player.getName());

		// Actually cleanup the data
		for (String key : getAllLoadedData().keySet())
			if (! online.contains(key))
				cache.remove(key);

		// Clear references
		online.clear();
		online = null;
	}

	// ---- Util

	private final String getKey(OfflinePlayer player)
	{
		return player.getUniqueId().toString();
	}
}