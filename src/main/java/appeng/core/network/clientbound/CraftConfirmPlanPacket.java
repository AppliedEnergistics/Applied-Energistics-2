
package appeng.core.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;

/**
 * Transfers a {@link CraftingPlanSummary} to the client for a {@link CraftConfirmMenu}
 */
public record CraftConfirmPlanPacket(CraftingPlanSummary plan) implements ClientboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftConfirmPlanPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    CraftConfirmPlanPacket::write,
                    CraftConfirmPlanPacket::decode);

    public static final Type<CraftConfirmPlanPacket> TYPE = CustomAppEngPayload.createType("craft_confirm_plan");

    @Override
    public Type<CraftConfirmPlanPacket> type() {
        return TYPE;
    }

    public static CraftConfirmPlanPacket decode(RegistryFriendlyByteBuf data) {
        return new CraftConfirmPlanPacket(CraftingPlanSummary.read(data));
    }

    public void write(RegistryFriendlyByteBuf data) {
        plan.write(data);
    }
}
