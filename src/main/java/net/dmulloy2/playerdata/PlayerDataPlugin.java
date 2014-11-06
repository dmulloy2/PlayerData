/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata;

import java.text.MessageFormat;
import java.util.logging.Level;

import net.dmulloy2.playerdata.backend.Backend;
import net.dmulloy2.playerdata.backend.SQLiteBackend;
import net.dmulloy2.playerdata.backend.YAMLBackend;

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

		// Configuration
		saveDefaultConfig();
		reloadConfig();

		// Determine backend
		String backendName = getConfig().getString("backend", "YAML");
		if (backendName.equalsIgnoreCase("YAML"))
		{
			backend = new YAMLBackend();
		}
		else if (backendName.equalsIgnoreCase("SQLite"))
		{
			backend = new SQLiteBackend();
		}
		else if (backendName.equalsIgnoreCase("MySQL"))
		{
			// TODO: This
			// backend = new MySQLBackend();
		}
		else
		{
			getLogger().severe("Unknown backend: " + backendName);
			setEnabled(false);
			return;
		}

		try
		{
			backend.initialize();
		}
		catch (Throwable ex)
		{
			getLogger().log(Level.SEVERE, "Failed to initialize " + backend.getName() + " backend:", ex);
			setEnabled(false);
			return;
		}

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

	// ---- Logging

	public static void log(Level level, String msg, Object... args)
	{
		instance.getLogger().log(level, MessageFormat.format(msg, args));
	}

	public static void log(String msg, Object... args)
	{
		log(Level.INFO, msg, args);
	}
}