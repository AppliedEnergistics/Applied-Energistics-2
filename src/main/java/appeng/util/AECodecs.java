package appeng.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEItems;

public final class AECodecs {
    private static final Logger LOG = LoggerFactory.getLogger(AECodecs.class);

    private AECodecs() {
    }

    public static <B extends ByteBuf, V> StreamCodec<B, V> nullable(StreamCodec<B, V> codec) {
        return new StreamCodec<>() {
            @Nullable
            public V decode(B buffer) {
                return buffer.readBoolean() ? codec.decode(buffer) : null;
            }

            public void encode(B buffer, @Nullable V value) {
                if (value != null) {
                    buffer.writeBoolean(true);
                    codec.encode(buffer, value);
                } else {
                    buffer.writeBoolean(false);
                }
            }
        };
    }

    private static final Codec.ResultFunction<ItemStack> MISSING_CONTENT_ITEMSTACK_RESULT = new Codec.ResultFunction<>() {
        @Override
        public <T> DataResult<Pair<ItemStack, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<ItemStack, T>> a) {
            if (a instanceof DataResult.Error<Pair<ItemStack, T>> error) {
                var missingContent = AEItems.MISSING_CONTENT.stack();
                var convert = Dynamic.convert(ops, NbtOps.INSTANCE, input);
                if (convert instanceof CompoundTag compoundTag) {
                    missingContent.set(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA, CustomData.of(compoundTag));
                }
                LOG.error("Failed to deserialize ItemStack: {}", error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, error.message());

                return DataResult.success(
                        Pair.of(missingContent, input),
                        Lifecycle.stable());
            }

            // Return unchanged if deserialization succeeded
            return a;
        }

        @Override
        public <T> DataResult<T> coApply(DynamicOps<T> ops, ItemStack input, DataResult<T> t) {
            // When the serialization result failed, we write a missing content item instead
            // this one will NOT be recoverable
            if (t instanceof DataResult.Error<T> error) {
                var missingContent = AEItems.MISSING_CONTENT.stack();
                LOG.error("Failed to serialize ItemStack {}: {}", input, error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, error.message());

                return ItemStack.SINGLE_ITEM_CODEC.encodeStart(ops, missingContent).setLifecycle(t.lifecycle());
            }

            // When the input is a MISSING_CONTENT item and has the original data attached,
            // we write that back.
            if (AEItems.MISSING_CONTENT.is(input)) {
                var originalData = input.get(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA);
                if (originalData != null) {
                    return DataResult.success(Dynamic.convert(NbtOps.INSTANCE, ops, originalData.copyTag()),
                            t.lifecycle());
                }
            }

            return t;
        }
    };

    public static final Codec<ItemStack> FAULT_TOLERANT_SIMPLE_ITEM_CODEC = ItemStack.SINGLE_ITEM_CODEC
            .mapResult(MISSING_CONTENT_ITEMSTACK_RESULT);

    public static final Codec<ItemStack> FAULT_TOLERANT_ITEMSTACK_CODEC = ItemStack.CODEC
            .mapResult(MISSING_CONTENT_ITEMSTACK_RESULT);

    public static final Codec<ItemStack> FAULT_TOLERANT_OPTIONAL_ITEMSTACK_CODEC = ItemStack.OPTIONAL_CODEC
            .mapResult(MISSING_CONTENT_ITEMSTACK_RESULT);
}
