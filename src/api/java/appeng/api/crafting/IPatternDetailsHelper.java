package appeng.api.crafting;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.networking.crafting.IPatternDetails;

public interface IPatternDetailsHelper {
    void registerDecoder(IPatternDetailsDecoder decoder);

    boolean isEncodedPattern(ItemStack stack);

    @Nullable
    default IPatternDetails decodePattern(ItemStack stack, Level level) {
        return decodePattern(stack, level, false);
    }

    @Nullable
    IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery);
}
