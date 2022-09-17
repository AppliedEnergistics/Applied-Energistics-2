/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.core;


import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class FacadeConfig {

    private static final String CONFIG_VERSION = "1";
    private static final String CONFIG_COMMON_KEY = "common";
    private static final String CONFIG_COMMON_COMMENT = "Settings applied to all facades.\n\n" //
            + "By default full blocks with no tile entity and a model do not need whitelisting.\n"//
            + "This will only be read once during client startup.";
    private static final String CONFIG_COMMON_ALLOW_TILEENTITIES_KEY = "allowTileEntityFacades";
    private static final String CONFIG_COMMON_ALLOW_TILEENTITIES_COMMENT = "Unsupported: Allows whitelisting TileEntity as facades. Could work, have render issues, or corrupt your world. USE AT YOUR OWN RISK.";
    private static final String CONFIG_FACADES_KEY = "facades";
    private static final String CONFIG_FACADES_COMMENT = "A way to explicitly handle certain blocks as facades.\n\n" //
            + "Blocks can be added by their resource location under the following rules.\n" //
            + " - One category per domain like minecraft or appliedenergistics2\n" //
            + " - One key per id. E.g. glass in case of minecraft:glass\n" //
            + " - An integer value ranging from 0 to 16 representing the metadata 0-15 and 16 as wildcard for all" //
            + " - Multiple entries for the same id but different metadata are possible when needed";

    private static FacadeConfig instance;

    private final boolean allowTileEntityFacades;
    private final Object2IntMap<ResourceLocation> whiteList;

    private FacadeConfig(boolean allowTileEntityFacades, Object2IntMap<ResourceLocation> whiteList) {
        this.allowTileEntityFacades = allowTileEntityFacades;
        this.whiteList = whiteList;
    }

    /**
     * Creates a custom confuration based on a {@link Configuration}, but ultimately throws it away after reading it
     * once to save a couple MB of memory.
     *
     * @param configFile
     */
    public static void init(final File configFile) {
        final Configuration configurartion = migrate(new Configuration(configFile, CONFIG_VERSION));

        final boolean allowTileEntityFacades = configurartion
                .get(CONFIG_COMMON_KEY, CONFIG_COMMON_ALLOW_TILEENTITIES_KEY, false, CONFIG_COMMON_ALLOW_TILEENTITIES_COMMENT)
                .setRequiresMcRestart(true)
                .setShowInGui(false)
                .getBoolean();

        final Object2IntMap<ResourceLocation> configWhiteList = new Object2IntArrayMap<>();

        final Set<ConfigCategory> whitelist = configurartion.getCategory(CONFIG_FACADES_KEY).getChildren();
        for (ConfigCategory configCategory : whitelist) {
            final String domain = configCategory.getName();
            final Map<String, Property> values = configCategory.getValues();

            for (Entry<String, Property> entry : values.entrySet()) {
                configWhiteList.put(new ResourceLocation(domain, entry.getKey()), entry.getValue().getInt());
            }
        }

        if (configurartion.hasChanged()) {
            configurartion.save();
        }

        instance = new FacadeConfig(allowTileEntityFacades, configWhiteList);
    }

    private static Configuration migrate(Configuration configurartion) {
        // Clear pre rv6 configs.
        if (configurartion.getLoadedConfigVersion() == null) {
            for (String category : configurartion.getCategoryNames()) {
                final ConfigCategory c = configurartion.getCategory(category);
                configurartion.removeCategory(c);
            }
        }

        // Create general category, if missing
        if (!configurartion.hasCategory(CONFIG_COMMON_KEY)) {
            configurartion.getCategory(CONFIG_COMMON_KEY).setComment(CONFIG_COMMON_COMMENT);
        }

        // Create whitelist, if missing
        if (!configurartion.hasCategory(CONFIG_FACADES_KEY)) {
            final ConfigCategory category = configurartion.getCategory(CONFIG_FACADES_KEY);
            category.setComment(CONFIG_FACADES_COMMENT);

            // Whitelist some vanilla blocks like glass
            final ConfigCategory minecraft = new ConfigCategory("minecraft", category);
            minecraft.put("glass", new Property("glass", "16", Type.INTEGER));
            minecraft.put("stained_glass", new Property("stained_glass", "16", Type.INTEGER));

            // Whitelist some AE2 blocks like quartz glass
            final ConfigCategory appliedenergistics = new ConfigCategory("appliedenergistics2", category);
            appliedenergistics.put("quartz_glass", new Property("quartz_glass", "16", Type.INTEGER));
            appliedenergistics.put("quartz_vibrant_glass", new Property("quartz_vibrant_glass", "16", Type.INTEGER));
        }

        return configurartion;
    }

    public static FacadeConfig instance() {
        return instance;
    }

    public boolean allowTileEntityFacades() {
        return this.allowTileEntityFacades;
    }

    public boolean isWhiteListed(final Block block, final int metadata) {
        final Integer entry = this.whiteList.get(block.getRegistryName());

        if (entry != null) {
            return entry == metadata || entry == 16;
        }

        return false;
    }
}
