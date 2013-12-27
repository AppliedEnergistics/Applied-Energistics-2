package appeng.parts.reporting;

import net.minecraft.item.ItemStack;
import appeng.client.texture.CableBusTextures;

public class PartPatternTerminal extends PartMonitor
{

	public PartPatternTerminal(ItemStack is) {
		super( PartPatternTerminal.class, is );
		frontBright = CableBusTextures.PartPatternTerm_Bright;
		frontColored = CableBusTextures.PartPatternTerm_Colored;
		frontDark = CableBusTextures.PartPatternTerm_Dark;
		frontSolid = CableBusTextures.PartPatternTerm_Solid;
	}

}
