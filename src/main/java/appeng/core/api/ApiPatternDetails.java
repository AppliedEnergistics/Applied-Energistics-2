package appeng.core.api;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.IPatternDetailsHelper;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternHelper;

public class ApiPatternDetails implements IPatternDetailsHelper {
    private final List<IPatternDetailsDecoder> decoders = new CopyOnWriteArrayList<>();

    public ApiPatternDetails() {
        // Register support for our own stacks.
        registerDecoder(appeng.crafting.pattern.AEPatternDecoder.INSTANCE);
    }

    @Override
    public void registerDecoder(IPatternDetailsDecoder decoder) {
        Objects.requireNonNull(decoder);
        decoders.add(decoder);
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        for (var decoder : decoders) {
            if (decoder.isEncodedPattern(stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
        for (var decoder : decoders) {
            var decoded = decoder.decodePattern(stack, level, autoRecovery);
            if (decoded != null) {
                return decoded;
            }
        }
        return null;
    }

    @Override
    public ItemStack encodeAE2CraftingPattern(@Nullable ItemStack stack, CraftingRecipe recipe, ItemStack[] in,
            ItemStack out, boolean allowSubstitutes) {
        if (stack == null) {
            stack = AEItems.ENCODED_PATTERN.stack();
        } else {
            Preconditions.checkArgument(isEncodedPattern(stack));
        }

        AEPatternHelper.encodeCraftingPattern(stack, recipe, in, out, allowSubstitutes);
        return stack;
    }

    @Override
    public ItemStack encodeAE2ProcessingPattern(@Nullable ItemStack stack, IAEStack[] in, IAEStack[] out) {
        checkItemsOrFluids(in);
        checkItemsOrFluids(out);
        if (stack == null) {
            stack = AEItems.ENCODED_PATTERN.stack();
        } else {
            Preconditions.checkArgument(isEncodedPattern(stack));
        }

        AEPatternHelper.encodeProcessingPattern(stack, in, out);
        return stack;
    }

    private static void checkItemsOrFluids(IAEStack[] stacks) {
        for (var stack : stacks) {
            if (stack != null) {
                if (stack.getChannel() != StorageChannels.items() || stack.getChannel() != StorageChannels.fluids()) {
                    throw new IllegalArgumentException("Unsupported storage channel: " + stack.getChannel());
                }
            }
        }
    }
}
