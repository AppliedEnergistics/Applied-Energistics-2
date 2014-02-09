package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemAxe;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class ToolQuartzAxe extends ItemAxe implements IAEFeature
{

	AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public ToolQuartzAxe(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzAxe ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
