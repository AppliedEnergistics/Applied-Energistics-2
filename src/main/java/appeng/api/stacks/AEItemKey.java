package appeng.api.stacks;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.CharDoubleMutablePair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.datafix.fixes.ItemStackSpawnEggFix;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;

public final class AEItemKey extends AEKey {
    private static final Logger LOG = LoggerFactory.getLogger(AEItemKey.class);

    private final ItemStack stack;
    private final int hashCode;
    private final int maxStackSize;
    private final int damage;

    private AEItemKey(ItemStack stack) {
        Preconditions.checkArgument(!stack.isEmpty(), "stack is empty");
        this.stack = stack;
        this.hashCode = ItemStack.hashItemAndComponents(stack);
        this.maxStackSize = stack.getMaxStackSize();
        this.damage = stack.getDamageValue();
    }

    @Nullable
    public static AEItemKey of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return new AEItemKey(stack.copy());
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
        return of(stack.getItem().getDefaultInstance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEItemKey aeItemKey = (AEItemKey) o;
        // The hash code comparison is a fast-fail cheap check
        return this.hashCode == aeItemKey.hashCode && ItemStack.isSameItemSameComponents(stack, aeItemKey.stack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEItemKey of(ItemLike item) {
        return of(item.asItem().getDefaultInstance());
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && ItemStack.isSameItemSameComponents(this.stack, stack);
    }

    public boolean matches(Ingredient ingredient) {
        return ingredient.test(getReadOnlyStack());
    }

    /**
     * @return The ItemStack represented by this key. <strong>NEVER MUTATE THIS</strong>
     */
    public ItemStack getReadOnlyStack() {
        return stack;
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        return stack.copyWithCount(count);
    }

    public Item getItem() {
        return stack.getItem();
    }

    @Nullable
    public static AEItemKey fromTag(HolderLookup.Provider provider, CompoundTag tag) {
        try {
            return of(ItemStack.parseOptional(provider, tag));
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
            return null;
        }
    }

    @Override
    public CompoundTag toTag(HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        stack.save(provider, result);
        return result;
    }

    @Override
    public Object getPrimaryKey() {
        return stack.getItem();
    }

    /**
     * @see ItemStack#getMaxDamage()
     */
    @Override
    public int getFuzzySearchValue() {
        return this.damage;
    }

    /**
     * @see ItemStack#getDamageValue()
     */
    @Override
    public int getFuzzySearchMaxValue() {
        return getReadOnlyStack().getMaxDamage();
    }

    @Override
    public ResourceLocation getId() {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return toStack();
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        while (amount > 0) {
            if (drops.size() > 1000) {
                AELog.warn("Tried dropping an excessive amount of items, ignoring %s %ss", amount, stack.getItem());
                break;
            }

            var taken = Math.min(amount, getMaxStackSize());
            amount -= taken;
            drops.add(toStack((int) taken));
        }
    }

    @Override
    protected Component computeDisplayName() {
        return getReadOnlyStack().getHoverName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTagged(TagKey<?> tag) {
        // This will just return false for incorrectly cast tags
        return stack.is((TagKey<Item>) tag);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<T> type) {
        return stack.get(type);
    }

    @Override
    public boolean hasComponents() {
        return stack.getComponents().isEmpty();
    }

    /**
     * @return True if the item represented by this key is damaged.
     */
    public boolean isDamaged() {
        return damage > 0;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        ItemStack.STREAM_CODEC.encode(data, stack);
    }

    public static AEItemKey fromPacket(RegistryFriendlyByteBuf data) {
        var stack = ItemStack.STREAM_CODEC.decode(data);
        return new AEItemKey(stack);
    }

    @Override
    public String toString() {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String idString = id != BuiltInRegistries.ITEM.getDefaultKey() ? id.toString()
                : stack.getItem().getClass().getName() + "(unregistered)";
        return stack.getComponents().isEmpty() ? idString : idString + " (+components)";
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
                    // Copy the tag if we don't get to have ownership of it
                    if (giveOwnership) {
                        ret = searchHolder;
                    } else {
                        ret = new InternedTag(tag.copy());
                    }
                    INTERNED.put(ret, new WeakReference<>(ret));
                }

                return ret;
            }
        }
    }
}
