/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.services.version;


import java.util.Date;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Separate config file to handle the version checker
 */
public final class VersionCheckerConfig
{

	private static final int DEFAULT_INTERVAL_HOURS = 24;
	private static final int MIN_INTERVAL_HOURS = 0;
	private static final int MAX_INTERVAL_HOURS = 7 * 24;

	private final Config config;

	private final ForgeConfigSpec spec;

	private VersionCheckerConfig(final Config config, ForgeConfigSpec spec)
	{
		this.config = config;
		this.spec = spec;
	}

	public boolean isVersionCheckingEnabled()
	{
		return config.isEnabled.get();
	}

	public long lastCheck()
	{
		return config.lastCheck.get();
	}

	/**
	 * Stores the current date in milli seconds into the "lastCheck" field of the config and makes it persistent.
	 */
	public void updateLastCheck()
	{
		final Date now = new Date();
		final long nowInMs = now.getTime();
		this.config.lastCheck.set(nowInMs);
	}

	public int interval()
	{
		return config.interval.get();
	}

	public String level()
	{
		return config.level.get();
	}

	public boolean shouldNotifyPlayer()
	{
		return config.shouldNotifyPlayer.get();
	}

	public boolean shouldPostChangelog()
	{
		return config.shouldPostChangelog.get();
	}

	public void save()
	{
		spec.save();
	}

	public static VersionCheckerConfig create() {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
		Config config = specPair.getLeft();
		ForgeConfigSpec spec = specPair.getRight();

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);

		return new VersionCheckerConfig(config, spec);
	}

	private static class Config {

		final ConfigValue<Boolean> isEnabled;

		final ConfigValue<Long> lastCheck;

		final ConfigValue<Integer> interval;

		final ConfigValue<String> level;

		final ForgeConfigSpec.BooleanValue shouldNotifyPlayer;

		final ForgeConfigSpec.BooleanValue shouldPostChangelog;

		public Config(ForgeConfigSpec.Builder builder) {
			// initializes default values by caching
			builder.push("general");
			this.isEnabled = builder
					.comment("If true, the version checker is enabled. Acts as a master switch.")
					.define("enabled", true);
			builder.pop();

			builder.push("cache");
			this.lastCheck = builder
					.comment("The number of milliseconds since January 1, 1970, 00:00:00 GMT of the last successful check.")
					.define("lastCheck", 0L);
			this.interval = builder
					.comment("Waits as many hours, until it checks again.")
					.define( "interval", DEFAULT_INTERVAL_HOURS, val -> {
						if (!(val instanceof Integer)) {
							return false;
						}
						int intVal = (Integer) val;
						return (intVal >= MIN_INTERVAL_HOURS) && (intVal <= MAX_INTERVAL_HOURS);
					});
			builder.pop();

			builder.push("channel");
			this.level = builder.comment("Determines the channel level which should be checked for updates. Can be either Stable, Beta or Alpha.")
					.define("level", "Beta");
			builder.pop();

			builder.push("client");
			this.shouldNotifyPlayer = builder.comment("If true, the player is getting a notification, that a new version is available.")
					.define("notify", true);
			this.shouldPostChangelog = builder.comment("If true, the player is getting a notification including changelog. Only happens if notification are enabled.")
					.define("changelog", true);
			builder.pop();
		}
	}

}
