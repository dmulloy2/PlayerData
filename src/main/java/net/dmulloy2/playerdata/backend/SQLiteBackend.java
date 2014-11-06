/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.sql.DriverManager;

import net.dmulloy2.playerdata.PlayerDataPlugin;

/**
 * @author dmulloy2
 */

public class SQLiteBackend extends SQLBackend
{
	private static final String DATABASE = "jdbc:sqlite:players.db";

	@Override
	public void initialize() throws Throwable
	{
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection(DATABASE);
		PlayerDataPlugin.log("Established connection to players.db");
	}

	@Override
	public String getName()
	{
		return "SQLite";
	}
}