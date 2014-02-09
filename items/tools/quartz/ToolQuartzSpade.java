package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemSpade;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class ToolQuartzSpade extends ItemSpade implements IAEFeature
{

	AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public ToolQuartzSpade(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzSpade ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
