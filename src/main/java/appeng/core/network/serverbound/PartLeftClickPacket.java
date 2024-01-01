
package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import appeng.api.parts.IPartHost;
import appeng.core.network.ServerboundPacket;

/**
 * Packet sent when a player left-clicks on a part attached to a cable bus. This packet contains the hit position to
 * restore the part that was hit on the client.
 */
public record PartLeftClickPacket(BlockHitResult hitResult, boolean alternateUseMode) implements ServerboundPacket {
    public static PartLeftClickPacket decode(FriendlyByteBuf stream) {
        var hitResult = stream.readBlockHitResult();
        var alternateUseMode = stream.readBoolean();
        return new PartLeftClickPacket(hitResult, alternateUseMode);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeBlockHitResult(hitResult);
        data.writeBoolean(alternateUseMode);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        // Fire event on the server to give protection mods a chance to cancel the interaction
        var evt = new PlayerInteractEvent.LeftClickBlock(player, hitResult.getBlockPos(), hitResult.getDirection());
        NeoForge.EVENT_BUS.post(evt);
        if (evt.isCanceled() || evt.getResult() == net.neoforged.bus.api.Event.Result.DENY) {
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
