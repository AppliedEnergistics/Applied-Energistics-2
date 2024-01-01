
package appeng.core.network.clientbound;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.core.AEConfig;
import appeng.core.network.ClientboundPacket;

/**
 * Confirms to the player that a crafting job has started.
 */
public record CraftingJobStatusPacket(
        UUID jobId,
        AEKey what,
        long requestedAmount,
        long remainingAmount,
        Status status

) implements ClientboundPacket {

    public static CraftingJobStatusPacket decode(FriendlyByteBuf stream) {
        var jobId = stream.readUUID();
        var status = stream.readEnum(Status.class);
        var what = AEKey.readKey(stream);
        var requestedAmount = stream.readLong();
        var remainingAmount = stream.readLong();
        return new CraftingJobStatusPacket(jobId, what, requestedAmount, remainingAmount, status);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeUUID(jobId);
        data.writeEnum(status);
        AEKey.writeKey(data, what);
        data.writeLong(requestedAmount);
        data.writeLong(remainingAmount);
    }

    @Override
    public void handleOnClient(Player player) {
        if (status == Status.STARTED) {
            if (AEConfig.instance().isPinAutoCraftedItems()) {
                PinnedKeys.pinKey(what, PinnedKeys.PinReason.CRAFTING);
            }
        }

        PendingCraftingJobs.jobStatus(jobId, what, requestedAmount, remainingAmount, status);
    }

    public enum Status {
        STARTED,
        CANCELLED,
        FINISHED
    }
}
