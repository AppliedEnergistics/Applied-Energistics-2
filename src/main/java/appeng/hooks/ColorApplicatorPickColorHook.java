package appeng.hooks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.ColorApplicatorSelectColorPacket;

public final class ColorApplicatorPickColorHook {
    private ColorApplicatorPickColorHook() {
    }

    public static boolean onPickColor(Player player, BlockHitResult hitResult) {
        if (!AEItems.COLOR_APPLICATOR.isSameAs(player.getOffhandItem())
                && !AEItems.COLOR_APPLICATOR.isSameAs(player.getMainHandItem())) {
            return false;
        }

        // Figure out which color...
        var be = player.level().getBlockEntity(hitResult.getBlockPos());
        if (be instanceof IColorableBlockEntity colorableBlockEntity) {
            ServerboundPacket message = new ColorApplicatorSelectColorPacket(colorableBlockEntity.getColor());
            PacketDistributor.sendToServer(message);
            return true;
        }

        return false;
    }
}
