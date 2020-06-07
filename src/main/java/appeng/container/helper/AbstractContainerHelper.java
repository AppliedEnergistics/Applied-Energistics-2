package appeng.container.helper;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

abstract class AbstractContainerHelper {

    private final SecurityPermissions requiredPermission;

    public AbstractContainerHelper(SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    protected boolean checkPermission(PlayerEntity player, Object accessInterface) {

        // FIXME: Check permissions...
        if (requiredPermission != null && accessInterface instanceof IActionHost)
        {
            final IGridNode gn = ( (IActionHost) accessInterface ).getActionableNode();
            if( gn != null )
            {
                final IGrid g = gn.getGrid();
                if( g != null )
                {
                    final boolean requirePower = false;
                    if( requirePower )
                    {
                        final IEnergyGrid eg = g.getCache( IEnergyGrid.class );
                        if( !eg.isNetworkPowered() )
                        {
                            // FIXME trace logging?
                            return false;
                        }
                    }

                    final ISecurityGrid sg = g.getCache( ISecurityGrid.class );
                    if( !sg.hasPermission( player, this.requiredPermission ) )
                    {
                        player.sendMessage(new TranslationTextComponent("appliedenergistics2.permission_denied")
                                .applyTextStyle(TextFormatting.RED));
                        // FIXME trace logging?
                        return false;
                    }
                }
            }
        }

        return true;

    }

}
