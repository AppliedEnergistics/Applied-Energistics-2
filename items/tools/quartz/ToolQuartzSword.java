package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemSword;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class ToolQuartzSword extends ItemSword implements IAEFeature
{

	AEFeatureHandler feature;

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public ToolQuartzSword(AEFeature Type) {
		super( Configuration.instance.getItemID( ToolQuartzSword.class, Type.name() ), EnumToolMaterial.IRON );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzSword ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
