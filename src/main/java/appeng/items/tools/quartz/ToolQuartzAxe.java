package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.util.Platform;

public class ToolQuartzAxe extends ItemAxe implements IAEFeature
{

	final AEFeature type;
	final AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public boolean getIsRepairable(ItemStack a, ItemStack b)
	{
		return Platform.canRepair( type, a, b );
	}

	public ToolQuartzAxe(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( type = Type, AEFeature.QuartzAxe ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
