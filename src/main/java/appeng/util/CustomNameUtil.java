package appeng.util;

import javax.annotation.Nullable;

import com.google.gson.JsonParseException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Allows serializing custom names to and from NBT data in the same format as
 * {@link net.minecraft.world.item.ItemStack}.
 */
public final class CustomNameUtil {
    private CustomNameUtil() {
    }

    /**
     * Writes a custom display name to the given tag the same was as {@link ItemStack#setHoverName(Component)} would.
     */
    public static void setCustomName(CompoundTag tag, @Nullable String name) {
        if (name == null || name.isEmpty()) {
            setCustomName(tag, (Component) null);
        } else {
            setCustomName(tag, Component.literal(name));
        }
    }

    /**
     * Writes a custom display name to the given tag the same was as {@link ItemStack#setHoverName(Component)} would.
     */
    public static void setCustomName(CompoundTag tag, @Nullable Component name) {
        if (name == null) {
            tag.remove(ItemStack.TAG_DISPLAY);
        } else {
            var display = new CompoundTag();
            display.putString(ItemStack.TAG_DISPLAY_NAME, Component.Serializer.toJson(name));
            tag.put(ItemStack.TAG_DISPLAY, display);
        }
    }

    /**
     * Reads a custom display name from the same property of the given tag, as an item stack would.
     *
     * @return Null if no name could be found or read.
     * @see ItemStack#getHoverName()
     */
    @Nullable
    public static Component getCustomName(CompoundTag tag) {
        if (!tag.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
            return null;
        }

        var compoundTag = tag.getCompound(ItemStack.TAG_DISPLAY);
        if (compoundTag.contains(ItemStack.TAG_DISPLAY_NAME, Tag.TAG_STRING)) {
            try {
                var component = Component.Serializer.fromJson(compoundTag.getString(ItemStack.TAG_DISPLAY_NAME));
                if (component != null) {
                    return component;
                }
            } catch (JsonParseException ignored) {
            }
        }

        return null;
    }
}
