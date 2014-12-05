/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.dmulloy2.playerdata.core.PlayerData;
import net.dmulloy2.playerdata.types.AbstractData;
import net.dmulloy2.playerdata.util.Util;

import org.bukkit.plugin.Plugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author dmulloy2
 */

public abstract class SQLBackend implements Backend
{
	private static final String TABLE = "players";

	protected Connection connection;

	protected final PlayerData main;
	public SQLBackend(PlayerData main)
	{
		this.main = main;
	}

	@Override
	public abstract void initialize() throws Throwable;

	protected final void createTable() throws SQLException
	{
		// Create the table
		Statement statement = connection.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + " (identifier VARCHAR(255));";
		statement.executeUpdate(sql);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractData> T load(String identifier, Plugin plugin, Class<T> clazz)
	{
		if (connection == null)
			throw new IllegalStateException("Connection does not exist!");

		try
		{
			String sql = "SELECT " + plugin + " FROM " + TABLE + " WHERE identifier='" + identifier + "';";
			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(sql);

			String dataKey = Util.getDataKey(plugin);

			// Deserialize
			String xml = results.getString(dataKey);
			XStream xStream = new XStream(new DomDriver());
			xStream.alias("map", Map.class);
			Map<String, Object> values = (Map<String, Object>) xStream.fromXML(xml);

			Constructor<?> construct = clazz.getConstructor(Map.class);
			if (construct == null)
				throw new NoSuchMethodException("Failed to obtain constructor for " + clazz);

			return (T) construct.newInstance(values);
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to load data for " + identifier + ":", ex);
			return null;
		}
	}

	@Override
	public <T extends AbstractData> void save(String identifier, Plugin plugin, T data)
	{
		if (connection == null)
			throw new IllegalStateException("Connection does not exist!");

		try
		{
			// Serialize and convert to xml
			Map<String, Object> values = data.serialize();
			XStream xStream = new XStream(new DomDriver());
			xStream.alias("map", Map.class);
			String xml = xStream.toXML(values);

			String sql = "";
			Statement statement = connection.createStatement();

			String dataKey = Util.getDataKey(plugin);

			// Ensure plugin column exists
			if (! columnExists(dataKey))
			{
				sql = "ALTER TABLE " + TABLE + " ADD " + dataKey + " VARCHAR(255);";
				statement.executeUpdate(sql);
			}

			if (! rowExists("identifier", identifier))
			{
				// Insert new data
				sql = "INSERT INTO " + TABLE + " (identifier, " + plugin + ") VALUES ('" + identifier + "', " + "'" + xml + "');";
				statement.executeUpdate(sql);
			}
			else
			{
				// Update existing data
				sql = "UPDATE " + TABLE + " SET " + plugin + "='" + xml + "' WHERE identifier='" + identifier + "';";
				statement.executeUpdate(sql);
			}
		}
		catch (Throwable ex)
		{
			plugin.getLogger().log(Level.WARNING, "Failed to save data for " + identifier + ":", ex);
		}
	}

	@Override
	public List<String> getKeys()
	{
		if (connection == null)
			throw new IllegalStateException("Connection does not exist!");

		try
		{
			List<String> ret = new ArrayList<>();

			String sql = "SELECT identifier FROM " + TABLE;
			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery(sql);
			while (results.next())
			{
				ret.add(results.getString("identifier"));
			}

			return ret;
		}
		catch (Throwable ex)
		{
			main.getLogger().log(Level.WARNING, "Failed to obtain keys:", ex);
			return null;
		}
	}

	private final boolean columnExists(String column)
	{
		try
		{
			String sql = "SELECT " + column + " from " + TABLE + ";";
			Statement statement = connection.createStatement();
			return statement.executeQuery(sql).next();
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	private final boolean rowExists(String identifier, String row)
	{
		try
		{
			String sql = "SELECT * from " + TABLE + " WHERE " + identifier + "='" + row + "'";
			Statement statement = connection.createStatement();
			return statement.executeQuery(sql).next();
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	@Override
	public abstract String getName();

	public final Connection getConnection()
	{
		return connection;
	}

}