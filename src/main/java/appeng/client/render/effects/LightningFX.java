package appeng.client.render.effects;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LightningFX extends EntityFX
{

	final int steps = getSteps();
	static Random rng = new Random();
	double[][] Steps;

	protected LightningFX(World w, double x, double y, double z, double r, double g, double b, int maxAge) {
		super( w, x, y, z, r, g, b );
		Steps = new double[steps][3];
		motionX = 0;
		motionY = 0;
		motionZ = 0;
		particleMaxAge = maxAge;
	}

	private int getSteps()
	{
		return 5;
	}

	public LightningFX(World w, double x, double y, double z, double r, double g, double b) {
		this( w, x, y, z, r, g, b, 6 );
		regen();
	}

	float currentPoint = 0;

	@Override
	public int getBrightnessForRender(float par1)
	{
		int j1 = 13;
		return j1 << 20 | j1 << 4;
	}

	protected void regen()
	{
		double lastDirectionX = (rng.nextDouble() - 0.5) * 0.9;
		double lastDirectionY = (rng.nextDouble() - 0.5) * 0.9;
		double lastDirectionZ = (rng.nextDouble() - 0.5) * 0.9;
		for (int s = 0; s < steps; s++)
		{
			Steps[s][0] = lastDirectionX = (lastDirectionX + (rng.nextDouble() - 0.5) * 0.9) / 2.0;
			Steps[s][1] = lastDirectionY = (lastDirectionY + (rng.nextDouble() - 0.5) * 0.9) / 2.0;
			Steps[s][2] = lastDirectionZ = (lastDirectionZ + (rng.nextDouble() - 0.5) * 0.9) / 2.0;
		}
	}

	@Override
	public void renderParticle(Tessellator tess, float l, float rX, float rY, float rZ, float rYZ, float rXY)
	{
		float j = 1.0f;
		tess.setColorRGBA_F( this.particleRed * j * 0.9f, this.particleGreen * j * 0.95f, this.particleBlue * j, this.particleAlpha );
		if ( particleAge == 3 )
		{
			regen();
		}
		double f6 = this.particleTextureIndexX / 16.0;
		double f7 = f6 + 0.0324375F;
		double f8 = this.particleTextureIndexY / 16.0;
		double f9 = f8 + 0.0324375F;

		f6 = f7;
		f8 = f9;

		double scale = 0.02;// 0.02F * this.particleScale;

		double a[] = new double[3];
		double b[] = new double[3];

		double ox = 0;
		double oy = 0;
		double oz = 0;

		EntityPlayer p = Minecraft.getMinecraft().thePlayer;
		double offX = -rZ;
		double offY = MathHelper.cos( (float) (Math.PI / 2.0f + p.rotationPitch * 0.017453292F) );
		double offZ = rX;

		for (int layer = 0; layer < 2; layer++)
		{
			if ( layer == 0 )
			{
				scale = 0.04;
				offX *= 0.001;
				offY *= 0.001;
				offZ *= 0.001;
				tess.setColorRGBA_F( this.particleRed * j * 0.4f, this.particleGreen * j * 0.25f, this.particleBlue * j * 0.45f, this.particleAlpha );
			}
			else
			{
				offX = 0;
				offY = 0;
				offZ = 0;
				scale = 0.02;
				tess.setColorRGBA_F( this.particleRed * j * 0.9f, this.particleGreen * j * 0.65f, this.particleBlue * j * 0.85f, this.particleAlpha );
			}

			for (int cycle = 0; cycle < 3; cycle++)
			{
				clear();

				double x = (this.prevPosX + (this.posX - this.prevPosX) * l - interpPosX) - offX;
				double y = (this.prevPosY + (this.posY - this.prevPosY) * l - interpPosY) - offY;
				double z = (this.prevPosZ + (this.posZ - this.prevPosZ) * l - interpPosZ) - offZ;

				for (int s = 0; s < steps; s++)
				{
					double xN = x + Steps[s][0];
					double yN = y + Steps[s][1];
					double zN = z + Steps[s][2];

					double xD = xN - x;
					double yD = yN - y;
					double zD = zN - z;

					if ( cycle == 0 )
					{
						ox = (yD * 0) - (1 * zD);
						oy = (zD * 0) - (0 * xD);
						oz = (xD * 1) - (0 * yD);
					}
					if ( cycle == 1 )
					{
						ox = (yD * 1) - (0 * zD);
						oy = (zD * 0) - (1 * xD);
						oz = (xD * 0) - (0 * yD);
					}
					if ( cycle == 2 )
					{
						ox = (yD * 0) - (0 * zD);
						oy = (zD * 1) - (0 * xD);
						oz = (xD * 0) - (1 * yD);
					}

					double ss = Math.sqrt( ox * ox + oy * oy + oz * oz ) / ((((double) steps - (double) s) / steps) * scale);
					ox /= ss;
					oy /= ss;
					oz /= ss;

					a[0] = x + ox;
					a[1] = y + oy;
					a[2] = z + oz;

					b[0] = x;
					b[1] = y;
					b[2] = z;

					draw( tess, a, b, f6, f8 );

					x = xN;
					y = yN;
					z = zN;
				}
			}
		}
		/*
		 * GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS ); GL11.glDisable( GL11.GL_CULL_FACE ); tess.draw();
		 * GL11.glPopAttrib(); tess.startDrawingQuads();
		 */
	}

	boolean hasData = false;
	double[] I = new double[3];
	double[] K = new double[3];

	private void draw(Tessellator tess, double[] a, double[] b, double f6, double f8)
	{
		if ( hasData )
		{
			tess.addVertexWithUV( a[0], a[1], a[2], f6, f8 );
			tess.addVertexWithUV( I[0], I[1], I[2], f6, f8 );
			tess.addVertexWithUV( K[0], K[1], K[2], f6, f8 );
			tess.addVertexWithUV( b[0], b[1], b[2], f6, f8 );
		}
		hasData = true;
		for (int x = 0; x < 3; x++)
		{
			I[x] = a[x];
			K[x] = b[x];
		}
	}

	private void clear()
	{
		hasData = false;
	}
}
