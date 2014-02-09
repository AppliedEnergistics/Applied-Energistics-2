package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemPickaxe;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class ToolQuartzPickaxe extends ItemPickaxe implements IAEFeature
{

	AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public ToolQuartzPickaxe(AEFeature Type) {
		super( ToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzPickaxe ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
