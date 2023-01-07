package appeng.api.stacks;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;
import appeng.util.Platform;

public final class AEItemKey extends AEKey {
    private static final WeakHashMap<SharedItemKey, WeakReference<AEItemKey>> interningMap = new WeakHashMap<>();

    private static synchronized AEItemKey getInterned(Item item, @Nullable CompoundTag originalTag) {
        var searchKey = new SharedItemKey(item, originalTag);
        var weak = interningMap.get(searchKey);
        AEItemKey ret = null;

        if (weak != null) {
            ret = weak.get();
        }

        if (ret == null) {
            // Always copy the tag since we're not sure we can take ownership of it.
            var sharedKey = new SharedItemKey(item, originalTag == null ? null : originalTag.copy());
            ret = new AEItemKey(sharedKey);
            interningMap.put(sharedKey, new WeakReference<>(ret));
        }

        return ret;
    }

    private final SharedItemKey internalKey;

    /**
     * Never call this directly, always use {@link #getInterned(Item, CompoundTag)}.
     */
    private AEItemKey(SharedItemKey internalKey) {
        super(Platform.getItemDisplayName(internalKey.item, internalKey.tag));

        this.internalKey = internalKey;
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

    public static boolean matches(AEKey what, ItemStack itemStack) {
        return what instanceof AEItemKey itemKey && itemKey.matches(itemStack);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEItemKey;
    }

    public static AEKeyFilter filter() {
        return AEItemKey::is;
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.items();
    }

    @Override
    public AEItemKey dropSecondary() {
        return of(internalKey.item, null);
    }

    @Override
    public boolean equals(Object o) {
        return this == o; // Safe since we're interning all AEItemKeys.
    }

    @Override
    public int hashCode() {
        return internalKey.hashCode;
    }

    public static AEItemKey of(ItemLike item) {
        return of(item, null);
    }

    public static AEItemKey of(ItemLike item, @Nullable CompoundTag tag) {
        return getInterned(item.asItem(), tag);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.is(internalKey.item) && Objects.equals(stack.getTag(), internalKey.tag);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        var result = new ItemStack(internalKey.item);
        result.setTag(copyTag());
        result.setCount(count);
        return result;
    }

    public Item getItem() {
        return internalKey.item;
    }

    @Nullable
    public static AEItemKey fromTag(CompoundTag tag) {
        try {
            var item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(tag.getString("id")))
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
        result.putString("id", BuiltInRegistries.ITEM.getKey(internalKey.item).toString());

        if (internalKey.tag != null) {
            result.put("tag", internalKey.tag.copy());
        }

        return result;
    }

    @Override
    public Object getPrimaryKey() {
        return internalKey.item;
    }

    /**
     * @see ItemStack#getMaxDamage()
     */
    @Override
    public int getFuzzySearchValue() {
        return internalKey.cachedDamage;
    }

    /**
     * @see ItemStack#getDamageValue()
     */
    @Override
    public int getFuzzySearchMaxValue() {
        return internalKey.item.getMaxDamage();
    }

    @Override
    public ResourceLocation getId() {
        return BuiltInRegistries.ITEM.getKey(internalKey.item);
    }

    public ItemVariant toVariant() {
        return ItemVariant.of(internalKey.item, internalKey.tag);
    }

    /**
     * @return <strong>NEVER MODIFY THE RETURNED TAG</strong>
     */
    @Nullable
    public CompoundTag getTag() {
        return internalKey.tag;
    }

    @Nullable
    public CompoundTag copyTag() {
        return internalKey.tag != null ? internalKey.tag.copy() : null;
    }

    public boolean hasTag() {
        return internalKey.tag != null;
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return toStack();
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        while (amount > 0) {
            if (drops.size() > 1000) {
                AELog.warn("Tried dropping an excessive amount of items, ignoring %s %ss", amount, internalKey.item);
                break;
            }

            var taken = Math.min(amount, internalKey.item.getMaxStackSize());
            amount -= taken;
            drops.add(toStack((int) taken));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTagged(TagKey<?> tag) {
        // This will just return false for incorrectly cast tags
        return internalKey.item.builtInRegistryHolder().is((TagKey<Item>) tag);
    }

    /**
     * @return True if the item represented by this key is damaged.
     */
    public boolean isDamaged() {
        return internalKey.tag != null && internalKey.tag.getInt(ItemStack.TAG_DAMAGE) > 0;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(Item.getId(internalKey.item));
        CompoundTag compoundTag = null;
        if (internalKey.item.canBeDepleted() || internalKey.item.shouldOverrideMultiplayerNbt()) {
            compoundTag = internalKey.tag;
        }
        data.writeNbt(compoundTag);
    }

    public static AEItemKey fromPacket(FriendlyByteBuf data) {
        int i = data.readVarInt();
        var item = Item.byId(i);
        var tag = data.readNbt();
        return getInterned(item, tag);
    }

    @Override
    public String toString() {
        var id = BuiltInRegistries.ITEM.getKey(internalKey.item);
        String idString = id != BuiltInRegistries.ITEM.getDefaultKey() ? id.toString()
                : internalKey.item.getClass().getName() + "(unregistered)";
        return internalKey.tag == null ? idString : idString + " (+tag)";
    }

    private static final class SharedItemKey {
        private final Item item;
        @Nullable
        private final CompoundTag tag;
        private final int hashCode;
        private final int cachedDamage;

        SharedItemKey(Item item, @Nullable CompoundTag tag) {
            this.item = item;
            this.tag = tag;
            this.hashCode = Objects.hash(item, tag);
            if (this.tag != null && tag.get("Damage") instanceof NumericTag numericTag) {
                this.cachedDamage = numericTag.getAsInt();
            } else {
                this.cachedDamage = 0;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            SharedItemKey aeItemKey = (SharedItemKey) o;
            return item == aeItemKey.item && Objects.equals(tag, aeItemKey.tag);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
