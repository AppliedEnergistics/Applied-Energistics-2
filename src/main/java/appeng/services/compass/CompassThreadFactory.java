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

package appeng.services.compass;

import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * @author thatsIch
 * @version rv3 - 31.05.2015
 * @since rv3 31.05.2015
 */
public final class CompassThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(@Nonnull final Runnable job) {
        Preconditions.checkNotNull(job);
        final Thread compass = new Thread(job, "AE Compass Service");
        compass.setDaemon(true);

        return compass;
    }
}
