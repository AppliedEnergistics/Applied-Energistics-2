package appeng.api.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface EncodedPatternRecovery {
    boolean attemptRecovery(CompoundTag tag, Level level, @Nullable Exception cause);
}
