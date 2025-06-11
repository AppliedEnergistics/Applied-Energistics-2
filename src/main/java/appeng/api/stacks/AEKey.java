package appeng.api.stacks;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.util.HashHelper;
import appeng.api.util.IHashCode64;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;

/**
 * Uniquely identifies something that "stacks" within an ME inventory.
 * <p/>
 * For example for items, this is the combination of an {@link net.minecraft.world.item.Item} and optional
 * {@link CompoundTag}. To account for common indexing scenarios, a key is (optionally) split into a primary and
 * secondary component, which serves two purposes:
 * <ul>
 * <li>Fuzzy cards allow setting filters for the primary component of a key, i.e. for an
 * {@link net.minecraft.world.item.Item}, while disregarding the compound tag.</li>
 * <li>When indexing resources, it is usually assumed that indexing by the primary key alone offers a good trade-off
 * between memory usage and lookup speed.</li>
 * </ul>
 */
public abstract class AEKey implements IHashCode64 {
    public static final String TYPE_FIELD = "#t";
    private static final Logger LOG = LoggerFactory.getLogger(AEKey.class);
    public static final MapCodec<AEKey> MAP_CODEC = AEKeyType.CODEC
            .<AEKey>dispatchMap(TYPE_FIELD, AEKey::getType, AEKeyType::codec)
            .mapResult(new MapCodec.ResultFunction<>() {
                @Override
                public <T> DataResult<AEKey> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<AEKey> a) {
                    if (a instanceof DataResult.Error<AEKey> error) {
                        var missingContent = AEItems.MISSING_CONTENT.stack();
                        var convert = ops.convertMap(NbtOps.INSTANCE, ops.createMap(input.entries()));
                        if (convert instanceof CompoundTag compoundTag) {
                            missingContent.set(AEComponents.MISSING_CONTENT_AEKEY_DATA, CustomData.of(compoundTag));
                        }
                        LOG.error("Failed to deserialize AE key: {}", error.message());
                        missingContent.set(AEComponents.MISSING_CONTENT_ERROR, error.message());

                        return DataResult.success(
                                AEItemKey.of(missingContent),
                                Lifecycle.stable());
                    }

                    return a; // Return unchanged if deserialization succeeded
                }

                @Override
                public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, AEKey input, RecordBuilder<T> t) {
                    // When the input is a MISSING_CONTENT item and has the original data attached,
                    // we write that back.
                    if (AEItems.MISSING_CONTENT.is(input)) {
                        var originalData = input.get(AEComponents.MISSING_CONTENT_AEKEY_DATA);
                        if (originalData != null) {
                            var originalDataMap = originalData.getUnsafe();
                            for (var key : originalDataMap.getAllKeys()) {
                                t.add(key, NbtOps.INSTANCE.convertTo(ops, originalDataMap.get(key)));
                            }
                        }
                    }

                    return t;
                }
            });

    public static final Codec<AEKey> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, AEKey> STREAM_CODEC = StreamCodec.of(
            AEKey::writeKey,
            AEKey::readKey);

    public static final StreamCodec<RegistryFriendlyByteBuf, AEKey> OPTIONAL_STREAM_CODEC = StreamCodec.of(
            AEKey::writeOptionalKey,
            AEKey::readOptionalKey);

    /**
     * Writes a generic, nullable key to the given buffer.
     */
    public static void writeOptionalKey(RegistryFriendlyByteBuf buffer, @Nullable AEKey key) {
        buffer.writeBoolean(key != null);
        if (key != null) {
            writeKey(buffer, key);
        }
    }

    public static void writeKey(RegistryFriendlyByteBuf buffer, AEKey key) {
        var id = key.getType().getRawId();
        buffer.writeVarInt(id);
        key.writeToPacket(buffer);
    }

    /**
     * Tries reading a key written using {@link #writeOptionalKey}.
     */
    @Nullable
    public static AEKey readOptionalKey(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        return readKey(buffer);
    }

    @Nullable
    public static AEKey readKey(RegistryFriendlyByteBuf buffer) {
        var id = buffer.readVarInt();
        var type = AEKeyType.fromRawId(id);
        if (type == null) {
            AELog.error("Received unknown key space id %d", id);
            return null;
        }
        return type.readFromPacket(buffer);
    }

    /**
     * The display name, which is used to sort by name in client terminal. Lazily initialized to avoid unnecessary work
     * on the server. Volatile ensures that this cache is thread-safe (but can be initialized multiple times).
     */
    private volatile Component cachedDisplayName;

    @Nullable
    public static AEKey fromTagGeneric(HolderLookup.Provider registries, CompoundTag tag) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        try {
            return CODEC.decode(ops, tag).getOrThrow().getFirst();
        } catch (Exception e) {
            LOG.warn("Cannot deserialize generic key from {}: {}", tag, e);
            return null;
        }
    }

    /**
     * Same as {@link #toTag(HolderLookup.Provider)}, but includes type information so that
     * {@link #fromTagGeneric(HolderLookup.Provider, CompoundTag)} can restore this particular type of key withot
     * knowing the actual type beforehand.
     */
    public final CompoundTag toTagGeneric(HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return (CompoundTag) CODEC.encodeStart(ops, this)
                .getOrThrow();
    }

    /**
     * How much of this key is in one unit (i.e. one bucket). This is used for display purposes where the technical
     * amount is not user-readable (i.e. a bucket of fluid has amount 1000 on Forge and 81000 on Fabric, but we want to
     * show it as 1 bucket, hence this method would return 1000 on Forge and 81000 on Fabric for AEFluidKey).
     */
    public final int getAmountPerUnit() {
        return getType().getAmountPerUnit();
    }

    @Nullable
    public final String getUnitSymbol() {
        return getType().getUnitSymbol();
    }

    /**
     * @see AEKeyType#getAmountPerOperation()
     */
    public final int getAmountPerOperation() {
        return getType().getAmountPerOperation();
    }

    /**
     * @see AEKeyType#getAmountPerByte()
     */
    public final int getAmountPerByte() {
        return getType().getAmountPerByte();
    }

    /**
     * @see AEKeyType#formatAmount(long, AmountFormat)
     */
    public String formatAmount(long amount, AmountFormat format) {
        return getType().formatAmount(amount, format);
    }

    /**
     * @return An object giving additional properties about the type of key.
     */
    public abstract AEKeyType getType();

    /**
     * @return This object if it has no secondary component, otherwise a copy of this resource key with the secondary
     *         component removed.
     */
    public abstract AEKey dropSecondary();

    /**
     * Serialized keys MUST NOT contain keys that start with <code>#</code>, because this prefix can be used to add
     * additional data into the same tag as the key.
     */
    public abstract CompoundTag toTag(HolderLookup.Provider registries);

    public abstract Object getPrimaryKey();

    /**
     * @return If {@link #getFuzzySearchMaxValue()} is greater than 0, this is the value in the range of
     *         [0,getFuzzyModeMaxValue] used to index keys by. Used by fuzzy mode search with percentage ranges.
     */
    public int getFuzzySearchValue() {
        return 0;
    }

    /**
     * @return The upper bound for values returned by {@link #getFuzzySearchValue()}. If it is equal to 0, no fuzzy
     *         range-search is possible for this type of key.
     */
    public int getFuzzySearchMaxValue() {
        return 0;
    }

    /**
     * Tests if this and the given AE key are in the same fuzzy partition given a specific fuzzy matching mode.
     */
    public final boolean fuzzyEquals(AEKey other, FuzzyMode fuzzyMode) {
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        // For any fuzzy mode, the primary key (item, fluid) must still match
        if (getPrimaryKey() != other.getPrimaryKey()) {
            return false;
        }

        // If the type doesn't support fuzzy range search, it always behaves like IGNORE_ALL, which just ignores NBT
        if (!supportsFuzzyRangeSearch()) {
            return true;
        } else if (fuzzyMode == FuzzyMode.IGNORE_ALL) {
            return true;
        } else if (fuzzyMode == FuzzyMode.PERCENT_99) {
            return getFuzzySearchValue() > 0 == other.getFuzzySearchValue() > 0;
        } else {
            final float percentA = (float) getFuzzySearchValue() / getFuzzySearchMaxValue();
            final float percentB = (float) other.getFuzzySearchValue() / other.getFuzzySearchMaxValue();

            return percentA > fuzzyMode.breakPoint == percentB > fuzzyMode.breakPoint;
        }
    }

    /**
     * Checks if the given stack has the same key as this.
     *
     * @return False if stack is null, otherwise true iff the stacks key is equal to this.
     */
    @Contract("null -> false")
    public final boolean matches(@Nullable GenericStack stack) {
        return stack != null && stack.what().equals(this);
    }

    /**
     * @return The ID of the mod this resource belongs to.
     */
    public String getModId() {
        return getId().getNamespace();
    }

    /**
     * @return The ID of the resource identified by this key.
     */
    public abstract ResourceLocation getId();

    public abstract void writeToPacket(RegistryFriendlyByteBuf data);

    /**
     * Wraps a key in an ItemStack that can be unwrapped into a key later.
     */
    public ItemStack wrapForDisplayOrFilter() {
        return GenericStack.wrapInItemStack(this, 0);
    }

    /**
     * True to indicate that this type of {@link AEKey} supports range-based fuzzy search using
     * {@link AEKey#getFuzzySearchValue()} and {@link AEKey#getFuzzySearchMaxValue()}.
     * <p/>
     * For items this is used for damage-based search and filtering.
     */
    public final boolean supportsFuzzyRangeSearch() {
        return getType().supportsFuzzyRangeSearch();
    }

    public final Component getDisplayName() {
        var ret = cachedDisplayName;

        if (ret == null) {
            cachedDisplayName = ret = computeDisplayName();
        }

        return ret;
    }

    /**
     * Compute the display name, which is used to sort by name in client terminal. Will be cached by
     * {@link #getDisplayName()}.
     */
    protected abstract Component computeDisplayName();

    /**
     * Adds the drops if the container holding this key is broken, such as an interface holding stacks. Item stacks
     * should be placed in the list and not spawned directly into the world
     *
     * @param amount Amount to drop
     * @param drops  Drop list to append to, in case of {@link ItemStack} drops
     * @param level  World where the stacks were being held
     * @param pos    Position where the stacks were being held
     */
    public abstract void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos);

    /**
     * If the underlying resource supports tagging, this method checks if the resource represented by this key is tagged
     * by the given tag.
     */
    public boolean isTagged(TagKey<?> tag) {
        return false;
    }

    /**
     * Get a data component attached to this key. It might be null.
     */
    @Nullable
    public <T> T get(DataComponentType<T> type) {
        return null;
    }

    /**
     * @return true if this key has *any* components attached.
     */
    public abstract boolean hasComponents();

    public long hashCode64() {
        return HashHelper.calculateTypedHash(getType().hashCode(), hashCode());
    }
}
