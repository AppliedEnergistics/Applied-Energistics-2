package appeng.parts.reporting;

import net.minecraft.item.ItemStack;
import appeng.client.texture.CableBusTextures;

public class PartCraftingTerminal extends PartMonitor
{

	public PartCraftingTerminal(ItemStack is) {
		super( PartCraftingTerminal.class, is );
		frontBright = CableBusTextures.PartCraftingTerm_Bright;
		frontColored = CableBusTextures.PartCraftingTerm_Colored;
		frontDark = CableBusTextures.PartCraftingTerm_Dark;
		frontSolid = CableBusTextures.PartCraftingTerm_Solid;
	}

}
