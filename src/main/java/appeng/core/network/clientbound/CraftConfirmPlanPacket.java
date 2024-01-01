
package appeng.core.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.core.network.ClientboundPacket;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;

/**
 * Transfers a {@link CraftingPlanSummary} to the client for a {@link CraftConfirmMenu}
 */
public record CraftConfirmPlanPacket(CraftingPlanSummary plan) implements ClientboundPacket {
    public static CraftConfirmPlanPacket decode(FriendlyByteBuf data) {
        return new CraftConfirmPlanPacket(CraftingPlanSummary.read(data));
    }

    @Override
    public void write(FriendlyByteBuf data) {
        plan.write(data);
    }

    @Override
    public void handleOnClient(Player player) {
        if (player.containerMenu instanceof CraftConfirmMenu menu) {
            menu.setPlan(plan);
        }
    }
}
