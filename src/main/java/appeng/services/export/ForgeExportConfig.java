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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nonnull;


/**
 * Offers configuration switches for the user to change the export process
 *
 * @author thatsIch
 * @version rv3 - 14.08.2015
 * @since rv3 14.08.2015
 */
public final class ForgeExportConfig implements ExportConfig {
    private static final String GENERAL_CATEGORY = "general";
    private static final String CACHE_CATEGORY = "cache";

    private static final String EXPORT_ITEM_NAMES_KEY = "exportItemNames";
    private static final boolean EXPORT_ITEM_NAMES_DEFAULT = true;
    private static final String EXPORT_ITEM_NAMES_DESCRIPTION = "If true, all registered items will be exported containing the internal minecraft name and the localized name to actually find the item you are using. This also contains the item representation of the blocks, but are missing items, which are too much to display e.g. FMP.";

    private static final String ENABLE_FORCE_REFRESH_KEY = "enableForceRefresh";
    private static final boolean ENABLE_FORCE_REFRESH_DEFAULT = false;
    private static final String ENABLE_FORCE_REFRESH_DESCRIPTION = "If true, the CSV exporting will always happen. This will not use the cache to reduce the computation.";

    private static final String ENABLE_CACHE_KEY = "enableCache";
    private static final boolean ENABLE_CACHE_DEFAULT = true;
    private static final String ENABLE_CACHE_DESCRIPTION = "Caching can save processing time, if there are a lot of items.";

    private static final String ENABLE_ADDITIONAL_INFO_KEY = "enableAdditionalInfo";
    private static final boolean ENABLE_ADDITIONAL_INFO_DEFAULT = false;
    private static final String ENABLE_ADDITIONAL_INFO_DESCRIPTION = "Will output more detailed information into the CSV like corresponding items";

    private static final String DIGEST_KEY = "digest";
    private static final String DIGEST_DEFAULT = "";
    private static final String DIGEST_DESCRIPTION = "Digest of all the mods and versions to check if a re-export of the item names is required.";

    private final boolean exportItemNamesEnabled;
    private final boolean cacheEnabled;
    private final boolean forceRefreshEnabled;
    private final boolean additionalInformationEnabled;
    private final String cache;
    private final Configuration config;

    /**
     * Constructor using the configuration. Apparently there are some race conditions if constructing configurations on
     * multiple file accesses
     *
     * @param config to be wrapped configuration.
     */
    public ForgeExportConfig(@Nonnull final Configuration config) {
        this.config = Preconditions.checkNotNull(config);

        this.exportItemNamesEnabled = this.config.getBoolean(EXPORT_ITEM_NAMES_KEY, GENERAL_CATEGORY, EXPORT_ITEM_NAMES_DEFAULT,
                EXPORT_ITEM_NAMES_DESCRIPTION);
        this.cacheEnabled = this.config.getBoolean(ENABLE_CACHE_KEY, CACHE_CATEGORY, ENABLE_CACHE_DEFAULT, ENABLE_CACHE_DESCRIPTION);
        this.additionalInformationEnabled = this.config.getBoolean(ENABLE_ADDITIONAL_INFO_KEY, GENERAL_CATEGORY, ENABLE_ADDITIONAL_INFO_DEFAULT,
                ENABLE_ADDITIONAL_INFO_DESCRIPTION);
        this.cache = this.config.getString(DIGEST_KEY, CACHE_CATEGORY, DIGEST_DEFAULT, DIGEST_DESCRIPTION);
        this.forceRefreshEnabled = this.config.getBoolean(ENABLE_FORCE_REFRESH_KEY, GENERAL_CATEGORY, ENABLE_FORCE_REFRESH_DEFAULT,
                ENABLE_FORCE_REFRESH_DESCRIPTION);
    }

    @Override
    public boolean isExportingItemNamesEnabled() {
        return this.exportItemNamesEnabled;
    }

    @Override
    public boolean isCacheEnabled() {
        return this.cacheEnabled;
    }

    @Override
    public boolean isForceRefreshEnabled() {
        return this.forceRefreshEnabled;
    }

    @Override
    public boolean isAdditionalInformationEnabled() {
        return this.additionalInformationEnabled;
    }

    @Override
    public String getCache() {
        return this.cache;
    }

    @Override
    public void setCache(@Nonnull final String digest) {
        final Property digestProperty = this.config.get(CACHE_CATEGORY, DIGEST_KEY, DIGEST_DEFAULT);
        digestProperty.set(digest);

        this.config.save();
    }

    @Override
    public void save() {
        this.config.save();
    }
}
