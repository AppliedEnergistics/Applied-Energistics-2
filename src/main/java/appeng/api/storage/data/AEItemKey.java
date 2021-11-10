package appeng.api.storage.data;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.core.AELog;

public class AEItemKey extends AEKey {
    private final Item item;
    @Nullable
    private final CompoundTag tag;
    private final int hashCode;

    private AEItemKey(Item item, @Nullable CompoundTag tag) {
        this.item = item;
        this.tag = tag;
        this.hashCode = Objects.hash(item, tag);
    }

    @Nullable
    public static AEItemKey of(ItemVariant variant) {

        if (variant.isBlank()) {
            return null;
        }
        return of(variant.getItem(), variant.getNbt());
    }

    @Nullable
    public static AEItemKey of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return of(stack.getItem(), stack.getTag());
    }

    @Override
    public IStorageChannel<?> getChannel() {
        return StorageChannels.items();
    }

    @Override
    public AEItemKey dropSecondary() {
        return of(item, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEItemKey aeItemKey = (AEItemKey) o;
        // The hash code comparison is a fast-fail for two objects with different NBT or items
        return hashCode == aeItemKey.hashCode && item == aeItemKey.item && Objects.equals(tag, aeItemKey.tag);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEItemKey of(ItemLike item) {
        return of(item, null);
    }

    public static AEItemKey of(ItemLike item, @Nullable CompoundTag tag) {
        // Do a defensive copy of the tag if we're not sure that we can take ownership
        return new AEItemKey(item.asItem(), tag != null ? tag.copy() : null);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.is(item) && Objects.equals(stack.getTag(), tag);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        var result = new ItemStack(item);
        result.setTag(copyTag());
        result.setCount(count);
        return result;
    }

    @Nullable
    public CompoundTag copyTag() {
        return tag != null ? tag.copy() : null;
    }

    public Item getItem() {
        return item;
    }

    @Nullable
    public static AEItemKey fromTag(CompoundTag tag) {
        try {
            var item = Registry.ITEM.getOptional(new ResourceLocation(tag.getString("id")))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown item id."));
            var extraTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            return of(item, extraTag);
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
            return null;
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        result.putString("id", Registry.ITEM.getKey(item).toString());

        if (tag != null) {
            result.put("tag", tag.copy());
        }

        return result;
    }

    @Override
    public Object getPrimaryKey() {
        return item;
    }

    /**
     * @see ItemStack#getMaxDamage()
     */
    @Override
    public int getFuzzySearchValue() {
        return this.tag == null ? 0 : this.tag.getInt("Damage");
    }

    /**
     * @see ItemStack#getDamageValue()
     */
    @Override
    public int getFuzzySearchMaxValue() {
        return item.getMaxDamage();
    }

    @Override
    public String getModId() {
        return Registry.ITEM.getKey(item).getNamespace();
    }

    public ItemVariant toVariant() {
        return ItemVariant.of(item, tag);
    }

    /**
     * @return <strong>NEVER MODIFY THE RETURNED TAG</strong>
     */
    @Nullable
    public CompoundTag getTag() {
        return tag;
    }

    public boolean hasTag() {
        return tag != null;
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return toStack();
    }

    @Override
    public ItemStack wrap(int amount) {
        return toStack(amount);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(Item.getId(item));
        CompoundTag compoundTag = null;
        if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
            compoundTag = tag;
        }
        data.writeNbt(compoundTag);
    }

    public static AEItemKey fromPacket(FriendlyByteBuf data) {
        int i = data.readVarInt();
        var item = Item.byId(i);
        var tag = data.readNbt();
        return new AEItemKey(item, tag);
    }

    @Override
    public String toString() {
        return tag == null ? item.toString() : item.toString() + " (+tag)";
    }
}
