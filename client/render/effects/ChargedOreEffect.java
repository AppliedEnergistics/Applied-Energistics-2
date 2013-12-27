package appeng.client.render.effects;

import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.world.World;

public class ChargedOreEffect extends EntityReddustFX
{

	public ChargedOreEffect(World w, double x, double y, double z, float r, float g, float b) {
		super( w, x, y, z, 0.21f, 0.61f, 1.0f );

	}

	@Override
	public int getBrightnessForRender(float par1)
	{
		int j1 = super.getBrightnessForRender( par1 );
		j1 = Math.max( j1 >> 20, j1 >> 4 );
		j1 += 3;
		if ( j1 > 15 )
			j1 = 15;
		return j1 << 20 | j1 << 4;
	}

}
