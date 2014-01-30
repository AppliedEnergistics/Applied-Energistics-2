package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemPickaxe;
import net.minecraftforge.common.MinecraftForge;
import appeng.core.Configuration;
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
		super( Configuration.instance.getItemID( ToolQuartzPickaxe.class, Type.name() ), EnumToolMaterial.IRON );
		MinecraftForge.setToolClass( this, "pickaxe", EnumToolMaterial.IRON.getHarvestLevel() );
		feature = new AEFeatureHandler( EnumSet.of( Type, AEFeature.QuartzPickaxe ), this, Type.name() );
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
