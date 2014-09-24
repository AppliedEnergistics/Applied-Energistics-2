package appeng.parts.misc;

import net.minecraft.item.ItemStack;

public class PartInvertedToggleBus extends PartToggleBus
{

	public PartInvertedToggleBus(ItemStack is) {
		super( PartInvertedToggleBus.class, is );
		proxy.setIdlePowerUsage( 0.0 );
		outerProxy.setIdlePowerUsage( 0.0 );
		proxy.setFlags();
		outerProxy.setFlags();
	}

	@Override
	protected boolean getIntention()
	{
		return !super.getIntention();
	}

}
