package appeng.hooks;

import com.mojang.serialization.Dynamic;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

import appeng.core.AELog;

/**
 * This fixes an issue with DFU that essentially trips over the spatial storage dimension if it happens to be the first
 * dimension it sees in the iteration order of the dimension compound tag, and a version update (i.e. 1.18 -> 1.18.1)
 * happens to be applied.
 * <p/>
 * It does this by removing the AE2 dimension from the level.dat NBT before it is processed by DFU, and re-adding it
 * before the actual level data is deserialized. This will also fix cases where the data is missing from level.dat
 * entirely.
 */
public final class FixupDimensionHook {
    private FixupDimensionHook() {
    }

    public static <T> void removeDimension(Dynamic<T> nbt) {
        if (!(nbt.getValue() instanceof CompoundNBT)) {
            AELog.warn("Failed to fixup spatial dimension: Not loading from NBT");
            return;
        }

        CompoundNBT compoundTag = (CompoundNBT) nbt.getValue();

        if (!compoundTag.contains("WorldGenSettings", Constants.NBT.TAG_COMPOUND)) {
            AELog.warn("Failed to fixup spatial dimension: Missing WorldGenSettings");
            return;
        }

        CompoundNBT worldGenSettings = compoundTag.getCompound("WorldGenSettings");
        if (!worldGenSettings.contains("dimensions", Constants.NBT.TAG_COMPOUND)) {
            AELog.warn("Failed to fixup spatial dimension: Missing WorldGenSettings.dimensions");
            return;
        }

        CompoundNBT dimensions = worldGenSettings.getCompound("dimensions");
        if (dimensions.contains("appliedenergistics2:spatial_storage")) {
            dimensions.remove("appliedenergistics2:spatial_storage");
            AELog.debug("Removed AE2 spatial storage before DFU can 'fix' it");
        } else {
            AELog.warn("AE2 spatial storage dimension missing. It will be re-added.");
        }
    }

    public static <T> void addDimension(Dynamic<T> nbt) {
        // NOTE this method gets the WorldGenSettings passed directly, not the top-level tag
        if (!(nbt.getValue() instanceof CompoundNBT)) {
            AELog.warn("Failed to re-add spatial dimension: Not loading from NBT");
            return;
        }
        CompoundNBT worldGenSettings = (CompoundNBT) nbt.getValue();

        if (!worldGenSettings.contains("dimensions", Constants.NBT.TAG_COMPOUND)) {
            AELog.warn("Failed to re-add spatial dimension: Missing dimensions key");
            return;
        }

        CompoundNBT dimensions = worldGenSettings.getCompound("dimensions");
        if (!dimensions.contains("appliedenergistics2:spatial_storage")) {
            AELog.debug("Re-adding spatial storage NBT to world generation settings");

            CompoundNBT spatialStorage = new CompoundNBT();
            spatialStorage.putString("type", "appliedenergistics2:spatial_storage");
            CompoundNBT generator = new CompoundNBT();
            generator.putString("type", "appliedenergistics2:spatial_storage");
            spatialStorage.put("generator", generator);
            dimensions.put("appliedenergistics2:spatial_storage", spatialStorage);
        }
    }
}
