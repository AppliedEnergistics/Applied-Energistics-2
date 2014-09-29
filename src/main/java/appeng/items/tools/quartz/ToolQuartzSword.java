package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.util.Platform;

public class ToolQuartzSword extends ItemSword implements IAEFeature
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

	public ToolQuartzSword(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( type = Type, AEFeature.QuartzSword ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
