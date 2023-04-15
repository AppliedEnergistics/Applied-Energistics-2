package appeng.core.worlddata;

import java.io.File;
import java.io.IOException;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Base class for all AE2 saved data to make them more resistant to crashes while writing. Thank you RS for the idea!
 */
public abstract class AESavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void save(File file) {
        if (!this.isDirty()) {
            return;
        }

        File tempFile = file.toPath().getParent().resolve(file.getName() + ".temp").toFile();

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", this.save(new CompoundTag()));
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        try {
            // Write to temp file first.
            NbtIo.writeCompressed(compoundTag, tempFile);
            // Delete old file.
            if (file.exists()) {
                if (!file.delete()) {
                    LOGGER.error("Could not delete old file {}", file);
                }
            }
            // Rename temp file to the correct name.
            if (!tempFile.renameTo(file)) {
                LOGGER.error("Could not rename file {} to {}", tempFile, file);
            }
        } catch (IOException iOException) {
            LOGGER.error("Could not save data {}", this, iOException);
        }
        this.setDirty(false);
    }
}
