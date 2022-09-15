package appeng.crafting.pattern;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
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
        return decode(AEItemKey.of(stack), level);
    }

    @Override
    public AEProcessingPattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return new AEProcessingPattern(what);
        } catch (Exception e) {
            AELog.warn("Could not decode an invalid processing pattern %s: %s", what.getTag(), e);
            return null;
        }
    }

    public ItemStack encode(GenericStack[] sparseInputs, GenericStack[] sparseOutputs) {
        if (Arrays.stream(sparseInputs).noneMatch(Objects::nonNull)) {
            throw new IllegalArgumentException("At least one input must be non-null.");
        }
        Objects.requireNonNull(sparseOutputs[0],
                "The first (primary) output must be non-null.");

        var stack = new ItemStack(this);
        ProcessingPatternEncoding.encodeProcessingPattern(stack.getOrCreateTag(), sparseInputs, sparseOutputs);
        return stack;
    }
}
