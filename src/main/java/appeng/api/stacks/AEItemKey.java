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
    private final Item item;
    private final InternedTag internedTag;
    private final int hashCode;
    private final int cachedDamage;

    private AEItemKey(Item item, InternedTag internedTag) {
        super(Platform.getItemDisplayName(item, internedTag.tag));
        this.item = item;
        this.internedTag = internedTag;
        this.hashCode = item.hashCode() * 31 + internedTag.hashCode;
        if (internedTag.tag != null && internedTag.tag.get("Damage") instanceof NumericTag numericTag) {
            this.cachedDamage = numericTag.getAsInt();
        } else {
            this.cachedDamage = 0;
        }
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
        return of(item, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEItemKey aeItemKey = (AEItemKey) o;
        // TagHolder instances are interned, compare by reference.
        return item == aeItemKey.item && internedTag == aeItemKey.internedTag;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEItemKey of(ItemLike item) {
        return of(item, null);
    }

    public static AEItemKey of(ItemLike item, @Nullable CompoundTag tag) {
        return new AEItemKey(item.asItem(), InternedTag.of(tag, false));
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.is(item) && Objects.equals(stack.getTag(), internedTag.tag);
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

    public Item getItem() {
        return item;
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
        result.putString("id", BuiltInRegistries.ITEM.getKey(item).toString());

        if (internedTag.tag != null) {
            result.put("tag", internedTag.tag.copy());
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
        return this.cachedDamage;
    }

    /**
     * @see ItemStack#getDamageValue()
     */
    @Override
    public int getFuzzySearchMaxValue() {
        return item.getMaxDamage();
    }

    @Override
    public ResourceLocation getId() {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public ItemVariant toVariant() {
        return ItemVariant.of(item, internedTag.tag);
    }

    /**
     * @return <strong>NEVER MODIFY THE RETURNED TAG</strong>
     */
    @Nullable
    public CompoundTag getTag() {
        return internedTag.tag;
    }

    @Nullable
    public CompoundTag copyTag() {
        return internedTag.tag != null ? internedTag.tag.copy() : null;
    }

    public boolean hasTag() {
        return internedTag.tag != null;
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return toStack();
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        while (amount > 0) {
            if (drops.size() > 1000) {
                AELog.warn("Tried dropping an excessive amount of items, ignoring %s %ss", amount, item);
                break;
            }

            var taken = Math.min(amount, item.getMaxStackSize());
            amount -= taken;
            drops.add(toStack((int) taken));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTagged(TagKey<?> tag) {
        // This will just return false for incorrectly cast tags
        return item.builtInRegistryHolder().is((TagKey<Item>) tag);
    }

    /**
     * @return True if the item represented by this key is damaged.
     */
    public boolean isDamaged() {
        return cachedDamage > 0;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(Item.getId(item));
        CompoundTag compoundTag = null;
        if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
            compoundTag = internedTag.tag;
        }
        data.writeNbt(compoundTag);
    }

    public static AEItemKey fromPacket(FriendlyByteBuf data) {
        int i = data.readVarInt();
        var item = Item.byId(i);
        var tag = data.readNbt();
        return new AEItemKey(item, InternedTag.of(tag, true));
    }

    @Override
    public String toString() {
        var id = BuiltInRegistries.ITEM.getKey(item);
        String idString = id != BuiltInRegistries.ITEM.getDefaultKey() ? id.toString()
                : item.getClass().getName() + "(unregistered)";
        return internedTag.tag == null ? idString : idString + " (+tag)";
    }

    private static final class InternedTag {
        private static final InternedTag EMPTY = new InternedTag(null);

        private static final WeakHashMap<InternedTag, WeakReference<InternedTag>> INTERNED = new WeakHashMap<>();

        private final CompoundTag tag;
        private final int hashCode;

        InternedTag(CompoundTag tag) {
            this.tag = tag;
            this.hashCode = Objects.hashCode(tag);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            InternedTag internedTag = (InternedTag) o;
            return Objects.equals(tag, internedTag.tag);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public static InternedTag of(@Nullable CompoundTag tag, boolean giveOwnership) {
            if (tag == null) {
                return EMPTY;
            }

            synchronized (AEItemKey.class) {
                var searchHolder = new InternedTag(tag);
                var weakRef = INTERNED.get(searchHolder);
                InternedTag ret = null;

                if (weakRef != null) {
                    ret = weakRef.get();
                }

                if (ret == null) {
                    // Intern a copy of the tag, since we cannot guarantee that we take ownership
                    ret = new InternedTag(giveOwnership ? tag : tag.copy());
                    INTERNED.put(ret, new WeakReference<>(ret));
                }

                return ret;
            }
        }
    }
}
