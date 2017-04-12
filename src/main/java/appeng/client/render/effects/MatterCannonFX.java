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


import appeng.client.texture.ExtraBlockTextures;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


public class MatterCannonFX extends EntityBreakingFX
{

	private final IIcon particleTextureIndex;

	public MatterCannonFX( final World par1World, final double par2, final double par4, final double par6, final Item par8Item )
	{
		super( par1World, par2, par4, par6, par8Item );
		this.particleGravity = 0;
		this.particleBlue = 255;
		this.particleGreen = 255;
		this.particleRed = 255;
		this.particleAlpha = 1.4f;
		this.particleScale = 1.1f;
		this.motionX = 0.0f;
		this.motionY = 0.0f;
		this.motionZ = 0.0f;
		this.particleTextureIndex = ExtraBlockTextures.BlockMatterCannonParticle.getIcon();
		this.noClip = true;
	}

	public void fromItem( final ForgeDirection d )
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
	public int getFXLayer()
	{
		return 1;
	}

	@Override
	public void renderParticle( final Tessellator par1Tessellator, final float par2, final float par3, final float par4, final float par5, final float par6, final float par7 )
	{
		final float f6 = this.particleTextureIndex.getMinU();
		final float f7 = this.particleTextureIndex.getMaxU();
		final float f8 = this.particleTextureIndex.getMinV();
		final float f9 = this.particleTextureIndex.getMaxV();
		final float f10 = 0.05F * this.particleScale;

		final float f11 = (float) ( this.prevPosX + ( this.posX - this.prevPosX ) * par2 - interpPosX );
		final float f12 = (float) ( this.prevPosY + ( this.posY - this.prevPosY ) * par2 - interpPosY );
		final float f13 = (float) ( this.prevPosZ + ( this.posZ - this.prevPosZ ) * par2 - interpPosZ );
		final float f14 = 1.0F;

		par1Tessellator.setColorRGBA_F( this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha );
		par1Tessellator.addVertexWithUV( f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10, f7, f9 );
		par1Tessellator.addVertexWithUV( f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10, f7, f8 );
		par1Tessellator.addVertexWithUV( f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10, f6, f8 );
		par1Tessellator.addVertexWithUV( f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10, f6, f9 );
	}
}
