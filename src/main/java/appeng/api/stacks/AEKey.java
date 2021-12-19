package appeng.api.stacks;

import javax.annotation.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
        var channel = AEKeyType.fromRawId(id);
        if (channel == null) {
            AELog.error("Received unknown key space id %d", id);
            return null;
        }
        return channel.readFromPacket(buffer);
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

    public boolean matches(@Nullable GenericStack stack) {
        return stack != null && stack.what().equals(this);
    }

    public abstract String getModId();

    public abstract void writeToPacket(FriendlyByteBuf data);

    /**
     * Wraps a key in an ItemStack that can be unwrapped into a key later.
     */
    public abstract ItemStack wrapForDisplayOrFilter();

    /**
     * Wraps a key in an ItemStack that can be unwrapped into a key later.
     */
    public abstract ItemStack wrap(int amount);

    /**
     * True to indicate that this type of {@link AEKey} supports range-based fuzzy search using
     * {@link AEKey#getFuzzySearchValue()} and {@link AEKey#getFuzzySearchMaxValue()}.
     * <p/>
     * For items this is used for damage-based search and filtering.
     */
    public boolean supportsFuzzyRangeSearch() {
        return false;
    }

    public abstract Component getDisplayName();
}
