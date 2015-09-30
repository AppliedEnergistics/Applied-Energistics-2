/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.effects;


import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.util.AEPartLocation;
import appeng.client.texture.ExtraBlockTextures;


@SideOnly( Side.CLIENT )
public class EnergyFx extends EntityBreakingFX
{

	private final TextureAtlasSprite particleTextureIndex;

	private final int startBlkX;
	private final int startBlkY;
	private final int startBlkZ;

	public EnergyFx( World par1World, double par2, double par4, double par6, Item par8Item )
	{
		super( par1World, par2, par4, par6, par8Item );
		this.particleGravity = 0;
		this.particleBlue = 255;
		this.particleGreen = 255;
		this.particleRed = 255;
		this.particleAlpha = 1.4f;
		this.particleScale = 3.5f;
		this.particleTextureIndex = ExtraBlockTextures.BlockEnergyParticle.getIcon().getAtlas();

		this.startBlkX = MathHelper.floor_double( this.posX );
		this.startBlkY = MathHelper.floor_double( this.posY );
		this.startBlkZ = MathHelper.floor_double( this.posZ );

		this.noClip = true;
	}

	@Override
	public int getFXLayer()
	{
		return 1;
	}

	@Override
    public void renderParticle(WorldRenderer par1Tessellator, Entity p_180434_2_, float par2, float par3, float par4, float par5, float par6, float par7)
    {
		float f6 = this.particleTextureIndex.getMinU();
		float f7 = this.particleTextureIndex.getMaxU();
		float f8 = this.particleTextureIndex.getMinV();
		float f9 = this.particleTextureIndex.getMaxV();
		float f10 = 0.1F * this.particleScale;

		float f11 = (float) ( this.prevPosX + ( this.posX - this.prevPosX ) * par2 - interpPosX );
		float f12 = (float) ( this.prevPosY + ( this.posY - this.prevPosY ) * par2 - interpPosY );
		float f13 = (float) ( this.prevPosZ + ( this.posZ - this.prevPosZ ) * par2 - interpPosZ );

		int blkX = MathHelper.floor_double( this.posX );
		int blkY = MathHelper.floor_double( this.posY );
		int blkZ = MathHelper.floor_double( this.posZ );

		if( blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ )
		{
			float f14 = 1.0F;
			par1Tessellator.setColorRGBA_F( this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha );
			par1Tessellator.addVertexWithUV( f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10, f7, f9 );
			par1Tessellator.addVertexWithUV( f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10, f7, f8 );
			par1Tessellator.addVertexWithUV( f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10, f6, f8 );
			par1Tessellator.addVertexWithUV( f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10, f6, f9 );
		}
	}

	public void fromItem( AEPartLocation d )
	{
		this.posX += 0.2 * d.xOffset;
		this.posY += 0.2 * d.yOffset;
		this.posZ += 0.2 * d.zOffset;
		this.particleScale *= 0.8f;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		this.particleScale *= 0.89f;
		this.particleAlpha *= 0.89f;
	}
}
