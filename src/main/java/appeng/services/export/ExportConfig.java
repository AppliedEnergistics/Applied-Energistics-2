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


import javax.annotation.Nonnull;


/**
 * @author thatsIch
 * @version rv3 - 14.08.2015
 * @since rv3 14.08.2015
 */
public interface ExportConfig {
    /**
     * config switch to disable the exporting.
     * if the recipes system is not used
     * there is no reason to export them.
     * Still can be useful for debugging purpose,
     * thus not tying it to the recipe system directly.
     *
     * @return true if exporting is enabled
     */
    boolean isExportingItemNamesEnabled();

    /**
     * config switch for using the digest cache.
     *
     * @return true if cache is enabled
     */
    boolean isCacheEnabled();

    /**
     * config switch to always refresh the CSV. Might be useful to activate on debugging.
     *
     * @return true if force refresh is enabled
     */
    boolean isForceRefreshEnabled();

    /**
     * config switch to export more information mostly used for debugging
     *
     * @return true if additional information are enabled
     */
    boolean isAdditionalInformationEnabled();

    /**
     * Will get the cache from last session. Can be used to reduce I/O operations though containing itself calculation.
     *
     * @return a digest from the last calculation
     */
    String getCache();

    /**
     * sets the cache for the next session to reduce calculation overhead
     *
     * @param digest new digest for the cache
     */
    void setCache(@Nonnull String digest);

    /**
     * Will delegate the saving
     */
    void save();
}
