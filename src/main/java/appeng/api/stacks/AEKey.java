package appeng.api.stacks;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.config.FuzzyMode;
import appeng.core.AELog;

/**
 * Uniquely identifies something that "stacks" within an ME inventory.
 * <p/>
 * For example for items, this is the combination of an {@link net.minecraft.world.item.Item} and optional
 * {@link net.minecraft.nbt.CompoundTag}. To account for common indexing scenarios, a key is (optionally) split into a
 * primary and secondary component, which serves two purposes:
 * <ul>
 * <li>Fuzzy cards allow setting filters for the primary component of a key, i.e. for an
 * {@link net.minecraft.world.item.Item}, while disregarding the compound tag.</li>
 * <li>When indexing resources, it is usually assumed that indexing by the primary key alone offers a good trade-off
 * between memory usage and lookup speed.</li>
 * </ul>
 */
public abstract class AEKey {

    /**
     * The display name, which is used to sort by name in client terminal
     */
    private final Component displayName;

    /**
     * @deprecated It has been deprecated because we should cache the display name in the initialization to speed up the
     *             sorting process.
     */
    @Deprecated
    public AEKey() {
        this.displayName = null;
    }

    /**
     * @param displayName the display name, which is used to sort by name in client terminal
     */
    public AEKey(Component displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public static AEKey fromTagGeneric(CompoundTag tag) {
        // Handle malformed tags where the channel is missing
        var channelId = tag.getString("#c");
        if (channelId.isEmpty()) {
            AELog.warn("Cannot deserialize generic key from %s because key '#c' is missing.", tag);
            return null;
        }

        // Handle tags where the mod that provided the channel has been uninstalled
        AEKeyType channel;
        try {
            channel = AEKeyTypes.get(new ResourceLocation(channelId));
        } catch (IllegalArgumentException | ResourceLocationException e) {
            AELog.warn("Cannot deserialize generic key from %s because channel '%s' is missing.", tag, channelId);
            return null;
        }

        return channel.loadKeyFromTag(tag);
    }

    /**
     * Writes a generic, nullable key to the given buffer.
     */
    public static void writeOptionalKey(FriendlyByteBuf buffer, @Nullable AEKey key) {
        buffer.writeBoolean(key != null);
        if (key != null) {
            writeKey(buffer, key);
        }
    }

    public static void writeKey(FriendlyByteBuf buffer, AEKey key) {
        var id = key.getType().getRawId();
        buffer.writeVarInt(id);
        key.writeToPacket(buffer);
    }

    /**
     * Tries reading a key written using {@link #writeOptionalKey}.
     */
    @Nullable
    public static AEKey readOptionalKey(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        return readKey(buffer);
    }

    @Nullable
    public static AEKey readKey(FriendlyByteBuf buffer) {
        var id = buffer.readVarInt();
        var type = AEKeyType.fromRawId(id);
        if (type == null) {
            AELog.error("Received unknown key space id %d", id);
            return null;
        }
        return type.readFromPacket(buffer);
    }

    /**
     * Same as {@link #toTag()}, but includes type information so that {@link #fromTagGeneric(CompoundTag)} can restore
     * this particular type of key withot knowing the actual type beforehand.
     */
    public final CompoundTag toTagGeneric() {
        var tag = toTag();
        tag.putString("#c", getType().getId().toString());
        return tag;
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
     * @see AEKeyType#formatAmount(long,AmountFormat)
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
    public abstract CompoundTag toTag();

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

    public abstract void writeToPacket(FriendlyByteBuf data);

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

    public Component getDisplayName() {
        return Objects.requireNonNull(this.displayName);
    }

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
}
