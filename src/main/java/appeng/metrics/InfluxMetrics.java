/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020 Team AppliedEnergistics, All rights reserved.
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

package appeng.metrics;

import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;

public class InfluxMetrics implements RegistryWrapper {

    private final InfluxMeterRegistry registry;

    public InfluxMetrics(String server, String database, String user, String password, Duration step) {
        InfluxConfig config = new Config(server, database, user, password, step);
        this.registry = new InfluxMeterRegistry(config, Clock.SYSTEM);
    }

    @Override
    public MeterRegistry registry() {
        return registry;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        this.registry.stop();
    }

    private class Config implements InfluxConfig {
        private final String server;
        private final String database;
        private final String user;
        private final String password;
        private final Duration step;

        public Config(String server, String database, String user, String password, Duration step) {
            this.server = server;
            this.database = database;
            this.user = user;
            this.password = password;
            this.step = step;
        }

        @Override
        public String uri() {
            return server;
        }

        @Override
        public String db() {
            return database;
        }

        @Override
        public String userName() {
            return user;
        }

        @Override
        public String password() {
            return password;
        }

        @Override
        public Duration step() {
            return step;
        }

        @Override
        public String get(String key) {
            return null;
        }
    }
}
