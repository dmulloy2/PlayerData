/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.dmulloy2.playerdata.types.StringJoiner;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * General utility class.
 *
 * @author dmulloy2
 */

public class Util
{
	/**
	 * Gets the data key for a given plugin.
	 * 
	 * @param plugin Plugin to get the data key for
	 * @return The data key
	 */
	public static final String getDataKey(Plugin plugin)
	{
		String name = plugin.getName();
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Returns a file's name without the extension.
	 * 
	 * @param file File
	 * @param extension Extension
	 * @return The file's name without the extension
	 */
	public static final String trimFileExtension(File file, String extension)
	{
		Validate.notNull(file, "file cannot be null!");
		Validate.notNull(extension, "extension cannot be null!");

		int index = file.getName().lastIndexOf(extension);
		return index > 0 ? file.getName().substring(0, index) : file.getName();
	}

	/**
	 * Returns a file's name without the extension.
	 * 
	 * @param name File name
	 * @param extension Extension
	 * @return The file's name without the extension
	 */
	public static final String trimFileExtension(String name, String extension)
	{
		Validate.notNull(name, "name cannot be null!");
		Validate.notNull(extension, "extension cannot be null!");

		int index = name.lastIndexOf(extension);
		return index > 0 ? name.substring(0, index) : name;
	}

	private static Method getOnlinePlayers;

	/**
	 * Gets a list of currently online players.
	 * 
	 * @return A list of currently online players
	 */
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

	/**
	 * Returns a useful Stack Trace for debugging purposes.
	 *
	 * @param ex {@link Throwable} to get the stack trace for
	 * @param circumstance Circumstance in which the Throwable was thrown
	 */
	public static String getUsefulStack(Throwable ex, String circumstance)
	{
		Validate.notNull(ex, "ex cannot be null!");

		StringJoiner joiner = new StringJoiner("\n");
		joiner.append("Encountered an exception" + (circumstance != null ? " while " + circumstance : "") + ": " + ex.toString());
		joiner.append("Affected classes:");

		for (StackTraceElement ste : ex.getStackTrace())
		{
			String className = ste.getClassName();
			if (! className.contains("net.minecraft"))
			{
				StringBuilder line = new StringBuilder();
				line.append("  " + className + "." + ste.getMethodName());
				if (ste.getLineNumber() > 0)
					line.append("(Line " + ste.getLineNumber() + ")");
				else
					line.append("(Native Method)");

				String jar = getWorkingJar(className);
				if (jar != null)
					line.append(" [" + jar + "]");

				joiner.append(line.toString());
			}
		}

		while (ex.getCause() != null)
		{
			ex = ex.getCause();
			joiner.append("Caused by: " + ex.toString());
			joiner.append("Affected classes:");
			for (StackTraceElement ste : ex.getStackTrace())
			{
				String className = ste.getClassName();
				if (! className.contains("net.minecraft"))
				{
					StringBuilder line = new StringBuilder();
					line.append("  " + className + "." + ste.getMethodName());
					if (ste.getLineNumber() > 0)
						line.append("(Line " + ste.getLineNumber() + ")");
					else
						line.append("(Native Method)");

					String jar = getWorkingJar(className);
					if (jar != null)
						line.append(" [" + jar + "]");

					joiner.append(line.toString());
				}
			}
		}

		return joiner.toString();
	}

	/**
	 * Gets the working jar of a given Class. This is the same as
	 * {@link #getWorkingJar(Class)}, but the class name is passed through
	 * {@link Class#forName(String)} first.
	 *
	 * @param clazzName Class name
	 * @return The working jar, or null if not found
	 */
	public static final String getWorkingJar(String clazzName)
	{
		try
		{
			return getWorkingJar(Class.forName(clazzName));
		} catch (Throwable ex) { }
		return null;
	}

	/**
	 * Gets the working jar of a given {@link Class}.
	 *
	 * @param clazz Class to get the jar for
	 * @return The working jar, or null if not found
	 */
	public static final String getWorkingJar(Class<?> clazz)
	{
		try
		{
			String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
			path = URLDecoder.decode(path, "UTF-8");
			path = path.substring(path.lastIndexOf("/") + 1);
			return ! path.isEmpty() ? path : null;
		} catch (Throwable ex) { }
		return null;
	}
}