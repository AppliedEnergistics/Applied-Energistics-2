package appeng.api.components;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ExportedUpgrades(List<ItemStack> upgrades) {
    // Defined using xmap since we previously used a List directly.
    // TODO 1.21.1 Use a normal record codec
    public static Codec<ExportedUpgrades> CODEC = ItemStack.CODEC.listOf().xmap(ExportedUpgrades::new,
            ExportedUpgrades::upgrades);

    public static StreamCodec<RegistryFriendlyByteBuf, ExportedUpgrades> STREAM_CODEC = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC, ExportedUpgrades::upgrades,
            ExportedUpgrades::new);

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof ExportedUpgrades that))
            return false;
        return ItemStack.listMatches(upgrades, that.upgrades);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(upgrades);
    }
}
