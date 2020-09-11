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

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;

import appeng.core.AEConfig;

public class AEMetrics {

    private static RegistryWrapper INSTANCE;

    private AEMetrics() {
    }

    public static void init(boolean enabled, RegistryType type) {
        if (!enabled) {
            return;
        }

        AEConfig config = AEConfig.instance();
        switch (type) {
            case INFLUX:
                INSTANCE = new InfluxMetrics(config.getMetricsInfluxServer(), config.getMetricsInfluxDatabase(),
                        config.getMetricsInfluxUser(), config.getMetricsInfluxPassword(),
                        Duration.ofSeconds(config.getMetricsInfluxStep()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported type");
        }

        if (config.getMetricsJVM()) {
            addJVMMetrics();
        }

        Metrics.addRegistry(INSTANCE.registry());
    }

    public static void stop() {
        if (INSTANCE != null) {
            Metrics.removeRegistry(INSTANCE.registry());
            INSTANCE.stop();
        }
    }

    private static void addJVMMetrics() {
        new ClassLoaderMetrics().bindTo(Metrics.globalRegistry);
        new JvmMemoryMetrics().bindTo(Metrics.globalRegistry);
        new JvmGcMetrics().bindTo(Metrics.globalRegistry);
        new ClassLoaderMetrics().bindTo(Metrics.globalRegistry);
        new ProcessorMetrics().bindTo(Metrics.globalRegistry);
        new JvmThreadMetrics().bindTo(Metrics.globalRegistry);
    }

}
