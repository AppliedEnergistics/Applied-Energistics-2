package appeng.core.sync.packets;

import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.container.me.crafting.CraftingPlanSummary;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

/**
 * Transfers a {@link CraftingPlanSummary} to the client for a {@link appeng.container.me.crafting.CraftConfirmContainer}
 */
public class CraftConfirmPlanPacket extends BasePacket {
    private final CraftingPlanSummary plan;

    public CraftConfirmPlanPacket(PacketBuffer data) {
        this.plan = CraftingPlanSummary.read(data);
    }

    public CraftConfirmPlanPacket(CraftingPlanSummary plan) {
        this.plan = plan;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(getPacketID());
        plan.write(data);
        configureWrite(data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        if (player.openContainer instanceof CraftConfirmContainer) {
            CraftConfirmContainer container = (CraftConfirmContainer) player.openContainer;
            container.setPlan(plan);
        }
    }
}
