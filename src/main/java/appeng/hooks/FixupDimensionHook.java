package appeng.hooks;

import com.mojang.serialization.Dynamic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

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
        if (!(nbt.getValue() instanceof CompoundTag compoundTag)) {
            AELog.warn("Failed to fixup spatial dimension: Not loading from NBT");
            return;
        }

        if (!compoundTag.contains("WorldGenSettings", Tag.TAG_COMPOUND)) {
            AELog.warn("Failed to fixup spatial dimension: Missing WorldGenSettings");
            return;
        }

        var worldGenSettings = compoundTag.getCompound("WorldGenSettings");
        if (!worldGenSettings.contains("dimensions", Tag.TAG_COMPOUND)) {
            AELog.warn("Failed to fixup spatial dimension: Missing WorldGenSettings.dimensions");
            return;
        }

        var dimensions = worldGenSettings.getCompound("dimensions");
        if (dimensions.contains("ae2:spatial_storage")) {
            dimensions.remove("ae2:spatial_storage");
            AELog.debug("Removed AE2 spatial storage before DFU can 'fix' it");
        } else {
            AELog.warn("AE2 spatial storage dimension missing. It will be re-added.");
        }
    }

    public static <T> void addDimension(Dynamic<T> nbt) {
        // NOTE this method gets the WorldGenSettings passed directly, not the top-level tag
        if (!(nbt.getValue() instanceof CompoundTag worldGenSettings)) {
            AELog.warn("Failed to re-add spatial dimension: Not loading from NBT");
            return;
        }

        if (!worldGenSettings.contains("dimensions", Tag.TAG_COMPOUND)) {
            AELog.warn("Failed to re-add spatial dimension: Missing dimensions key");
            return;
        }

        var dimensions = worldGenSettings.getCompound("dimensions");
        if (!dimensions.contains("ae2:spatial_storage")) {
            AELog.debug("Re-adding spatial storage NBT to world generation settings");

            var spatialStorage = new CompoundTag();
            spatialStorage.putString("type", "ae2:spatial_storage");
            var generator = new CompoundTag();
            generator.putString("type", "ae2:spatial_storage");
            spatialStorage.put("generator", generator);
            dimensions.put("ae2:spatial_storage", spatialStorage);
        }
    }
}
