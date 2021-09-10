package appeng.api.crafting;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.networking.crafting.IPatternDetails;

/**
 * Allows mod to decode their {@link IPatternDetails} from their item stacks. This is required for custom patterns,
 * otherwise the crafting CPU can't properly persist them. Register a single instance to {@link ICraftingHelper}.
 */
public interface IPatternDetailsDecoder {
    boolean isEncodedPattern(ItemStack stack);

    @Nullable
    IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery);
}
