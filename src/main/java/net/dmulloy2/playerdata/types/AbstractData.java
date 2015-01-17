/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.playerdata.types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.dmulloy2.playerdata.core.DebugLogger;
import net.dmulloy2.playerdata.util.Util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

/**
 * @author dmulloy2
 */

public abstract class AbstractData implements ConfigurationSerializable
{
	public AbstractData() { }

	public AbstractData(Map<String, Object> args)
	{
		for (Entry<String, Object> entry : args.entrySet())
		{
			try
			{
				for (Field field : getClass().getDeclaredFields())
				{
					if (field.getName().equals(entry.getKey()))
					{
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						field.set(this, entry.getValue());
						field.setAccessible(accessible);
					}
				}
			}
			catch (Throwable ex)
			{
				DebugLogger.debug(Level.WARNING, Util.getUsefulStack(ex, "applying field " + entry.getKey()));
			}
		}
	}

	public AbstractData(ResultSet resultSet, Plugin plugin)
	{
		String dataKey = Util.getDataKey(plugin) + ".";
		for (Field field : getClass().getDeclaredFields())
		{
			try
			{
				Object value = resultSet.getObject(dataKey + field.getName());
				if (value != null)
				{
					boolean accessible = field.isAccessible();
					field.setAccessible(true);
					field.set(this, value);
					field.setAccessible(accessible);
				}
			}
			catch (Throwable ex)
			{
				DebugLogger.debug(Level.WARNING, Util.getUsefulStack(ex, "applying field " + field.getName()));
			}
		}
	}

	@Override
	public final Map<String, Object> serialize()
	{
		Map<String, Object> data = new LinkedHashMap<>();

		for (Field field : getClass().getDeclaredFields())
		{
			if (Modifier.isTransient(field.getModifiers()))
				continue;

			try
			{
				boolean accessible = field.isAccessible();

				field.setAccessible(true);

				if (field.getType().equals(Integer.TYPE))
				{
					if (field.getInt(this) != 0)
						data.put(field.getName(), field.getInt(this));
				}
				else if (field.getType().equals(Long.TYPE))
				{
					if (field.getLong(this) != 0)
						data.put(field.getName(), field.getLong(this));
				}
				else if (field.getType().equals(Boolean.TYPE))
				{
					if (field.getBoolean(this))
						data.put(field.getName(), field.getBoolean(this));
				}
				else if (field.getType().isAssignableFrom(Collection.class))
				{
					if (! ((Collection<?>) field.get(this)).isEmpty())
						data.put(field.getName(), field.get(this));
				}
				else if (field.getType().isAssignableFrom(String.class))
				{
					if (((String) field.get(this)) != null)
						data.put(field.getName(), field.get(this));
				}
				else if (field.getType().isAssignableFrom(Map.class))
				{
					if (! ((Map<?, ?>) field.get(this)).isEmpty())
						data.put(field.getName(), field.get(this));
				}
				else
				{
					if (field.get(this) != null)
						data.put(field.getName(), field.get(this));
				}

				field.setAccessible(accessible);
			}
			catch (Throwable ex)
			{
				DebugLogger.debug(Level.WARNING, Util.getUsefulStack(ex, "serializing field " + field.getName()));
			}
		}

		return data;
	}

	public void setDefaults() { }
}