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

package appeng.items.tools.powered;


import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.hooks.DispenserMatterCannon;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.ItemPaintBall;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.misc.TilePaint;
import appeng.util.LookDirection;
import appeng.util.Platform;


public class ToolMassCannon extends AEBasePoweredItem implements IStorageCell
{

	public ToolMassCannon()
	{
		super( AEConfig.instance.matterCannonBattery, Optional.<String>absent() );
		this.setFeature( EnumSet.of( AEFeature.MatterCannon, AEFeature.PoweredTools ) );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserMatterCannon() );
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		final IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			final ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack item, final World w, final EntityPlayer p )
	{
		if( this.getAECurrentPower( item ) > 1600 )
		{
			int shots = 1;

			final CellUpgrades cu = (CellUpgrades) this.getUpgradesInventory( item );
			if( cu != null )
			{
				shots += cu.getInstalledUpgrades( Upgrades.SPEED );
			}

			final IMEInventory inv = AEApi.instance().registries().cell().getCellInventory( item, null, StorageChannel.ITEMS );
			if( inv != null )
			{
				final IItemList itemList = inv.getAvailableItems( AEApi.instance().storage().createItemList() );
				IAEStack aeAmmo = itemList.getFirstItem();
				if( aeAmmo instanceof IAEItemStack )
				{
					shots = Math.min( shots, (int) aeAmmo.getStackSize() );
					for( int sh = 0; sh < shots; sh++ )
					{
						this.extractAEPower( item, 1600 );

						if( Platform.isClient() )
						{
							return item;
						}

						aeAmmo.setStackSize( 1 );
						final ItemStack ammo = ( (IAEItemStack) aeAmmo ).getItemStack();
						if( ammo == null )
						{
							return item;
						}

						ammo.stackSize = 1;
						aeAmmo = inv.extractItems( aeAmmo, Actionable.MODULATE, new PlayerSource( p, null ) );
						if( aeAmmo == null )
						{
							return item;
						}
						
						final LookDirection dir = Platform.getPlayerRay( p, p.getEyeHeight() );

						final Vec3 vec3 = dir.a;
						final Vec3 vec31 = dir.b;
						final Vec3 direction = vec31.subtract( vec3 );
						direction.normalize();

						final double d0 = vec3.xCoord;
						final double d1 = vec3.yCoord;
						final double d2 = vec3.zCoord;
						
						final float penetration = AEApi.instance().registries().matterCannon().getPenetration( ammo ); // 196.96655f;
						if( penetration <= 0 )
						{
							final ItemStack type = ( (IAEItemStack) aeAmmo ).getItemStack();
							if( type.getItem() instanceof ItemPaintBall )
							{
								this.shootPaintBalls( type, w, p, vec3, vec31, direction, d0, d1, d2 );
							}
							return item;
						}
						else
						{
							this.standardAmmo( penetration, w, p, vec3, vec31, direction, d0, d1, d2 );
						}
					}
				}
				else
				{
					if( Platform.isServer() )
					{
						p.addChatMessage( PlayerMessages.AmmoDepleted.get() );
					}
					return item;
				}
			}
		}
		return item;
	}

	private void shootPaintBalls( final ItemStack type, final World w, final EntityPlayer p, final Vec3 vec3, final Vec3 vec31, final Vec3 direction, final double d0, final double d1, final double d2 )
	{
		final AxisAlignedBB bb = AxisAlignedBB.fromBounds( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ), Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

		Entity entity = null;
		final List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
		double closest = 9999999.0D;

		for( int l = 0; l < list.size(); ++l )
		{
			final Entity entity1 = (Entity) list.get( l );

			if( !entity1.isDead && entity1 != p && !( entity1 instanceof EntityItem ) )
			{
				if( entity1.isEntityAlive() )
				{
					// prevent killing / flying of mounts.
					if( entity1.riddenByEntity == p )
					{
						continue;
					}

					final float f1 = 0.3F;

					final AxisAlignedBB boundingBox = entity1.getEntityBoundingBox().expand( f1, f1, f1 );
					final MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

					if( movingObjectPosition != null )
					{
						final double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

						if( nd < closest )
						{
							entity = entity1;
							closest = nd;
						}
					}
				}
			}
		}

		MovingObjectPosition pos = w.rayTraceBlocks( vec3, vec31, false );

		final Vec3 vec = new Vec3( d0, d1, d2 );
		if( entity != null && pos != null && pos.hitVec.squareDistanceTo( vec ) > closest )
		{
			pos = new MovingObjectPosition( entity );
		}
		else if( entity != null && pos == null )
		{
			pos = new MovingObjectPosition( entity );
		}

		try
		{
			CommonHelper.proxy.sendToAllNearExcept( null, d0, d1, d2, 128, w, new PacketMatterCannon( d0, d1, d2, (float) direction.xCoord, (float) direction.yCoord, (float) direction.zCoord, (byte) ( pos == null ? 32 : pos.hitVec.squareDistanceTo( vec ) + 1 ) ) );
		}
		catch( final Exception err )
		{
			AELog.error( err );
		}

		if( pos != null && type != null && type.getItem() instanceof ItemPaintBall )
		{
			final ItemPaintBall ipb = (ItemPaintBall) type.getItem();

			final AEColor col = ipb.getColor( type );
			// boolean lit = ipb.isLumen( type );

			if( pos.typeOfHit == MovingObjectType.ENTITY )
			{
				final int id = pos.entityHit.getEntityId();
				final PlayerColor marker = new PlayerColor( id, col, 20 * 30 );
				TickHandler.INSTANCE.getPlayerColors().put( id, marker );

				if( pos.entityHit instanceof EntitySheep )
				{
					final EntitySheep sh = (EntitySheep) pos.entityHit;
					sh.setFleeceColor( col.dye );
				}

				pos.entityHit.attackEntityFrom( DamageSource.causePlayerDamage( p ), 0 );
				NetworkHandler.instance.sendToAll( marker.getPacket() );
			}
			else if( pos.typeOfHit == MovingObjectType.BLOCK )
			{
				final EnumFacing side = pos.sideHit;
				final BlockPos hitPos = pos.getBlockPos().offset( side );

				if( !Platform.hasPermissions( new DimensionalCoord( w, hitPos ), p ) )
				{
					return;
				}

				final Block whatsThere = w.getBlockState( hitPos ).getBlock();
				if( whatsThere.isReplaceable( w, hitPos ) && w.isAirBlock( hitPos ) )
				{
					for( final Block paintBlock : AEApi.instance().definitions().blocks().paint().maybeBlock().asSet() )
					{
						w.setBlockState( hitPos, paintBlock.getDefaultState(), 3 );
					}
				}

				final TileEntity te = w.getTileEntity( hitPos );
				if( te instanceof TilePaint )
				{
					final Vec3 hp = pos.hitVec.subtract(  hitPos.getX(), hitPos.getY(), hitPos.getZ() );
					( (TilePaint) te ).addBlot( type, side.getOpposite(), hp  );
				}
			}
		}
	}

	private void standardAmmo( float penetration, final World w, final EntityPlayer p, final Vec3 vec3, final Vec3 vec31, final Vec3 direction, final double d0, final double d1, final double d2 )
	{
		boolean hasDestroyed = true;
		while( penetration > 0 && hasDestroyed )
		{
			hasDestroyed = false;

			final AxisAlignedBB bb = AxisAlignedBB.fromBounds( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ), Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

			Entity entity = null;
			final List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
			double closest = 9999999.0D;

			for( int l = 0; l < list.size(); ++l )
			{
				final Entity entity1 = (Entity) list.get( l );

				if( !entity1.isDead && entity1 != p && !( entity1 instanceof EntityItem ) )
				{
					if( entity1.isEntityAlive() )
					{
						// prevent killing / flying of mounts.
						if( entity1.riddenByEntity == p )
						{
							continue;
						}

						final float f1 = 0.3F;

						final AxisAlignedBB boundingBox = entity1.getEntityBoundingBox().expand( f1, f1, f1 );
						final MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

						if( movingObjectPosition != null )
						{
							final double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

							if( nd < closest )
							{
								entity = entity1;
								closest = nd;
							}
						}
					}
				}
			}

			final Vec3 vec = new Vec3( d0, d1, d2 );
			MovingObjectPosition pos = w.rayTraceBlocks( vec3, vec31, true );
			if( entity != null && pos != null && pos.hitVec.squareDistanceTo( vec ) > closest )
			{
				pos = new MovingObjectPosition( entity );
			}
			else if( entity != null && pos == null )
			{
				pos = new MovingObjectPosition( entity );
			}

			try
			{
				CommonHelper.proxy.sendToAllNearExcept( null, d0, d1, d2, 128, w, new PacketMatterCannon( d0, d1, d2, (float) direction.xCoord, (float) direction.yCoord, (float) direction.zCoord, (byte) ( pos == null ? 32 : pos.hitVec.squareDistanceTo( vec ) + 1 ) ) );
			}
			catch( final Exception err )
			{
				AELog.error( err );
			}

			if( pos != null )
			{
				final DamageSource dmgSrc = DamageSource.causePlayerDamage( p );
				dmgSrc.damageType = "masscannon";

				if( pos.typeOfHit == MovingObjectType.ENTITY )
				{
					final int dmg = (int) Math.ceil( penetration / 20.0f );
					if( pos.entityHit instanceof EntityLivingBase )
					{
						final EntityLivingBase el = (EntityLivingBase) pos.entityHit;
						penetration -= dmg;
						el.knockBack( p, 0, -direction.xCoord, -direction.zCoord );
						// el.knockBack( p, 0, vec3.xCoord,
						// vec3.zCoord );
						el.attackEntityFrom( dmgSrc, dmg );
						if( !el.isEntityAlive() )
						{
							hasDestroyed = true;
						}
					}
					else if( pos.entityHit instanceof EntityItem )
					{
						hasDestroyed = true;
						pos.entityHit.setDead();
					}
					else if( pos.entityHit.attackEntityFrom( dmgSrc, dmg ) )
					{
						hasDestroyed = true;
					}
				}
				else if( pos.typeOfHit == MovingObjectType.BLOCK )
				{
					if( !AEConfig.instance.isFeatureEnabled( AEFeature.MassCannonBlockDamage ) )
					{
						penetration = 0;
					}
					else
					{
						final Block b = w.getBlockState( pos.getBlockPos() ).getBlock();
						// int meta = w.getBlockMetadata(
						// pos.blockX, pos.blockY, pos.blockZ );

						final float hardness = b.getBlockHardness( w, pos.getBlockPos() ) * 9.0f;
						if( hardness >= 0.0 )
						{
							if( penetration > hardness && Platform.hasPermissions( new DimensionalCoord( w, pos.getBlockPos() ), p ) )
							{
								hasDestroyed = true;
								penetration -= hardness;
								penetration *= 0.60;
								w.destroyBlock( pos.getBlockPos(), true );
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 4 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return 512;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 1;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		final float pen = AEApi.instance().registries().matterCannon().getPenetration( requestedAddition.getItemStack() );
		if( pen > 0 )
		{
			return false;
		}

		if( requestedAddition.getItem() instanceof ItemPaintBall )
		{
			return false;
		}

		return true;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return true;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}
}
