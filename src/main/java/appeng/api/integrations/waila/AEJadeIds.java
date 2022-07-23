package appeng.api.integrations.waila;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.IJadeProvider;

import appeng.core.AppEng;

/**
 * {@link IJadeProvider#getUid() IDs} of AE2s data providers.
 */
public final class AEJadeIds {
    private AEJadeIds() {
    }

    public static final ResourceLocation BLOCK_ENTITIES_PROVIDER = AppEng.makeId("provider");
    public static final ResourceLocation DEBUG_PROVIDER = AppEng.makeId("debug");
    public static final ResourceLocation GRID_NODE_STATE_PROVIDER = AppEng.makeId("grid_node_state");
    public static final ResourceLocation POWER_STORAGE_PROVIDER = AppEng.makeId("power_storage");
    public static final ResourceLocation CRAFTING_MONITOR_PROVIDER = AppEng.makeId("crafting_monitor");
    public static final ResourceLocation CHARGER_PROVIDER = AppEng.makeId("charger");
    public static final ResourceLocation PART_PROVIDER = AppEng.makeId("part");
    public static final ResourceLocation PART_NAME_PROVIDER = AppEng.makeId("part_name");
    public static final ResourceLocation PART_ICON_PROVIDER = AppEng.makeId("part_icon");
    public static final ResourceLocation PART_DATA_PROVIDER = AppEng.makeId("part_data");
}
