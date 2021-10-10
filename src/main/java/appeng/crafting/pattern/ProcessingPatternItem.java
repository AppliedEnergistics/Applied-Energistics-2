package appeng.crafting.pattern;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;

/**
 * An item that contains an encoded {@link AEProcessingPattern}.
 */
public class ProcessingPatternItem extends EncodedPatternItem {
    public ProcessingPatternItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public AEProcessingPattern decode(ItemStack stack, Level level, boolean tryRecovery) {
        if (stack.getItem() != this || !stack.hasTag()) {
            return null;
        }

        return decode(stack.getOrCreateTag(), level, tryRecovery);
    }

    @Override
    public AEProcessingPattern decode(CompoundTag tag, Level level, boolean tryRecovery) {
        try {
            return new AEProcessingPattern(tag.copy());
        } catch (IllegalStateException e) {
            AELog.warn("Could not decode an invalid crafting pattern %s: %s", tag, e);
            return null;
        }
    }

    public ItemStack encode(IAEStack[] sparseInputs, IAEStack[] sparseOutputs) {
        Preconditions.checkNotNull(sparseInputs[0]);
        checkItemsOrFluids(sparseInputs);
        checkItemsOrFluids(sparseOutputs);

        var stack = new ItemStack(this);
        AEPatternHelper.encodeProcessingPattern(stack.getOrCreateTag(), sparseInputs, sparseOutputs);
        return stack;
    }

    private static void checkItemsOrFluids(IAEStack[] stacks) {
        for (var stack : stacks) {
            if (stack != null) {
                if (stack.getChannel() != StorageChannels.items()
                        && stack.getChannel() != StorageChannels.fluids()) {
                    throw new IllegalArgumentException("Unsupported storage channel: " + stack.getChannel());
                }
            }
        }
    }
}
