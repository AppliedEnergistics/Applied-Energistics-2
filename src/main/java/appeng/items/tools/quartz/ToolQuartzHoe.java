package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.util.Platform;

public class ToolQuartzHoe extends ItemHoe implements IAEFeature
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

	public ToolQuartzHoe(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( type = Type, AEFeature.QuartzHoe ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}