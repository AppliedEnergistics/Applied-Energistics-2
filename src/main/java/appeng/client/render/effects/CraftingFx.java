package appeng.client.render.effects;

import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.client.texture.ExtraBlockTextures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CraftingFx extends EntityBreakingFX
{

	private IIcon particleTextureIndex;

	private int startBlkX;
	private int startBlkY;
	private int startBlkZ;

	@Override
	public int getFXLayer()
	{
		return 1;
	}

	public CraftingFx(World par1World, double par2, double par4, double par6, Item par8Item) {
		super( par1World, par2, par4, par6, par8Item );
		particleGravity = 0;
		this.particleBlue = 1;
		this.particleGreen = 0.9f;
		this.particleRed = 1;
		this.particleAlpha = 1.3f;
		this.particleScale = 1.5f;
		this.particleTextureIndex = ExtraBlockTextures.BlockEnergyParticle.getIcon();
		particleMaxAge /= 1.2;

		startBlkX = MathHelper.floor_double( posX );
		startBlkY = MathHelper.floor_double( posY );
		startBlkZ = MathHelper.floor_double( posZ );
	}

	public void fromItem(ForgeDirection d)
	{
		this.posX += 0.2 * d.offsetX;
		this.posY += 0.2 * d.offsetY;
		this.posZ += 0.2 * d.offsetZ;
		this.particleScale *= 0.8f;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		this.particleScale *= 0.51f;
		this.particleAlpha *= 0.51f;
	}

	@Override
	public void renderParticle(Tessellator par1Tessellator, float partialTick, float x, float y, float z, float rx, float rz)
	{
		if ( partialTick < 0 || partialTick > 1 )
			return;

		float f6 = this.particleTextureIndex.getMinU();
		float f7 = this.particleTextureIndex.getMaxU();
		float f8 = this.particleTextureIndex.getMinV();
		float f9 = this.particleTextureIndex.getMaxV();
		float scale = 0.1F * this.particleScale;

		float offX = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTick);
		float offY = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTick);
		float offZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTick);
		float f14 = 1.0F;

		int blkX = MathHelper.floor_double( offX );
		int blkY = MathHelper.floor_double( offY );
		int blkZ = MathHelper.floor_double( offZ );
		if ( blkX == startBlkX && blkY == startBlkY && blkZ == startBlkZ )
		{
			offX -= interpPosX;
			offY -= interpPosY;
			offZ -= interpPosZ;

			// AELog.info( "" + partialTick );
			par1Tessellator.setColorRGBA_F( this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha );
			par1Tessellator.addVertexWithUV( offX - x * scale - rx * scale, offY - y * scale, offZ - z * scale - rz * scale,
					f7, f9 );
			par1Tessellator.addVertexWithUV( offX - x * scale + rx * scale, offY + y * scale, offZ - z * scale + rz * scale,
					f7, f8 );
			par1Tessellator.addVertexWithUV( offX + x * scale + rx * scale, offY + y * scale, offZ + z * scale + rz * scale,
					f6, f8 );
			par1Tessellator.addVertexWithUV( offX + x * scale - rx * scale, offY - y * scale, offZ + z * scale - rz * scale,
					f6, f9 );
		}
	}

}
