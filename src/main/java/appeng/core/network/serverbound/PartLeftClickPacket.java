
package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;

import appeng.api.parts.IPartHost;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;

/**
 * Packet sent when a player left-clicks on a part attached to a cable bus. This packet contains the hit position to
 * restore the part that was hit on the client.
 */
public record PartLeftClickPacket(BlockHitResult hitResult, boolean alternateUseMode) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PartLeftClickPacket> STREAM_CODEC = StreamCodec.ofMember(
            PartLeftClickPacket::write,
            PartLeftClickPacket::decode);

    public static final Type<PartLeftClickPacket> TYPE = CustomAppEngPayload.createType("part_left_click");

    @Override
    public Type<PartLeftClickPacket> type() {
        return TYPE;
    }

    public static PartLeftClickPacket decode(RegistryFriendlyByteBuf stream) {
        var hitResult = stream.readBlockHitResult();
        var alternateUseMode = stream.readBoolean();
        return new PartLeftClickPacket(hitResult, alternateUseMode);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockHitResult(hitResult);
        data.writeBoolean(alternateUseMode);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        // Fire event on the server to give protection mods a chance to cancel the interaction
        var evt = CommonHooks.onLeftClickBlock(player, hitResult.getBlockPos(), hitResult.getDirection(),
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
        NeoForge.EVENT_BUS.post(evt);
        if (evt.isCanceled() || evt.getUseBlock().isFalse()) {
            return;
        }

        var localPos = hitResult.getLocation().subtract(
                hitResult.getBlockPos().getX(),
                hitResult.getBlockPos().getY(),
                hitResult.getBlockPos().getZ());

        if (player.level().getBlockEntity(hitResult.getBlockPos()) instanceof IPartHost partHost) {
            var selectedPart = partHost.selectPartLocal(localPos);
            if (selectedPart.part != null) {
                if (!alternateUseMode) {
                    selectedPart.part.onClicked(player, localPos);
                } else {
                    selectedPart.part.onShiftClicked(player, localPos);
                }
            }
        }
    }
}
