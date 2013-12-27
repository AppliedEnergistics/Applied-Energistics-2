package appeng.parts.misc;

import net.minecraft.item.ItemStack;

public class PartInvertedToggleBus extends PartToggleBus
{

	public PartInvertedToggleBus(ItemStack is) {
		super( PartInvertedToggleBus.class, is );
	}

	@Override
	protected boolean getIntention()
	{
		return !super.getIntention();
	}

}
