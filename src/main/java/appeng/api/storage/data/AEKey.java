package appeng.api.storage.data;

import javax.annotation.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.GenericStack;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
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
        IStorageChannel<?> channel;
        try {
            channel = StorageChannels.get(new ResourceLocation(channelId));
        } catch (IllegalArgumentException | ResourceLocationException e) {
            AELog.warn("Cannot deserialize generic key from %s because channel '%s' is missing.", tag, channelId);
            return null;
        }

        return channel.loadKeyFromTag(tag);
    }

    public static void writeOptionalKey(FriendlyByteBuf buffer, @Nullable AEKey key) {
        buffer.writeBoolean(key != null);
        if (key != null) {
            writeKey(buffer, key);
        }
    }

    @Nullable
    public static AEKey readOptionalKey(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        return readKey(buffer);
    }

    public static void writeKey(FriendlyByteBuf buffer, AEKey key) {
        buffer.writeResourceLocation(key.getChannel().getId());
        key.writeToPacket(buffer);
    }

    @Nullable
    public static AEKey readKey(FriendlyByteBuf buffer) {
        var channel = StorageChannels.get(buffer.readResourceLocation());
        return channel.readFromPacket(buffer);
    }

    /**
     * Same as {@link #toTag()}, but includes type information so that {@link #fromTagGeneric(CompoundTag)} can restore
     * this particular type of key withot knowing the actual type beforehand.
     */
    public final CompoundTag toTagGeneric() {
        var tag = toTag();
        tag.putString("#c", getChannel().getId().toString());
        return tag;
    }

    /**
     * Can be used as factor for transferring stacks of a channel.
     * <p>
     * E.g. used by IO Ports to transfer 1000 mB, not 1 mB to match the item channel transferring a full bucket per
     * operation.
     */
    public int transferFactor() {
        return 1;
    }

    /**
     * The number of units (eg item count, or millibuckets) that can be stored per byte in a storage cell. Standard
     * value for items is 8, and for fluids it's 8000
     *
     * @return number of units
     */
    public int getUnitsPerByte() {
        return 8;
    }

    /**
     * The storage channel this type of resource key is used for.
     */
    public abstract IStorageChannel<?> getChannel();

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

    @SuppressWarnings("unchecked")
    public <T extends AEKey> T cast(IStorageChannel<T> channel) {
        if (getChannel() == channel) {
            return (T) this;
        }
        return null;
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
