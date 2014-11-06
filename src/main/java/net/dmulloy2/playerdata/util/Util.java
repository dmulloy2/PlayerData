/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */

public class Util
{
	public static final String getDataKey(Plugin plugin)
	{
		String name = plugin.getName();
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	private static Method getOnlinePlayers;

	@SuppressWarnings("unchecked")
	public static final List<Player> getOnlinePlayers()
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

	public static final String trimFileExtension(File file, String extension)
	{
		Validate.notNull(file, "file cannot be null!");
		Validate.notNull(extension, "extension cannot be null!");

		int index = file.getName().lastIndexOf(extension);
		return index > 0 ? file.getName().substring(0, index) : file.getName();
	}

	public static final String trimFileExtension(String name, String extension)
	{
		Validate.notNull(name, "name cannot be null!");
		Validate.notNull(extension, "extension cannot be null!");

		int index = name.lastIndexOf(extension);
		return index > 0 ? name.substring(0, index) : name;
	}
}
