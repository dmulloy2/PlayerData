/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.core;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.logging.Level;

import net.dmulloy2.playerdata.backend.Backend;
import net.dmulloy2.playerdata.backend.MySQLBackend;
import net.dmulloy2.playerdata.backend.SQLBackend;
import net.dmulloy2.playerdata.backend.SQLiteBackend;
import net.dmulloy2.playerdata.backend.YAMLBackend;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dmulloy2
 */

public class PlayerData extends JavaPlugin
{
	private static PlayerData instance;
	private Backend backend;

	@Override
	public void onEnable()
	{
		instance = this;

		// Configuration
		saveDefaultConfig();
		reloadConfig();

		// Debug logger
		DebugLogger.initialize(this);

		// Determine backend
		String backendName = getConfig().getString("backend", "YAML");
		if (backendName.equalsIgnoreCase("YAML"))
		{
			backend = new YAMLBackend(this);
		}
		else if (backendName.equalsIgnoreCase("SQLite"))
		{
			backend = new SQLiteBackend(this);
		}
		else if (backendName.equalsIgnoreCase("MySQL"))
		{
			backend = new MySQLBackend(this);
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

	@Override
	public void onDisable()
	{
		// Clear the debug logger
		DebugLogger.clear();

		// Attempt to close any SQL connections we might have
		if (backend instanceof SQLBackend)
		{
			SQLBackend sql = (SQLBackend) backend;
			Connection connection = sql.getConnection();
			if (connection != null)
			{
				try
				{
					connection.close();
				} catch (Throwable ex) { }
			}
		}
	}

	// ---- Getters

	// TODO: I hate this
	public static PlayerData getInstance()
	{
		return instance;
	}

	public Backend getBackend()
	{
		return backend;
	}

	// ---- Logging

	public void log(Level level, String msg, Object... args)
	{
		getLogger().log(level, MessageFormat.format(msg, args));
	}

	public void log(String msg, Object... args)
	{
		log(Level.INFO, msg, args);
	}
}