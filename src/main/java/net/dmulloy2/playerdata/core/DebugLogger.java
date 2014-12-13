/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.core;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dmulloy2
 */
public class DebugLogger
{
	private DebugLogger() { }

	private static boolean enabled;
	private static Logger logger;

	public static void initialize(PlayerData main)
	{
		logger = main.getLogger();
		enabled = main.getConfig().getBoolean("debug");
	}

	public static void debug(Level level, String msg, Object... args)
	{
		if (enabled && logger != null)
		{
			logger.log(level, MessageFormat.format(msg, args));
		}
	}

	public static void debug(String msg, Object... args)
	{
		debug(Level.INFO, msg, args);
	}

	public static void clear()
	{
		enabled = false;
		logger = null;
	}
}
