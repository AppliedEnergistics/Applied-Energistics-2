package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemHoe;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class ToolQuartzHoe extends ItemHoe implements IAEFeature
{

	AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public ToolQuartzHoe(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzHoe ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}