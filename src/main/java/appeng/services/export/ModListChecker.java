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

package appeng.services.export;


import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import java.util.List;


/**
 * Checks the cached digest against the current mods including their versions.
 * Use the config to manipulate the process
 *
 * @author thatsIch
 * @version rv3 - 01.09.2015
 * @since rv3 - 01.09.2015
 */
final class ModListChecker implements Checker<List<ModContainer>> {
    private final String configHashValue;

    @Nonnull
    private final ExportConfig config;

    /**
     * @param config uses the config to retrieve the old hash of the mod list
     */
    ModListChecker(@Nonnull final ExportConfig config) {
        this.config = Preconditions.checkNotNull(config);
        this.configHashValue = Preconditions.checkNotNull(config.getCache());
    }

    /**
     * Compiles a list of all mods and their versions to a digest which is updated, if it differs from the config. This
     * is used to elevate the need to export
     * the csv once again, if no change was detected.
     *
     * @param modContainers all mods and their versions to check if a difference exists between the current instance and
     *                      the previous instance
     * @return CheckType.EQUAL if no change was detected
     */
    @Nonnull
    @Override
    public CheckType isEqual(@Nonnull final List<ModContainer> modContainers) {
        Preconditions.checkNotNull(modContainers);

        final StringBuilder builder = new StringBuilder();

        for (final ModContainer container : modContainers) {
            builder.append(container.getModId());
            builder.append(container.getVersion());
        }

        final String allModsAndVersions = builder.toString();
        final String hex = DigestUtils.md5Hex(allModsAndVersions);

        if (hex.equals(this.configHashValue)) {
            return CheckType.EQUAL;
        } else {
            this.config.setCache(hex);

            return CheckType.UNEQUAL;
        }
    }
}
