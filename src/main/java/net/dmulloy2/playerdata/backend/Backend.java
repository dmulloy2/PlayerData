/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.util.List;

import net.dmulloy2.playerdata.types.AbstractPlayerData;

import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */

public interface Backend
{
	void initialize() throws Throwable;

	<T extends AbstractPlayerData> T load(String key, Plugin plugin, Class<T> clazz);

	<T extends AbstractPlayerData> void save(String key, Plugin plugin, T instance);

	List<String> getAllDataKeys();

	String getName();
}