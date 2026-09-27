package appeng.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public final class StackUtil {
    private StackUtil() {
    }

    public static CompoundTag toTag(RegistryAccess registries, ItemStack stack) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return (CompoundTag) ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack).getOrThrow();
    }

    public static ItemStack fromTag(RegistryAccess registries, CompoundTag tag) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.decode(ops, tag).getOrThrow().getFirst();
    }
}
