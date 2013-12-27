package appeng.parts.reporting;

import net.minecraft.item.ItemStack;
import appeng.client.texture.CableBusTextures;

public class PartConversionMonitor extends PartStorageMonitor
{

	public PartConversionMonitor(ItemStack is) {
		super( PartConversionMonitor.class, is );
		frontBright = CableBusTextures.PartConvMonitor_Bright;
		frontColored = CableBusTextures.PartConvMonitor_Colored;
		frontDark = CableBusTextures.PartConvMonitor_Dark;
		frontSolid = CableBusTextures.PartConvMonitor_Solid;
	}

}
