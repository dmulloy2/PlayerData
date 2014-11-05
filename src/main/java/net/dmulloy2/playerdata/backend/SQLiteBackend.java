/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.dmulloy2.playerdata.PlayerDataPlugin;
import net.dmulloy2.playerdata.types.AbstractPlayerData;

import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */
public class SQLiteBackend implements Backend {
	private static final String DATABASE = "jdbc:sqlite:players.db";

	private PreparedStatement allKeysStatement;
	private PreparedStatement loadStatement;
	private Connection connection;

	@Override
	public void initialize() throws Throwable {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection(DATABASE);
		PlayerDataPlugin.log("Established connection to players.db");
	}

	@Override
	public <T extends AbstractPlayerData> T load(String key, Plugin plugin, Class<T> clazz) {
		try {
			if (loadStatement == null)
				loadStatement = connection.prepareStatement("SELECT * FROM players WHERE uniqueId = ?");

			loadStatement.setString(1, key);
			ResultSet results = loadStatement.executeQuery();
			Constructor<T> constructor = clazz.getConstructor(ResultSet.class);
			return constructor.newInstance(results);
		} catch (Throwable ex) {
			plugin.getLogger().log(Level.SEVERE, "Failed to load data for " + key + ":", ex);
			return null;
		}
	}

	@Override
	public <T extends AbstractPlayerData> void save(String key, Plugin plugin, T instance) {
		try {
			Statement statement = connection.createStatement();
			String query = "UPDATE players SET";
			Map<String, Object> args = instance.serialize();
			for (Entry<String, Object> entry : args.entrySet()) {
				query += " " + entry.getKey() + "=" + entry.getValue();
			}
			query += " WHERE uniqueId=" + key;
			statement.executeQuery(query);
		} catch (Throwable ex) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save data for " + key + ":", ex);
		}
	}

	@Override
	public List<String> getAllDataKeys() {
		try {
			if (allKeysStatement == null)
				allKeysStatement = connection.prepareStatement("SELECT uniqueId from players");
			ResultSet results = allKeysStatement.executeQuery();

			int i = 0;
			List<String> ret = new ArrayList<>();
			while (results.next()) {
				ret.add(results.getString(i));
				i++;
			}

			return ret;
		} catch (Throwable ex) {
			PlayerDataPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to obtain data keys:", ex);
			return null;
		}
	}

	@Override
	public String getName() {
		return "SQLite";
	}

}