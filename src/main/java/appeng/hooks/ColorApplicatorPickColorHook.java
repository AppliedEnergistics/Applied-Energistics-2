package appeng.hooks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.network.NetworkHandler;
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
            NetworkHandler.instance()
                    .sendToServer(new ColorApplicatorSelectColorPacket(colorableBlockEntity.getColor()));
            return true;
        }

        return false;
    }
}
