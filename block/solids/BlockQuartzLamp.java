package appeng.block.solids;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;
import appeng.client.render.effects.VibrantEffect;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuartzLamp extends BlockQuartzGlass
{

	public BlockQuartzLamp() {
		super( BlockQuartzLamp.class );
		setfeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks, AEFeature.DecorativeLights ) );
		setLightValue( 1.0f );
		setTextureName( "BlockQuartzGlass" );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !Configuration.instance.enableEffects )
			return;

		double d0 = (double) (r.nextFloat() - 0.5F) * 0.96D;
		double d1 = (double) (r.nextFloat() - 0.5F) * 0.96D;
		double d2 = (double) (r.nextFloat() - 0.5F) * 0.96D;

		VibrantEffect fx = new VibrantEffect( w, 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );

		Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
	}

}
