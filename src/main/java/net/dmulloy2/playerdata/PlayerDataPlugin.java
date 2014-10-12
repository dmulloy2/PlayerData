/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.dmulloy2.playerdata.backend.Backend;
import net.dmulloy2.playerdata.backend.YAMLBackend;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dmulloy2
 */

public class PlayerDataPlugin extends JavaPlugin
{
	// Instance
	private static PlayerDataPlugin instance;
	private Backend backend;

	@Override
	public void onEnable()
	{
		instance = this;

		backend = new YAMLBackend();
		getLogger().info("Using the " + backend.getName() + " backend!");
	}

	// ---- Getters

	public static PlayerDataPlugin getInstance()
	{
		return instance;
	}

	public static Backend getBackend()
	{
		return instance.backend;
	}

	// ---- Utility methods

	private static Method getOnlinePlayers;

	@SuppressWarnings("unchecked")
	public static List<Player> getOnlinePlayers()
	{
		try
		{
			// Provide backwards compatibility
			if (getOnlinePlayers == null)
				getOnlinePlayers = Bukkit.class.getMethod("getOnlinePlayers");
			if (getOnlinePlayers.getReturnType() != Collection.class)
				return Arrays.asList((Player[]) getOnlinePlayers.invoke(null));
		} catch (Throwable ex) { }
		return (List<Player>) Bukkit.getOnlinePlayers();
	}

	public static String trimFileExtension(File file, String extension)
	{
		Validate.notNull(file, "file cannot be null!");
		Validate.notNull(extension, "extension cannot be null!");

		int index = file.getName().lastIndexOf(extension);
		return index > 0 ? file.getName().substring(0, index) : file.getName();
	}

	public static String trimFileExtension(String name, String extension)
	{
		Validate.notNull(name, "name cannot be null!");
		Validate.notNull(extension, "extension cannot be null!");

		int index = name.lastIndexOf(extension);
		return index > 0 ? name.substring(0, index) : name;
	}
}