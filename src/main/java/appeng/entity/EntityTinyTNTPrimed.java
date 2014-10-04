package appeng.entity;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.sync.packets.PacketMockExplosion;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

final public class EntityTinyTNTPrimed extends EntityTNTPrimed implements IEntityAdditionalSpawnData
{

	public EntityTinyTNTPrimed(World w) {
		super( w );
		this.setSize( 0.35F, 0.35F );
	}

	public EntityTinyTNTPrimed(World w, double x, double y, double z, EntityLivingBase igniter) {
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

		if ( this.onGround )
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}

		if ( this.isInWater() && Platform.isServer() ) // put out the fuse.
		{
			EntityItem item = new EntityItem( worldObj, this.posX, this.posY, this.posZ, AEApi.instance().blocks().blockTinyTNT.stack( 1 ) );
			item.motionX = motionX;
			item.motionY = motionY;
			item.motionZ = motionZ;
			item.prevPosX = this.prevPosX;
			item.prevPosY = this.prevPosY;
			item.prevPosZ = this.prevPosZ;
			worldObj.spawnEntityInWorld( item );
			this.setDead();
		}

		if ( this.fuse-- <= 0 )
		{
			this.setDead();

			if ( !this.worldObj.isRemote )
			{
				this.explode();
			}
		}
		else
		{
			this.worldObj.spawnParticle( "smoke", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D );
		}
	}

	// override :P
	void explode()
	{
		this.worldObj.playSoundEffect( this.posX, this.posY, this.posZ, "random.explode", 4.0F,
				(1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 32.9F );

		if ( this.isInWater() )
		{
			return;
		}

		for (Object e : this.worldObj.getEntitiesWithinAABBExcludingEntity( this,
				AxisAlignedBB.getBoundingBox( this.posX - 1.5, this.posY - 1.5f, this.posZ - 1.5, this.posX + 1.5, this.posY + 1.5, this.posZ + 1.5 ) ))
		{
			if ( e instanceof Entity )
			{
				((Entity) e).attackEntityFrom( DamageSource.setExplosionSource( null ), 6 );
			}

		}

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.TinyTNTBlockDamage ) )
		{
			posY -= 0.25;
			Explosion ex = new Explosion( worldObj, this, posX, posY, posZ, 0.2f );

			for (int x = (int) (posX - 2); x <= posX + 2; x++)
			{
				for (int y = (int) (posY - 2); y <= posY + 2; y++)
				{
					for (int z = (int) (posZ - 2); z <= posZ + 2; z++)
					{
						Block block = worldObj.getBlock( x, y, z );
						if ( block != null && !block.isAir( worldObj, x, y, z ) )
						{
							float strength = (float) (2.3f - (((x + 0.5f) - posX) * ((x + 0.5f) - posX) + ((y + 0.5f) - posY) * ((y + 0.5f) - posY) + ((z + 0.5f) - posZ)
									* ((z + 0.5f) - posZ)));

							float resistance = block.getExplosionResistance( this, worldObj, x, y, z, posX, posY, posZ );
							strength -= (resistance + 0.3F) * 0.11f;

							if ( strength > 0.01 )
							{
								if ( block.getMaterial() != Material.air )
								{
									if ( block.canDropFromExplosion( ex ) )
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

		CommonHelper.proxy.sendToAllNearExcept( null, posX, posY, posZ, 64, this.worldObj, new PacketMockExplosion( posX, posY, posZ ) );
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		data.writeByte( fuse );
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		fuse = data.readByte();
	}

}
