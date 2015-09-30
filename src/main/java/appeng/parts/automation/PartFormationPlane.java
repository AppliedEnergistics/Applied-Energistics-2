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

package appeng.parts.automation;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.CableBusTextures;
import appeng.core.AEConfig;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;


public class PartFormationPlane extends PartUpgradeable implements ICellContainer, IPriorityHost, IMEInventory<IAEItemStack>
{
	final MEInventoryHandler myHandler = new MEInventoryHandler( this, StorageChannel.ITEMS );
	final AppEngInternalAEInventory Config = new AppEngInternalAEInventory( this, 63 );
	int priority = 0;
	boolean wasActive = false;
	boolean blocked = false;

	public PartFormationPlane( ItemStack is )
	{
		super( is );

		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.PLACE_BLOCK, YesNo.YES );
		this.updateHandler();
	}

	private void updateHandler()
	{
		this.myHandler.setBaseAccess( AccessRestriction.WRITE );
		this.myHandler.setWhitelist( this.getInstalledUpgrades( Upgrades.INVERTER ) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST );
		this.myHandler.setPriority( this.priority );

		IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

		int slotsToUse = 18 + this.getInstalledUpgrades( Upgrades.CAPACITY ) * 9;
		for( int x = 0; x < this.Config.getSizeInventory() && x < slotsToUse; x++ )
		{
			IAEItemStack is = this.Config.getAEStackInSlot( x );
			if( is != null )
			{
				priorityList.add( is );
			}
		}

		if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
		{
			this.myHandler.setPartitionList( new FuzzyPriorityList( priorityList, (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE ) ) );
		}
		else
		{
			this.myHandler.setPartitionList( new PrecisePriorityList( priorityList ) );
		}

		try
		{
			this.proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch( GridAccessException e )
		{
			// :P
		}
	}

	@Override
	protected int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		this.updateHandler();
		this.host.markForSave();
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		super.onChangeInventory( inv, slot, mc, removedStack, newStack );

		if( inv == this.Config )
		{
			this.updateHandler();
		}
	}

	@Override
	public void upgradesChanged()
	{
		this.updateHandler();
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.Config.readFromNBT( data, "config" );
		this.priority = data.getInteger( "priority" );
		this.updateHandler();
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.Config.writeToNBT( data, "config" );
		data.setInteger( "priority", this.priority );
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		if( name.equals( "config" ) )
		{
			return this.Config;
		}

		return super.getInventoryByName( name );
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		boolean currentActive = this.proxy.isActive();
		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			this.updateHandler();// proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			this.getHost().markForUpdate();
		}
	}

	@MENetworkEventSubscribe
	public void updateChannels( MENetworkChannelsChanged changedChannels )
	{
		boolean currentActive = this.proxy.isActive();
		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			this.updateHandler();// proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			this.getHost().markForUpdate();
		}
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		IPartHost host = this.getHost();
		if( host != null )
		{
			TileEntity te = host.getTile();

			BlockPos pos = te.getPos();

			EnumFacing e = bch.getWorldX();
			EnumFacing u = bch.getWorldY();

			if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( e.getOpposite() ) ), this.side ) )
			{
				minX = 0;
			}

			if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( e)  ), this.side ) )
			{
				maxX = 16;
			}

			if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( u.getOpposite() ) ), this.side ) )
			{
				minY = 0;
			}

			if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( u) ), this.side ) )
			{
				maxY = 16;
			}
		}

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, ModelGenerator renderer )
	{
		rh.setTexture( CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartTransitionPlaneBack.getIcon(), renderer.getIcon( is ), CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon() );

		rh.setBounds( 1, 1, 15, 15, 15, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( BlockPos opos, IPartRenderHelper rh, ModelGenerator renderer )
	{
		int minX = 1;

		EnumFacing e = rh.getWorldX();
		EnumFacing u = rh.getWorldY();

		TileEntity te = this.getHost().getTile();
		BlockPos pos = te.getPos();

		if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( e.getOpposite() ) ), this.side ) )
		{
			minX = 0;
		}

		int maxX = 15;
		if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( e ) ), this.side ) )
		{
			maxX = 16;
		}

		int minY = 1;
		if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( u.getOpposite() ) ), this.side ) )
		{
			minY = 0;
		}

		int maxY = 15;
		if( this.isTransitionPlane( te.getWorld().getTileEntity( pos.offset( u ) ), this.side ) )
		{
			maxY = 16;
		}

		boolean isActive = ( this.clientFlags & ( PartBasicState.POWERED_FLAG | PartBasicState.CHANNEL_FLAG ) ) == ( PartBasicState.POWERED_FLAG | PartBasicState.CHANNEL_FLAG );

		rh.setTexture( CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockFormPlaneOn.getIcon() : renderer.getIcon( is ), CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon() );

		rh.setBounds( minX, minY, 15, maxX, maxY, 16 );
		rh.renderBlock( opos, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockFormPlaneOn.getIcon() : renderer.getIcon( is ), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( opos, renderer );

		this.renderLights( opos, rh, renderer );
	}

	@Override
	public void onNeighborChanged()
	{
		TileEntity te = this.host.getTile();
		World w = te.getWorld();
		AEPartLocation side = this.side;

		BlockPos tePos = te.getPos().offset( side.getFacing()  );
		
		this.blocked = !w.getBlockState( tePos ).getBlock().isReplaceable( w, tePos );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		if( !player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( player, this.getHost().getTile(), this.side, GuiBridge.GUI_FORMATION_PLANE );
			return true;
		}

		return false;
	}

	private boolean isTransitionPlane( TileEntity blockTileEntity, AEPartLocation side )
	{
		if( blockTileEntity instanceof IPartHost )
		{
			IPart p = ( (IPartHost) blockTileEntity ).getPart( side );
			return p instanceof PartFormationPlane;
		}
		return false;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( StorageChannel channel )
	{
		if( this.proxy.isActive() && channel == StorageChannel.ITEMS )
		{
			List<IMEInventoryHandler> Handler = new ArrayList<IMEInventoryHandler>( 1 );
			Handler.add( this.myHandler );
			return Handler;
		}
		return new ArrayList<IMEInventoryHandler>();
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public void setPriority( int newValue )
	{
		this.priority = newValue;
		this.host.markForSave();
		this.updateHandler();
	}

	@Override
	public void blinkCell( int slot )
	{
		// :P
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable type, BaseActionSource src )
	{
		if( this.blocked || input == null || input.getStackSize() <= 0 )
		{
			return input;
		}

		YesNo placeBlock = (YesNo) this.getConfigManager().getSetting( Settings.PLACE_BLOCK );

		ItemStack is = input.getItemStack();
		Item i = is.getItem();

		long maxStorage = Math.min( input.getStackSize(), is.getMaxStackSize() );
		boolean worked = false;

		TileEntity te = this.host.getTile();
		World w = te.getWorld();
		AEPartLocation side = this.side;

		BlockPos tePos = te.getPos().offset( side.getFacing() );

		if( w.getBlockState( tePos ).getBlock().isReplaceable( w, tePos ) )
		{
			if( placeBlock == YesNo.YES && ( i instanceof ItemBlock || i instanceof IPlantable || i instanceof ItemSkull || i instanceof ItemFirework || i instanceof IPartItem || i instanceof ItemReed ) )
			{
				EntityPlayer player = Platform.getPlayer( (WorldServer) w );
				Platform.configurePlayer( player, side, this.tile );

				// TODO: LIMIT FIREWORKS
				/*
				if( i instanceof ItemFirework )
				{
					Chunk c = w.getChunkFromBlockCoords( tePos );
					int sum = 0;
					for( List Z : c.geten )
					{
						sum += Z.size();
					}
					if( sum > 32 )
					{
						return input;
					}
				}
				*/
				
				maxStorage = is.stackSize;
				worked = true;
				if( type == Actionable.MODULATE )
				{
					if( i instanceof IPlantable || i instanceof ItemSkull || i instanceof ItemReed )
					{
						boolean Worked = false;

						if( side.xOffset == 0 && side.zOffset == 0 )
						{
							Worked = i.onItemUse( is, player, w, tePos.offset( side.getFacing() ), side.getFacing().getOpposite(), side.xOffset, side.yOffset, side.zOffset );
						}

						if( !Worked && side.xOffset == 0 && side.zOffset == 0 )
						{
							Worked = i.onItemUse( is, player, w, tePos.offset( side.getFacing().getOpposite() ), side.getFacing(), side.xOffset, side.yOffset, side.zOffset );
						}

						if( !Worked && side.yOffset == 0 )
						{
							Worked = i.onItemUse( is, player, w, tePos.offset( EnumFacing.DOWN ), EnumFacing.UP, side.xOffset, side.yOffset, side.zOffset );
						}

						if( !Worked )
						{
							i.onItemUse( is, player, w, tePos, side.getFacing().getOpposite(), side.xOffset, side.yOffset, side.zOffset );
						}

						maxStorage -= is.stackSize;
					}
					else
					{
						i.onItemUse( is, player, w, tePos, side.getFacing().getOpposite(), side.xOffset, side.yOffset, side.zOffset );
						maxStorage -= is.stackSize;
					}
				}
				else
				{
					maxStorage = 1;
				}
			}
			else
			{
				worked = true;
				Chunk c = w.getChunkFromBlockCoords( tePos );

				int sum = 0;
				
				// TODO: LIMIT OTHER THIGNS!
				/*
				for( List Z : c.entityLists )
				{
					sum += Z.size();
				}
				*/
				
				if( sum < AEConfig.instance.formationPlaneEntityLimit )
				{
					if( type == Actionable.MODULATE )
					{

						is.stackSize = (int) maxStorage;
						EntityItem ei = new EntityItem( w, // w
								( ( side.xOffset != 0 ? 0.0 : 0.7 ) * ( Platform.getRandomFloat() - 0.5f ) ) + 0.5 + side.xOffset * -0.3 + tePos.getX(), // spawn
								( ( side.yOffset != 0 ? 0.0 : 0.7 ) * ( Platform.getRandomFloat() - 0.5f ) ) + 0.5 + side.yOffset * -0.3 + tePos.getY(), // spawn
								( ( side.zOffset != 0 ? 0.0 : 0.7 ) * ( Platform.getRandomFloat() - 0.5f ) ) + 0.5 + side.zOffset * -0.3 + tePos.getZ(), // spawn
								is.copy() );

						Entity result = ei;

						ei.motionX = side.xOffset * 0.2;
						ei.motionY = side.yOffset * 0.2;
						ei.motionZ = side.zOffset * 0.2;

						if( is.getItem().hasCustomEntity( is ) )
						{
							result = is.getItem().createEntity( w, ei, is );
							if( result != null )
							{
								ei.setDead();
							}
							else
							{
								result = ei;
							}
						}

						if( !w.spawnEntityInWorld( result ) )
						{
							result.setDead();
							worked = false;
						}
					}
				}
				else
				{
					worked = false;
				}
			}
		}

		this.blocked = !w.getBlockState( tePos ).getBlock().isReplaceable( w, tePos );

		if( worked )
		{
			IAEItemStack out = input.copy();
			out.decStackSize( maxStorage );
			if( out.getStackSize() == 0 )
			{
				return null;
			}
			return out;
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public void saveChanges( IMEInventory cellInventory )
	{
		// nope!
	}
}
