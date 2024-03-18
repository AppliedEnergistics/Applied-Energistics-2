package appeng.crafting.pattern;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.machinezoo.noexception.optional.OptionalBoolean;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;

final class PatternNbtUtils {
    private PatternNbtUtils() {
    }

    public static boolean getBoolean(@Nullable CompoundTag tag, String name, boolean defaultValue) {
        return tryGetBoolean(tag, name).orElse(defaultValue);
    }

    public static OptionalBoolean tryGetBoolean(@Nullable CompoundTag tag, String name) {
        if (tag != null && tag.contains(name, Tag.TAG_BYTE)) {
            return OptionalBoolean.of(tag.getBoolean(name));
        }
        return OptionalBoolean.empty();
    }

    public static ResourceLocation getRequiredResourceLocation(@Nullable CompoundTag tag, String name) {
        if (tag != null && tag.contains(name, Tag.TAG_STRING)) {
            try {
                return new ResourceLocation(tag.getString(name));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to read required resource location " + name, e);
            }
        }
        throw new IllegalArgumentException("Tag is missing required resource location " + name);
    }

    public static Optional<ResourceLocation> tryGetResourceLocation(@Nullable CompoundTag tag, String name) {
        if (tag != null && tag.contains(name, Tag.TAG_STRING)) {
            try {
                return Optional.of(new ResourceLocation(tag.getString(name)));
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    public static AEItemKey getRequiredItemKey(@Nullable CompoundTag tag, String name) {
        if (tag != null && tag.contains(name, Tag.TAG_COMPOUND)) {
            return AEItemKey.fromTag(tag.getCompound(name));
        }
        throw new IllegalArgumentException("Tag is missing required key " + name);
    }

    public static ListTag encodeItemStackList(ItemStack[] stacks) {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (var stack : stacks) {
            if (stack.isEmpty()) {
                tag.add(new CompoundTag());
            } else {
                tag.add(stack.save(new CompoundTag()));
                foundStack = true;
            }
        }
        Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
        return tag;
    }

    /**
     * Attempts to read an ItemStack in a fault-tolerant way.
     */
    public static Optional<PatternDetailsTooltip.Entry> readItemStackFaultTolerant(@Nullable CompoundTag tag,
            String name) {
        if (tag != null) {
            return readItemStackFaultTolerant(tag.get(name));
        }
        return Optional.empty();
    }

    /**
     * Attempts to read an ItemStack in a fault-tolerant way.
     */
    public static Optional<PatternDetailsTooltip.Entry> readItemStackFaultTolerant(@Nullable Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag)) {
            return Optional.empty();
        }

        if (compoundTag.isEmpty()) {
            return Optional.empty();
        }

        var stack = GenericStack.fromItemStack(ItemStack.of(compoundTag));
        if (stack != null) {
            return Optional.of(new PatternDetailsTooltip.ValidEntry(stack));
        }

        // Attempt recovery of information
        try {
            var id = compoundTag.getString("id");
            var count = compoundTag.getInt("Count");
            return Optional.of(new PatternDetailsTooltip.InvalidEntry(Component.literal(id), AEKeyType.items(), count));
        } catch (Exception ignored) {
            // Fully unknown what we should do here
            return Optional.of(
                    new PatternDetailsTooltip.InvalidEntry(Component.literal("Invalid Entry"), AEKeyType.items(), 1));
        }
    }

    /**
     * Reads a list of ItemStacks as wrapped GenericStacks, if possible. This method works under the assumption that
     * normally deserializing the list probably fails and tries to recover as much information as possible.
     */
    public static void readItemStackListFaultTolerant(@Nullable CompoundTag tag, String name,
            Consumer<PatternDetailsTooltip.Entry> entryConsumer) {
        if (tag == null || !tag.contains(name, Tag.TAG_LIST)) {
            return;
        }

        var list = (ListTag) tag.get(name);
        for (var child : list) {
            readItemStackFaultTolerant(child).ifPresent(entryConsumer);
        }
    }

    public static Optional<PatternDetailsTooltip.Entry> readKeyFaultTolerant(@Nullable CompoundTag tag,
            String name) {
        // Try reading it normally first
        try {
            return Optional.of(new PatternDetailsTooltip.ValidEntry(
                    getRequiredItemKey(tag, name),
                    1));
        } catch (Exception ignored) {
        }

        // When reading failed, all we can do is trying to read the id
        if (tag != null && tag.contains(name, Tag.TAG_COMPOUND)) {
            var keyTag = tag.getCompound(name);
            if (keyTag.contains("id", Tag.TAG_STRING)) {
                // The very
                var id = keyTag.getString("id");
                return Optional.of(new PatternDetailsTooltip.InvalidEntry(Component.literal(id), AEKeyType.items(), 1));
            }
        }

        return Optional.empty();
    }

    public static GenericStack[] getRequiredGenericStackList(@Nullable CompoundTag tag, String name, int maxSize) {
        Objects.requireNonNull(tag, "Pattern must have a tag.");
        if (!tag.contains(name, Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Tag is missing required key " + name);
        }

        var listTag = tag.getList(name, Tag.TAG_COMPOUND);
        if (listTag.size() > maxSize) {
            throw new IllegalArgumentException("Cannot use more than " + maxSize + " ingredients");
        }

        var result = new GenericStack[listTag.size()];
        for (int x = 0; x < listTag.size(); ++x) {
            var entry = listTag.getCompound(x);
            if (entry.isEmpty()) {
                continue;
            }
            var stack = GenericStack.readTag(entry);
            if (stack == null) {
                throw new IllegalArgumentException("Pattern references missing stack: " + entry);
            }
            result[x] = stack;
        }
        return result;
    }

    public static Optional<PatternDetailsTooltip.Entry> readGenericStackFaultTolerant(@Nullable Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag) || compoundTag.isEmpty()) {
            return Optional.empty();
        }

        // Try reading it normally first
        try {
            var stack = GenericStack.readTag(compoundTag);
            if (stack != null) {
                return Optional.of(new PatternDetailsTooltip.ValidEntry(stack));
            }
        } catch (Exception ignored) {
        }

        long amount = 1;
        if (compoundTag.contains("#", Tag.TAG_LONG)) {
            amount = compoundTag.getLong("#");
        }

        var id = "Unknown";
        if (compoundTag.contains("id", Tag.TAG_STRING)) {
            id = compoundTag.getString("id");
        }
        var idString = Component.literal(id);

        // Try reading the key type
        AEKeyType keyType = null;
        if (compoundTag.contains("#c", Tag.TAG_STRING)) {
            var keyTypeId = compoundTag.getString("#c");
            try {
                keyType = AEKeyTypes.get(new ResourceLocation(keyTypeId));
            } catch (Exception ignored) {
                idString.append(" (" + keyTypeId + ")");
            }
        }

        return Optional.of(new PatternDetailsTooltip.InvalidEntry(idString, keyType, amount));
    }

    public static void getGenericStackListFaultTolerant(@Nullable CompoundTag tag, String name,
            Consumer<PatternDetailsTooltip.Entry> consumer) {
        if (tag == null || !tag.contains(name, Tag.TAG_LIST)) {
            return;
        }

        var listTag = (ListTag) tag.get(name);

        for (var child : listTag) {
            if (child instanceof CompoundTag compoundChild) {
                if (!compoundChild.isEmpty()) {
                    var stack = GenericStack.readTag(compoundChild);
                    if (stack != null) {
                        consumer.accept(new PatternDetailsTooltip.ValidEntry(stack));
                    } else {
                        // Try recovery
                        readGenericStackFaultTolerant(child).ifPresent(consumer);
                    }
                }
            }
        }
    }

    public static Optional<String> tryGetString(@Nullable CompoundTag tag, String name) {
        if (tag != null && tag.contains(name, Tag.TAG_STRING)) {
            return Optional.of(tag.getString(name));
        }
        return Optional.empty();
    }
}
