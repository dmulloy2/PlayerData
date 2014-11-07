/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.dmulloy2.playerdata.PlayerDataPlugin;
import net.dmulloy2.playerdata.types.AbstractData;
import net.dmulloy2.playerdata.util.Util;

import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */

public abstract class SQLBackend implements Backend
{
	private PreparedStatement allKeysStatement;
	private PreparedStatement loadStatement;
	protected Connection connection;

	@Override
	public abstract void initialize() throws Throwable;

	@Override
	public <T extends AbstractData> T load(String type, String key, Plugin plugin, Class<T> clazz)
	{
		try
		{
			if (loadStatement == null)
				loadStatement = connection.prepareStatement("SELECT * FROM " + type + " WHERE identifier=?");

			loadStatement.setString(1, key);
			ResultSet results = loadStatement.executeQuery();
			Constructor<T> constructor = clazz.getConstructor(ResultSet.class, Plugin.class);
			return constructor.newInstance(results, plugin);
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.SEVERE, "Failed to load data for " + key + ":", ex);
			return null;
		}
	}

	@Override
	public <T extends AbstractData> void save(String type, String key, Plugin plugin, T instance)
	{
		try
		{
			Statement statement = connection.createStatement();
			String query = "UPDATE " + type + " SET";
			String dataKey = Util.getDataKey(plugin) + ".";
			Map<String, Object> args = instance.serialize();
			for (Entry<String, Object> entry : args.entrySet())
			{
				query += " " + dataKey + entry.getKey() + "=" + entry.getValue();
			}
			query += " WHERE identifier=" + key;
			statement.executeQuery(query);
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.SEVERE, "Failed to save data for " + key + ":", ex);
		}
	}

	@Override
	public List<String> getKeys(String type)
	{
		try
		{
			if (allKeysStatement == null)
				allKeysStatement = connection.prepareStatement("SELECT identifier from " + type);
			ResultSet results = allKeysStatement.executeQuery();

			int i = 0;
			List<String> ret = new ArrayList<>();
			while (results.next())
			{
				ret.add(results.getString(i));
				i++;
			}

			return ret;
		}
		catch (Throwable ex)
		{
			PlayerDataPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to obtain keys for type " + type + ":", ex);
			return null;
		}
	}

	@Override
	public abstract String getName();

}