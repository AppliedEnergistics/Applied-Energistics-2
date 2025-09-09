package appeng.core.sync.packets;

import java.util.UUID;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.core.AEConfig;
import appeng.core.sync.BasePacket;

/**
 * Confirms to the player that a crafting job has started.
 */
public class CraftingJobStatusPacket extends BasePacket {
    /**
     * What is being crafted.
     */
    private UUID jobId;
    private AEKey what;
    private long requestedAmount;
    private long remainingAmount;
    private long elapsedTime;
    /**
     * If true, we notify the player when the job is finished.
     */
    private boolean isFollowing;
    private Status status;

    public CraftingJobStatusPacket(FriendlyByteBuf stream) {
        this.jobId = stream.readUUID();
        this.status = stream.readEnum(Status.class);
        this.what = AEKey.readKey(stream);
        this.requestedAmount = stream.readLong();
        this.remainingAmount = stream.readLong();
        this.elapsedTime = stream.readLong();
        this.isFollowing = stream.readBoolean();
    }

    public CraftingJobStatusPacket(UUID jobId, AEKey what, long requestedAmount, long remainingAmount,
            long elapsedTime, boolean isFollowing, Status status) {
        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(getPacketID());
        data.writeUUID(jobId);
        data.writeEnum(status);
        AEKey.writeKey(data, what);
        data.writeLong(requestedAmount);
        data.writeLong(remainingAmount);
        data.writeLong(elapsedTime);
        data.writeBoolean(isFollowing);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(Player player) {
        if (status == Status.STARTED) {
            if (AEConfig.instance().isPinAutoCraftedItems()) {
                PinnedKeys.pinKey(what, PinnedKeys.PinReason.CRAFTING);
            }
        }

        PendingCraftingJobs.jobStatus(jobId, what, requestedAmount, remainingAmount, elapsedTime, isFollowing, status);
    }

    public enum Status {
        STARTED,
        CANCELLED,
        FINISHED
    }
}
