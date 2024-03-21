
package appeng.core.network.clientbound;

import java.util.UUID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.core.AEConfig;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

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

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingJobStatusPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    CraftingJobStatusPacket::write,
                    CraftingJobStatusPacket::decode);

    public static final Type<CraftingJobStatusPacket> TYPE = CustomAppEngPayload.createType("crafting_job_status");

    @Override
    public Type<CraftingJobStatusPacket> type() {
        return TYPE;
    }

    public static CraftingJobStatusPacket decode(RegistryFriendlyByteBuf stream) {
        var jobId = stream.readUUID();
        var status = stream.readEnum(Status.class);
        var what = AEKey.readKey(stream);
        var requestedAmount = stream.readLong();
        var remainingAmount = stream.readLong();
        return new CraftingJobStatusPacket(jobId, what, requestedAmount, remainingAmount, status);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeUUID(jobId);
        data.writeEnum(status);
        AEKey.writeKey(data, what);
        data.writeLong(requestedAmount);
        data.writeLong(remainingAmount);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
