package appeng.api.components;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ExportedUpgrades(List<ItemStack> upgrades) {
    // Defined using xmap since we previously used a List directly.
    public static Codec<ExportedUpgrades> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.CODEC.listOf().fieldOf("upgrades").forGetter(ExportedUpgrades::upgrades))
            .apply(builder, ExportedUpgrades::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ExportedUpgrades> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, ExportedUpgrades::upgrades,
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
