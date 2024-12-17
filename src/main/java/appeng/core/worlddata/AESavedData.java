package appeng.core.worlddata;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Base class for all AE2 saved data to make them more resistant to crashes while writing. Thank you RS for the idea!
 */
public abstract class AESavedData extends SavedData {
    private static final Logger LOG = LoggerFactory.getLogger(AESavedData.class);

    @Override
    public void save(File file, HolderLookup.Provider registries) {
        if (!this.isDirty()) {
            return;
        }

        var targetPath = file.toPath().toAbsolutePath();
        var tempFile = targetPath.getParent().resolve(file.getName() + ".temp");

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", this.save(new CompoundTag(), registries));
        NbtUtils.addCurrentDataVersion(compoundTag);
        try {
            // Write to temp file first.
            NbtIo.writeCompressed(compoundTag, tempFile);
            // Try atomic move
            try {
                Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException iOException) {
            LOG.error("Could not save data {}", this, iOException);
        }
        this.setDirty(false);
    }
}
