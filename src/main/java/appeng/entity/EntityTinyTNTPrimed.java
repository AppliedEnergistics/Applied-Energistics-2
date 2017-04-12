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

package appeng.entity;


import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.sync.packets.PacketMockExplosion;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;


public final class EntityTinyTNTPrimed extends EntityTNTPrimed implements IEntityAdditionalSpawnData
{
	@Reflected
	public EntityTinyTNTPrimed( final World w )
	{
		super( w );
		this.setSize( 0.35F, 0.35F );
	}

	public EntityTinyTNTPrimed( final World w, final double x, final double y, final double z, final EntityLivingBase igniter )
	{
		super( w, x, y, z, igniter );
		this.setSize( 0.55F, 0.55F );
		this.yOffset = this.height / 2.0F;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		this.handleWaterMovement();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.03999999910593033D;
		this.moveEntity( this.motionX, this.motionY, this.motionZ );
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if( this.onGround )
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}

		if( this.isInWater() && Platform.isServer() ) // put out the fuse.
		{
			for( final ItemStack tntStack : AEApi.instance().definitions().blocks().tinyTNT().maybeStack( 1 ).asSet() )
			{
				final EntityItem item = new EntityItem( this.worldObj, this.posX, this.posY, this.posZ, tntStack );

				item.motionX = this.motionX;
				item.motionY = this.motionY;
				item.motionZ = this.motionZ;
				item.prevPosX = this.prevPosX;
				item.prevPosY = this.prevPosY;
				item.prevPosZ = this.prevPosZ;

				this.worldObj.spawnEntityInWorld( item );
				this.setDead();
			}
		}

		if( this.fuse <= 0 )
		{
			this.setDead();

			if( !this.worldObj.isRemote )
			{
				this.explode();
			}
		}
		else
		{
			this.worldObj.spawnParticle( "smoke", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D );
		}
		this.fuse--;
	}

	// override :P
	void explode()
	{
		this.worldObj.playSoundEffect( this.posX, this.posY, this.posZ, "random.explode", 4.0F, ( 1.0F + ( this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat() ) * 0.2F ) * 32.9F );

		if( this.isInWater() )
		{
			return;
		}

		for( final Object e : this.worldObj.getEntitiesWithinAABBExcludingEntity( this, AxisAlignedBB.getBoundingBox( this.posX - 1.5, this.posY - 1.5f, this.posZ - 1.5, this.posX + 1.5, this.posY + 1.5, this.posZ + 1.5 ) ) )
		{
			if( e instanceof Entity )
			{
				( (Entity) e ).attackEntityFrom( DamageSource.setExplosionSource( null ), 6 );
			}
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.TinyTNTBlockDamage ) )
		{
			this.posY -= 0.25;
			final Explosion ex = new Explosion( this.worldObj, this, this.posX, this.posY, this.posZ, 0.2f );

			for( int x = (int) ( this.posX - 2 ); x <= this.posX + 2; x++ )
			{
				for( int y = (int) ( this.posY - 2 ); y <= this.posY + 2; y++ )
				{
					for( int z = (int) ( this.posZ - 2 ); z <= this.posZ + 2; z++ )
					{
						final Block block = this.worldObj.getBlock( x, y, z );
						if( block != null && !block.isAir( this.worldObj, x, y, z ) )
						{
							float strength = (float) ( 2.3f - ( ( ( x + 0.5f ) - this.posX ) * ( ( x + 0.5f ) - this.posX ) + ( ( y + 0.5f ) - this.posY ) * ( ( y + 0.5f ) - this.posY ) + ( ( z + 0.5f ) - this.posZ ) * ( ( z + 0.5f ) - this.posZ ) ) );

							final float resistance = block.getExplosionResistance( this, this.worldObj, x, y, z, this.posX, this.posY, this.posZ );
							strength -= ( resistance + 0.3F ) * 0.11f;

							if( strength > 0.01 )
							{
								if( block.getMaterial() != Material.air )
								{
									if( block.canDropFromExplosion( ex ) )
									{
										block.dropBlockAsItemWithChance( this.worldObj, x, y, z, this.worldObj.getBlockMetadata( x, y, z ), 1.0F / 1.0f, 0 );
									}

									block.onBlockExploded( this.worldObj, x, y, z, ex );
								}
							}
						}
					}
				}
			}
		}

		CommonHelper.proxy.sendToAllNearExcept( null, this.posX, this.posY, this.posZ, 64, this.worldObj, new PacketMockExplosion( this.posX, this.posY, this.posZ ) );
	}

	@Override
	public void writeSpawnData( final ByteBuf data )
	{
		data.writeByte( this.fuse );
	}

	@Override
	public void readSpawnData( final ByteBuf data )
	{
		this.fuse = data.readByte();
	}
}
