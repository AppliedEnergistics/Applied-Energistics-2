package appeng.api.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * This stragegy is used when a player views the tooltip of an encoded pattern that failed to decode properly. It should
 * attempt to extract as much information as possible from the given tag to help the player identify which recipe the
 * pattern contained originally.
 *
 * @see EncodedPatternItemBuilder#invalidPatternTooltip(InvalidPatternTooltipStrategy)
 */
@FunctionalInterface
public interface InvalidPatternTooltipStrategy {
    /**
     * @param tag   The NBT tag of the encoded pattern.
     * @param cause The error thrown by the decoder - if any.
     */
    PatternDetailsTooltip getTooltip(CompoundTag tag, Level level, @Nullable Exception cause, TooltipFlag flags);
}
