package appeng.parts.reporting;

import net.minecraft.item.ItemStack;
import appeng.client.texture.CableBusTextures;

public class PartCraftingMonitor extends PartMonitor
{

	public PartCraftingMonitor(ItemStack is) {
		super( PartCraftingMonitor.class, is,true );
		frontBright = CableBusTextures.PartCraftingMonitor_Bright;
		frontColored = CableBusTextures.PartCraftingMonitor_Colored;
		frontDark = CableBusTextures.PartCraftingMonitor_Dark;
		// frontSolid = CableBusTextures.PartCraftingMonitor_Solid;
	}

}
