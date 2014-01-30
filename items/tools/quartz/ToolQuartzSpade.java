package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemSpade;
import net.minecraftforge.common.MinecraftForge;
import appeng.core.Configuration;
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
		super( Configuration.instance.getItemID( ToolQuartzSpade.class, Type.name() ), EnumToolMaterial.IRON );
		MinecraftForge.setToolClass( this, "shovel", EnumToolMaterial.IRON.getHarvestLevel() );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzSpade ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
