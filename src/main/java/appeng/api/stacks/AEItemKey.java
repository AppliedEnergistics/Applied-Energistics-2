package appeng.api.stacks;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

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

    @Nullable
    private static CompoundTag serializeStackCaps(ItemStack stack) {
        try {
            var caps = stack.serializeAttachments();
            // Ensure stacks with no serializable cap providers are treated the same as stacks with no caps!
            return caps == null || caps.isEmpty() ? null : caps;
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to call serializeCaps", ex);
        }
    }

    private final Item item;
    private final InternedTag internedTag;
    private final InternedTag internedCaps;
    private final int hashCode;
    private final int cachedDamage;
    /**
     * A lazily initialized itemstack used for display and ingredient testing purposes. This should never be modified
     * and will always have amount 1.
     */
    @Nullable
    private ItemStack readOnlyStack;

    /**
     * Max stack size cache, or {@code -1} if not initialized.
     */
    private int maxStackSize = -1;

    private AEItemKey(Item item, InternedTag internedTag, InternedTag internedCaps) {
        this.item = item;
        this.internedTag = internedTag;
        this.internedCaps = internedCaps;
        this.hashCode = Objects.hash(item, internedTag, internedCaps);
        if (internedTag.tag != null && internedTag.tag.get("Damage") instanceof NumericTag numericTag) {
            this.cachedDamage = numericTag.getAsInt();
        } else {
            this.cachedDamage = 0;
        }
    }

    @Nullable
    public static AEItemKey of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        var ret = of(stack.getItem(), stack.getTag(), serializeStackCaps(stack));
        // Cache max stack size since we already have an ItemStack.
        ret.maxStackSize = stack.getMaxStackSize();
        return ret;
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
        return item == aeItemKey.item && internedTag == aeItemKey.internedTag && internedCaps == aeItemKey.internedCaps;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEItemKey of(ItemLike item) {
        return of(item, null);
    }

    public static AEItemKey of(ItemLike item, @Nullable CompoundTag tag) {
        return of(item, tag, null);
    }

    private static AEItemKey of(ItemLike item, @Nullable CompoundTag tag, @Nullable CompoundTag caps) {
        return new AEItemKey(item.asItem(), InternedTag.of(tag, false), InternedTag.of(caps, false));
    }

    public boolean matches(ItemStack stack) {
        // TODO: remove or optimize cap check if it becomes too slow >:-(
        return !stack.isEmpty() && stack.is(item) && Objects.equals(stack.getTag(), internedTag.tag)
                && Objects.equals(serializeStackCaps(stack), internedCaps.tag);
    }

    public boolean matches(Ingredient ingredient) {
        return ingredient.test(getReadOnlyStack());
    }

    /**
     * @return The ItemStack represented by this key. <strong>NEVER MUTATE THIS</strong>
     */
    public ItemStack getReadOnlyStack() {
        if (readOnlyStack == null) {
            readOnlyStack = new ItemStack(item, 1, internedCaps.tag);
            readOnlyStack.setTag(internedTag.tag);
        } else {
            if (readOnlyStack.isEmpty()) {
                LOG.error("Something destroyed the read-only itemstack of {}", this);
                readOnlyStack = null;
                return getReadOnlyStack();
            }
        }
        return readOnlyStack;
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        var result = new ItemStack(item, count, internedCaps.tag);
        result.setTag(copyTag());
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
            var extraCaps = tag.contains("caps") ? tag.getCompound("caps") : null;

            // Sanitize caps since we'll be deserializing them over and over
            // If there was a non backwards compatible change to a modded item (i.e. it lost its cap on an item)
            // This will trigger continuous error logs on every call to toStack(), killing performance
            if (extraCaps != null) {
                var stack = new ItemStack(item, 1, extraCaps);
                var sanitizedCaps = stack.serializeAttachments();
                if (!Objects.equals(extraCaps, sanitizedCaps)) {
                    LOG.info("Sanitized item attachments for {} from {} -> {}", item.asItem(),
                            extraCaps, sanitizedCaps);
                }

                extraCaps = sanitizedCaps;
            }

            return of(item, extraTag, extraCaps);
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
        if (internedCaps.tag != null) {
            result.put("caps", internedCaps.tag.copy());
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
        return getReadOnlyStack().getMaxDamage();
    }

    @Override
    public ResourceLocation getId() {
        return BuiltInRegistries.ITEM.getKey(item);
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
        return item.builtInRegistryHolder().is((TagKey<Item>) tag);
    }

    /**
     * @return True if the item represented by this key is damaged.
     */
    public boolean isDamaged() {
        return cachedDamage > 0;
    }

    public int getMaxStackSize() {
        int ret = maxStackSize;

        if (ret == -1) {
            maxStackSize = ret = getReadOnlyStack().getMaxStackSize();
        }

        return ret;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(Item.getId(item));
        CompoundTag compoundTag = null;
        if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
            compoundTag = internedTag.tag;
        }
        data.writeNbt(compoundTag);
        data.writeNbt(internedCaps.tag);
    }

    public static AEItemKey fromPacket(FriendlyByteBuf data) {
        int i = data.readVarInt();
        var item = Item.byId(i);
        var compoundTag = data.readNbt();
        var attachedCapsData = data.readNbt();
        return new AEItemKey(item, InternedTag.of(compoundTag, true),
                InternedTag.of(attachedCapsData, true));
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
