/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.sql.DriverManager;

import net.dmulloy2.playerdata.core.PlayerData;

/**
 * @author dmulloy2
 */

public class SQLiteBackend extends SQLBackend
{
	private static final String DATABASE = "jdbc:sqlite:players.db";

	public SQLiteBackend(PlayerData main)
	{
		super(main);
	}

	@Override
	public void initialize() throws Throwable
	{
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection(DATABASE);
		if (connection == null)
			throw new IllegalStateException("Failed to connect to database!");

		createTable();
		main.log("Established database connection to players.db");
	}

	@Override
	public String getName()
	{
		return "SQLite";
	}
}