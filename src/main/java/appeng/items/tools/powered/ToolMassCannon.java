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
import net.minecraft.util.MathHelper;
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

import com.google.common.base.Optional;


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
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public ItemStack onItemRightClick( ItemStack item, World w, EntityPlayer p )
	{
		if( this.getAECurrentPower( item ) > 1600 )
		{
			int shots = 1;

			CellUpgrades cu = (CellUpgrades) this.getUpgradesInventory( item );
			if( cu != null )
			{
				shots += cu.getInstalledUpgrades( Upgrades.SPEED );
			}

			IMEInventory inv = AEApi.instance().registries().cell().getCellInventory( item, null, StorageChannel.ITEMS );
			if( inv != null )
			{
				IItemList itemList = inv.getAvailableItems( AEApi.instance().storage().createItemList() );
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
						ItemStack ammo = ( (IAEItemStack) aeAmmo ).getItemStack();
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
						
						LookDirection dir = Platform.getPlayerRay( p, p.getEyeHeight() );

						Vec3 vec3 = dir.a;
						Vec3 vec31 = dir.b;
						Vec3 direction = vec31.subtract( vec3 );
						direction.normalize();

						double d0 = vec3.xCoord;
						double d1 = vec3.yCoord;
						double d2 = vec3.zCoord;
						
						float penetration = AEApi.instance().registries().matterCannon().getPenetration( ammo ); // 196.96655f;
						if( penetration <= 0 )
						{
							ItemStack type = ( (IAEItemStack) aeAmmo ).getItemStack();
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

	private void shootPaintBalls( ItemStack type, World w, EntityPlayer p, Vec3 vec3, Vec3 vec31, Vec3 direction, double d0, double d1, double d2 )
	{
		AxisAlignedBB bb = AxisAlignedBB.fromBounds( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ), Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

		Entity entity = null;
		List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
		double closest = 9999999.0D;
		int l;

		for( l = 0; l < list.size(); ++l )
		{
			Entity entity1 = (Entity) list.get( l );

			if( !entity1.isDead && entity1 != p && !( entity1 instanceof EntityItem ) )
			{
				if( entity1.isEntityAlive() )
				{
					// prevent killing / flying of mounts.
					if( entity1.riddenByEntity == p )
					{
						continue;
					}

					float f1 = 0.3F;

					AxisAlignedBB boundingBox = entity1.getBoundingBox().expand( f1, f1, f1 );
					MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

					if( movingObjectPosition != null )
					{
						double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

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

		Vec3 vec = new Vec3( d0, d1, d2 );
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
		catch( Exception err )
		{
			AELog.error( err );
		}

		if( pos != null && type != null && type.getItem() instanceof ItemPaintBall )
		{
			ItemPaintBall ipb = (ItemPaintBall) type.getItem();

			AEColor col = ipb.getColor( type );
			// boolean lit = ipb.isLumen( type );

			if( pos.typeOfHit == MovingObjectType.ENTITY )
			{
				int id = pos.entityHit.getEntityId();
				PlayerColor marker = new PlayerColor( id, col, 20 * 30 );
				TickHandler.INSTANCE.getPlayerColors().put( id, marker );

				if( pos.entityHit instanceof EntitySheep )
				{
					EntitySheep sh = (EntitySheep) pos.entityHit;
					sh.setFleeceColor( col.dye );
				}

				pos.entityHit.attackEntityFrom( DamageSource.causePlayerDamage( p ), 0 );
				NetworkHandler.instance.sendToAll( marker.getPacket() );
			}
			else if( pos.typeOfHit == MovingObjectType.BLOCK )
			{
				EnumFacing side = pos.sideHit;				
				BlockPos hitPos = pos.getBlockPos().offset( side );

				if( !Platform.hasPermissions( new DimensionalCoord( w, hitPos ), p ) )
				{
					return;
				}

				Block whatsThere = w.getBlockState( hitPos ).getBlock();
				if( whatsThere.isReplaceable( w, hitPos ) && w.isAirBlock( hitPos ) )
				{
					for( Block paintBlock : AEApi.instance().definitions().blocks().paint().maybeBlock().asSet() )
					{
						w.setBlockState( hitPos, paintBlock.getDefaultState(), 3 );
					}
				}

				TileEntity te = w.getTileEntity( hitPos );
				if( te instanceof TilePaint )
				{
					Vec3 hp = pos.hitVec.subtract(  hitPos.getX(), hitPos.getY(), hitPos.getZ() );
					( (TilePaint) te ).addBlot( type, side.getOpposite(), hp  );
				}
			}
		}
	}

	private void standardAmmo( float penetration, World w, EntityPlayer p, Vec3 vec3, Vec3 vec31, Vec3 direction, double d0, double d1, double d2 )
	{
		boolean hasDestroyed = true;
		while( penetration > 0 && hasDestroyed )
		{
			hasDestroyed = false;

			AxisAlignedBB bb = AxisAlignedBB.fromBounds( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ), Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

			Entity entity = null;
			List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
			double closest = 9999999.0D;
			int l;

			for( l = 0; l < list.size(); ++l )
			{
				Entity entity1 = (Entity) list.get( l );

				if( !entity1.isDead && entity1 != p && !( entity1 instanceof EntityItem ) )
				{
					if( entity1.isEntityAlive() )
					{
						// prevent killing / flying of mounts.
						if( entity1.riddenByEntity == p )
						{
							continue;
						}

						float f1 = 0.3F;

						AxisAlignedBB boundingBox = entity1.getBoundingBox().expand( f1, f1, f1 );
						MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

						if( movingObjectPosition != null )
						{
							double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

							if( nd < closest )
							{
								entity = entity1;
								closest = nd;
							}
						}
					}
				}
			}

			Vec3 vec = new Vec3( d0, d1, d2 );
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
			catch( Exception err )
			{
				AELog.error( err );
			}

			if( pos != null )
			{
				DamageSource dmgSrc = DamageSource.causePlayerDamage( p );
				dmgSrc.damageType = "masscannon";

				if( pos.typeOfHit == MovingObjectType.ENTITY )
				{
					int dmg = (int) Math.ceil( penetration / 20.0f );
					if( pos.entityHit instanceof EntityLivingBase )
					{
						EntityLivingBase el = (EntityLivingBase) pos.entityHit;
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
						Block b = w.getBlockState( pos.getBlockPos() ).getBlock();
						// int meta = w.getBlockMetadata(
						// pos.blockX, pos.blockY, pos.blockZ );

						float hardness = b.getBlockHardness( w, pos.getBlockPos() ) * 9.0f;
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
	public boolean isEditable( ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( ItemStack is )
	{
		return new CellUpgrades( is, 4 );
	}

	@Override
	public IInventory getConfigInventory( ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( ItemStack is )
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( ItemStack is, FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public int getBytes( ItemStack cellItem )
	{
		return 512;
	}

	@Override
	public int BytePerType( ItemStack cell )
	{
		return 8;
	}

	@Override
	public int getBytesPerType( ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( ItemStack cellItem )
	{
		return 1;
	}

	@Override
	public boolean isBlackListed( ItemStack cellItem, IAEItemStack requestedAddition )
	{
		float pen = AEApi.instance().registries().matterCannon().getPenetration( requestedAddition.getItemStack() );
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
	public boolean isStorageCell( ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}
}
