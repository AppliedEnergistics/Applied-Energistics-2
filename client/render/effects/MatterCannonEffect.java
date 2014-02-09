package appeng.client.render.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.client.texture.ExtraTextures;

public class MatterCannonEffect extends EntityBreakingFX
{

	private IIcon particleTextureIndex;

	public MatterCannonEffect(World par1World, double par2, double par4, double par6, Item par8Item) {
		super( par1World, par2, par4, par6, par8Item );
		particleGravity = 0;
		this.particleBlue = 255;
		this.particleGreen = 255;
		this.particleRed = 255;
		this.particleAlpha = 1.4f;
		this.particleScale = 1.1f;
		this.motionX = 0.0f;
		this.motionY = 0.0f;
		this.motionZ = 0.0f;
		this.particleTextureIndex = ExtraTextures.BlockMatterCannonParticle.getIcon();
	}

	public void fromItem(ForgeDirection d)
	{
		this.particleScale *= 1.2f;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		this.particleScale *= 1.19f;
		this.particleAlpha *= 0.59f;
	}

	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.locationBlocksTexture );

		float f6 = this.particleTextureIndex.getMinU();
		float f7 = this.particleTextureIndex.getMaxU();
		float f8 = this.particleTextureIndex.getMinV();
		float f9 = this.particleTextureIndex.getMaxV();
		float f10 = 0.05F * this.particleScale;

		float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) par2 - interpPosX);
		float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) par2 - interpPosY);
		float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) par2 - interpPosZ);
		float f14 = 1.0F;

		par1Tessellator.setColorRGBA_F( this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha );
		par1Tessellator.addVertexWithUV( (double) (f11 - par3 * f10 - par6 * f10), (double) (f12 - par4 * f10), (double) (f13 - par5 * f10 - par7 * f10),
				(double) f7, (double) f9 );
		par1Tessellator.addVertexWithUV( (double) (f11 - par3 * f10 + par6 * f10), (double) (f12 + par4 * f10), (double) (f13 - par5 * f10 + par7 * f10),
				(double) f7, (double) f8 );
		par1Tessellator.addVertexWithUV( (double) (f11 + par3 * f10 + par6 * f10), (double) (f12 + par4 * f10), (double) (f13 + par5 * f10 + par7 * f10),
				(double) f6, (double) f8 );
		par1Tessellator.addVertexWithUV( (double) (f11 + par3 * f10 - par6 * f10), (double) (f12 - par4 * f10), (double) (f13 + par5 * f10 - par7 * f10),
				(double) f6, (double) f9 );
	}

}
