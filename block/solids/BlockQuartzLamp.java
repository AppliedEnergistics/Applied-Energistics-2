package appeng.block.solids;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;
import appeng.client.render.effects.VibrantFX;
import appeng.core.CommonHelper;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuartzLamp extends BlockQuartzGlass
{

	public BlockQuartzLamp() {
		super( BlockQuartzLamp.class );
		setfeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks, AEFeature.DecorativeLights ) );
		setLightLevel( 1.0f );
		setBlockTextureName( "BlockQuartzGlass" );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !AEConfig.instance.enableEffects )
			return;

		if ( CommonHelper.proxy.shouldAddParticles( r ) )
		{
			double d0 = (double) (r.nextFloat() - 0.5F) * 0.96D;
			double d1 = (double) (r.nextFloat() - 0.5F) * 0.96D;
			double d2 = (double) (r.nextFloat() - 0.5F) * 0.96D;

			VibrantFX fx = new VibrantFX( w, 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );

			Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
		}
	}

}
