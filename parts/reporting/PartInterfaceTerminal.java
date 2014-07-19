package appeng.parts.reporting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

public class PartInterfaceTerminal extends PartMonitor
{

	public PartInterfaceTerminal(ItemStack is) {
		super( PartInterfaceTerminal.class, is, true );
		frontBright = CableBusTextures.PartInterfaceTerm_Bright;
		frontColored = CableBusTextures.PartInterfaceTerm_Colored;
		frontDark = CableBusTextures.PartInterfaceTerm_Dark;
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !super.onPartActivate( player, pos ) )
		{
			if ( !player.isSneaking() )
			{
				if ( Platform.isClient() )
					return true;

				Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_INTERFACE_TERMINAL );

				return true;
			}
		}

		return false;
	}
}
