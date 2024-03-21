package appeng.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class RecursiveTagReplace {
    private RecursiveTagReplace() {
    }

    public static int replace(Tag tag, String from, String to) {
        if (tag instanceof CompoundTag compoundTag) {
            return replace(compoundTag, from, to);
        } else if (tag instanceof ListTag listTag) {
            return replace(listTag, from, to);
        } else {
            return 0;
        }
    }

    public static int replace(CompoundTag compoundTag, String from, String to) {
        int found = 0;
        for (var key : compoundTag.getAllKeys()) {
            var child = compoundTag.get(key);
            if (child instanceof StringTag stringChild && stringChild.getAsString().equals(from)) {
                compoundTag.put(key, StringTag.valueOf(to));
                found++;
            } else if (child instanceof CompoundTag compoundChild) {
                found += replace(compoundChild, from, to);
            } else if (child instanceof ListTag listChild) {
                found += replace(listChild, from, to);
            }
        }
        return found;
    }

    public static int replace(ListTag listTag, String from, String to) {
        int found = 0;
        for (int i = 0; i < listTag.size(); i++) {
            var child = listTag.get(i);
            if (child instanceof StringTag stringChild && stringChild.getAsString().equals(from)) {
                listTag.set(i, StringTag.valueOf(to));
                found++;
            } else if (child instanceof CompoundTag compoundChild) {
                found += replace(compoundChild, from, to);
            } else if (child instanceof ListTag listChild) {
                found += replace(listChild, from, to);
            }
        }
        return found;
    }
}
