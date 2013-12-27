package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemHoe;
import net.minecraftforge.common.MinecraftForge;
import appeng.core.Configuration;
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
		super( Configuration.instance.getItemID( ToolQuartzHoe.class, Type.name() ), EnumToolMaterial.IRON );
		MinecraftForge.setToolClass( this, "hoe", EnumToolMaterial.IRON.getHarvestLevel() );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzHoe ), this, Type.name() );
	}

}