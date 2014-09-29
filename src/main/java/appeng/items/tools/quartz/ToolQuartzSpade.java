package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.util.Platform;

public class ToolQuartzSpade extends ItemSpade implements IAEFeature
{

	final AEFeature type;
	final AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	@Override
	public boolean getIsRepairable(ItemStack a, ItemStack b)
	{
		return Platform.canRepair( type, a, b );
	}

	public ToolQuartzSpade(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( type = Type, AEFeature.QuartzSpade ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
