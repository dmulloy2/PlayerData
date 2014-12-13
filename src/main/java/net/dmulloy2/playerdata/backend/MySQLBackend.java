/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.backend;

import java.sql.DriverManager;

import org.apache.commons.lang.Validate;

import net.dmulloy2.playerdata.core.PlayerData;

/**
 * @author dmulloy2
 */

public class MySQLBackend extends SQLBackend
{
	public MySQLBackend(PlayerData main)
	{
		super(main);
	}

	@Override
	public void initialize() throws Throwable
	{
		String url = main.getConfig().getString("mySQL.url");
		Validate.notEmpty(url, "url cannot be null or empty!");

		String username = main.getConfig().getString("mySQL.username");
		Validate.notEmpty(username, "username cannot be null or empty!");

		String password = main.getConfig().getString("mySQL.password");
		Validate.notEmpty(password, "password cannot be null or empty!");

		connection = DriverManager.getConnection(url, username, password);
		Validate.notNull(connection, "Failed to establish database connection!");

		createTable();
		main.log("Established database connection to {0}", url);
	}

	@Override
	public String getName()
	{
		return "MySQL";
	}
}