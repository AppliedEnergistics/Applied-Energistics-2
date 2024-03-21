
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.crafting.CraftingStatus;

public record CraftingStatusPacket(CraftingStatus status) implements ClientboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatusPacket> STREAM_CODEC = StreamCodec.ofMember(
            CraftingStatusPacket::write,
            CraftingStatusPacket::decode);

    public static final Type<CraftingStatusPacket> TYPE = CustomAppEngPayload.createType("crafting_status");

    @Override
    public Type<CraftingStatusPacket> type() {
        return TYPE;
    }

    public static CraftingStatusPacket decode(RegistryFriendlyByteBuf buffer) {
        return new CraftingStatusPacket(CraftingStatus.read(buffer));
    }

    public void write(RegistryFriendlyByteBuf data) {
        status.write(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        Screen screen = Minecraft.getInstance().screen;

        if (screen instanceof CraftingCPUScreen<?> cpuScreen) {
            cpuScreen.postUpdate(this.status);
        }
    }

}
