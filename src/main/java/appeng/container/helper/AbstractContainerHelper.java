package appeng.container.helper;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

abstract class AbstractContainerHelper {

    private final SecurityPermissions requiredPermission;

    public AbstractContainerHelper(SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    protected boolean checkPermission(PlayerEntity player, Object accessInterface) {

        if (requiredPermission != null)
        {
            return Platform.checkPermissions(player, accessInterface, requiredPermission, true);
        }

        return true;

    }

}
