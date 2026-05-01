package appeng.api.stacks;

import java.util.List;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEItems;
import appeng.items.misc.WrappedGenericStack;

/**
 * Represents some amount of some generic resource that AE can store or handle in crafting.
 */
public record GenericStack(AEKey what, long amount) {

    @ApiStatus.Internal
    public static final String AMOUNT_FIELD = "#";

    private static final Logger LOG = LoggerFactory.getLogger(GenericStack.class);

    public static final MapCodec<GenericStack> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AEKey.MAP_CODEC.forGetter(GenericStack::what),
            Codec.LONG.fieldOf(AMOUNT_FIELD).forGetter(GenericStack::amount)).apply(builder, GenericStack::new));

    public static final Codec<GenericStack> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStack> STREAM_CODEC = StreamCodec.ofMember(
            GenericStack::writeBuffer,
            GenericStack::readBuffer);

    /**
     * This result function converts failed serialization results for GenericStack into missing content storing the
     * error message.
     */
    private static final Codec.ResultFunction<GenericStack> MISSING_CONTENT_GENERICSTACK_RESULT = new Codec.ResultFunction<>() {
        @Override
        public <T> DataResult<Pair<GenericStack, T>> apply(DynamicOps<T> ops, T input,
                DataResult<Pair<GenericStack, T>> a) {
            if (a instanceof DataResult.Error<Pair<GenericStack, T>> error) {
                var missingContent = AEItems.MISSING_CONTENT.stack();
                var convert = Dynamic.convert(ops, NbtOps.INSTANCE, input);
                if (convert instanceof CompoundTag compoundTag) {
                    missingContent.set(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA, CustomData.of(compoundTag));
                }
                LOG.error("Failed to deserialize GenericStack {}: {}", input, error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, error.message());

                var replacement = new GenericStack(AEItemKey.of(missingContent), 1);

                return DataResult.success(
                        Pair.of(replacement, input),
                        Lifecycle.stable());
            }

            // Return unchanged if deserialization succeeded
            return a;
        }

        @Override
        public <T> DataResult<T> coApply(DynamicOps<T> ops, GenericStack input, DataResult<T> t) {
            // When the serialization result failed, we write a missing content item instead
            // this one will NOT be recoverable
            if (t instanceof DataResult.Error<T> error) {
                var missingContent = AEItems.MISSING_CONTENT.stack();
                LOG.error("Failed to serialize GenericStack {}: {}", input, error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, error.message());

                var replacement = new GenericStack(AEItemKey.of(missingContent), 1);
                return CODEC.encodeStart(ops, replacement).setLifecycle(t.lifecycle());
            }

            // When the input is a MISSING_CONTENT item and has the original data attached,
            // we write that back.
            if (input.what() instanceof AEItemKey itemKey && itemKey.is(AEItems.MISSING_CONTENT)) {
                var originalData = itemKey.get(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA);
                if (originalData != null) {
                    return DataResult.success(Dynamic.convert(NbtOps.INSTANCE, ops, originalData.copyTag()),
                            t.lifecycle());
                }
            }

            return t;
        }
    };

    public static final Codec<List<@Nullable GenericStack>> FAULT_TOLERANT_NULLABLE_LIST_CODEC = new GenericStackListCodec(
            CODEC.mapResult(MISSING_CONTENT_GENERICSTACK_RESULT));

    public static final Codec<List<GenericStack>> FAULT_TOLERANT_LIST_CODEC = CODEC
            .mapResult(MISSING_CONTENT_GENERICSTACK_RESULT).listOf();

    public GenericStack {
        Objects.requireNonNull(what, "what");
    }

    @Nullable
    public static GenericStack readBuffer(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }

        var what = AEKey.readKey(buffer);
        if (what == null) {
            return null;
        }

        return new GenericStack(what, buffer.readVarLong());
    }

    public static void writeBuffer(@Nullable GenericStack stack, RegistryFriendlyByteBuf buffer) {
        if (stack == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);

            AEKey.writeKey(buffer, stack.what);
            buffer.writeVarLong(stack.amount);
        }
    }

    @Nullable
    public static GenericStack readTag(ValueInput input) {
        if (input.getString(AEKey.TYPE_FIELD).isEmpty()) {
            return null;
        }
        return input.read(MAP_CODEC).orElse(null);
    }

    public static void writeTag(ValueOutput output, @Nullable GenericStack stack) {
        if (stack != null) {
            output.store(MAP_CODEC, stack);
        }
    }

    /**
     * Converts a given item stack into a generic stack, accounting for a {@link GenericStack} already wrapped in an
     * {@link ItemStack}, unwrapping it automatically. If the item stack is empty, null is returned.
     */
    @Nullable
    public static GenericStack fromItemStack(ItemStack stack) {
        var genericStack = GenericStack.unwrapItemStack(stack);
        if (genericStack != null) {
            return genericStack;
        }

        var key = AEItemKey.of(stack);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, stack.getCount());
    }

    /**
     * Converts a given fluid stack into a generic stack. If the fluid stack is empty, null is returned.
     */
    @Nullable
    public static GenericStack fromFluidStack(FluidStack stack) {
        var key = AEFluidKey.of(stack);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, stack.getAmount());
    }

    /**
     * Converts a given item resource and amount into a generic stack. If the resource is empty, null is returned.
     */
    @Nullable
    public static GenericStack from(ItemResource resource, long amount) {
        var key = AEItemKey.of(resource);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, amount);
    }

    /**
     * Converts a given fluid resource and amount into a generic stack. If the resource is empty, null is returned.
     */
    @Nullable
    public static GenericStack from(FluidResource resource, long amount) {
        var key = AEFluidKey.of(resource);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, amount);
    }

    public static long getStackSizeOrZero(@Nullable GenericStack stack) {
        return stack == null ? 0 : stack.amount;
    }

    public static ItemStack wrapInItemStack(@Nullable GenericStack stack) {
        if (stack != null) {
            return wrapInItemStack(stack.what(), stack.amount());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack wrapInItemStack(AEKey what, long amount) {
        return WrappedGenericStack.wrap(what, amount);
    }

    public static boolean isWrapped(ItemStack stack) {
        return stack.getItem() instanceof WrappedGenericStack;
    }

    public static GenericStack unwrapItemStack(ItemStack stack) {
        // The isEmpty stack is needed because the item can match while its count is 0
        if (!stack.isEmpty() && stack.getItem() instanceof WrappedGenericStack item) {
            var what = item.unwrapWhat(stack);
            if (what != null) {
                var amount = item.unwrapAmount(stack);
                return new GenericStack(what, amount);
            }
        }

        return null;
    }

    public static GenericStack sum(GenericStack left, GenericStack right) {
        if (!left.what.equals(right.what)) {
            throw new IllegalArgumentException("Cannot sum generic stacks of " + left.what + " and " + right.what);
        }
        return new GenericStack(left.what, left.amount + right.amount);
    }

}
