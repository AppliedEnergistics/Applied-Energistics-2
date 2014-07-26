package appeng.block.solids;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.client.render.effects.ChargedOreFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OreQuartzCharged extends OreQuartz
{

	public OreQuartzCharged() {
		super( OreQuartzCharged.class );
		boostBrightnessLow = 2;
		boostBrightnessHigh = 5;
	}

	@Override
	ItemStack getItemDropped()
	{
		return AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !AEConfig.instance.enableEffects )
			return;

		double xOff = (double) (r.nextFloat());
		double yOff = (double) (r.nextFloat());
		double zOff = (double) (r.nextFloat());

		switch (r.nextInt( 6 ))
		{
		case 0:
			xOff = -0.01;
			break;
		case 1:
			yOff = -0.01;
			break;
		case 2:
			xOff = -0.01;
			break;
		case 3:
			zOff = -0.01;
			break;
		case 4:
			xOff = 1.01;
			break;
		case 5:
			yOff = 1.01;
			break;
		case 6:
			zOff = 1.01;
			break;
		}

		if ( CommonHelper.proxy.shouldAddParticles( r ) )
		{
			ChargedOreFX fx = new ChargedOreFX( w, x + xOff, y + yOff, z + zOff, 0.0f, 0.0f, 0.0f );
			Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
		}
	}

}
