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

package appeng.core.sync;


import java.lang.reflect.Constructor;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;

import com.google.common.collect.Lists;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.GuiNull;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerChest;
import appeng.container.implementations.ContainerCondenser;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerDrive;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.container.implementations.ContainerGrinder;
import appeng.container.implementations.ContainerIOPort;
import appeng.container.implementations.ContainerInscriber;
import appeng.container.implementations.ContainerInterface;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerMAC;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerMEPortableCell;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerQNB;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.container.implementations.ContainerSecurity;
import appeng.container.implementations.ContainerSkyChest;
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.container.implementations.ContainerStorageBus;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.container.implementations.ContainerWireless;
import appeng.container.implementations.ContainerWirelessTerm;
import appeng.core.stats.Achievements;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.contents.QuartzKnifeObj;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartMonitor;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileCellWorkbench;
import appeng.tile.misc.TileCondenser;
import appeng.tile.misc.TileInscriber;
import appeng.tile.misc.TileSecurity;
import appeng.tile.misc.TileVibrationChamber;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import appeng.tile.storage.TileIOPort;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;

import static appeng.core.sync.GuiHostType.ITEM;
import static appeng.core.sync.GuiHostType.ITEM_OR_WORLD;
import static appeng.core.sync.GuiHostType.WORLD;


public enum GuiBridge implements IGuiHandler
{
	GUI_Handler(),

	GUI_GRINDER( ContainerGrinder.class, TileGrinder.class, WORLD, null ),

	GUI_QNB( ContainerQNB.class, TileQuantumBridge.class, WORLD, SecurityPermissions.BUILD ),

	GUI_SKYCHEST( ContainerSkyChest.class, TileSkyChest.class, WORLD, null ),

	GUI_CHEST( ContainerChest.class, TileChest.class, WORLD, SecurityPermissions.BUILD ),

	GUI_WIRELESS( ContainerWireless.class, TileWireless.class, WORLD, SecurityPermissions.BUILD ),

	GUI_ME( ContainerMEMonitorable.class, ITerminalHost.class, WORLD, null ),

	GUI_PORTABLE_CELL( ContainerMEPortableCell.class, IPortableCell.class, ITEM, null ),

	GUI_WIRELESS_TERM( ContainerWirelessTerm.class, WirelessTerminalGuiObject.class, ITEM, null ),

	GUI_NETWORK_STATUS( ContainerNetworkStatus.class, INetworkTool.class, ITEM, null ),

	GUI_CRAFTING_CPU( ContainerCraftingCPU.class, TileCraftingTile.class, WORLD, SecurityPermissions.CRAFT ),

	GUI_NETWORK_TOOL( ContainerNetworkTool.class, INetworkTool.class, ITEM, null ),

	GUI_QUARTZ_KNIFE( ContainerQuartzKnife.class, QuartzKnifeObj.class, ITEM, null ),

	GUI_DRIVE( ContainerDrive.class, TileDrive.class, WORLD, SecurityPermissions.BUILD ),

	GUI_VIBRATION_CHAMBER( ContainerVibrationChamber.class, TileVibrationChamber.class, WORLD, null ),

	GUI_CONDENSER( ContainerCondenser.class, TileCondenser.class, WORLD, null ),

	GUI_INTERFACE( ContainerInterface.class, IInterfaceHost.class, WORLD, SecurityPermissions.BUILD ),

	GUI_BUS( ContainerUpgradeable.class, IUpgradeableHost.class, WORLD, SecurityPermissions.BUILD ),

	GUI_IOPORT( ContainerIOPort.class, TileIOPort.class, WORLD, SecurityPermissions.BUILD ),

	GUI_STORAGEBUS( ContainerStorageBus.class, PartStorageBus.class, WORLD, SecurityPermissions.BUILD ),

	GUI_FORMATION_PLANE( ContainerFormationPlane.class, PartFormationPlane.class, WORLD, SecurityPermissions.BUILD ),

	GUI_PRIORITY( ContainerPriority.class, IPriorityHost.class, WORLD, SecurityPermissions.BUILD ),

	GUI_SECURITY( ContainerSecurity.class, TileSecurity.class, WORLD, SecurityPermissions.SECURITY ),

	GUI_CRAFTING_TERMINAL( ContainerCraftingTerm.class, PartCraftingTerminal.class, WORLD, SecurityPermissions.CRAFT ),

	GUI_PATTERN_TERMINAL( ContainerPatternTerm.class, PartPatternTerminal.class, WORLD, SecurityPermissions.CRAFT ),

	// extends (Container/Gui) + Bus
	GUI_LEVEL_EMITTER( ContainerLevelEmitter.class, PartLevelEmitter.class, WORLD, SecurityPermissions.BUILD ),

	GUI_SPATIAL_IO_PORT( ContainerSpatialIOPort.class, TileSpatialIOPort.class, WORLD, SecurityPermissions.BUILD ),

	GUI_INSCRIBER( ContainerInscriber.class, TileInscriber.class, WORLD, null ),

	GUI_CELL_WORKBENCH( ContainerCellWorkbench.class, TileCellWorkbench.class, WORLD, null ),

	GUI_MAC( ContainerMAC.class, TileMolecularAssembler.class, WORLD, null ),

	GUI_CRAFTING_AMOUNT( ContainerCraftAmount.class, ITerminalHost.class, ITEM_OR_WORLD, SecurityPermissions.CRAFT ),

	GUI_CRAFTING_CONFIRM( ContainerCraftConfirm.class, ITerminalHost.class, ITEM_OR_WORLD, SecurityPermissions.CRAFT ),

	GUI_INTERFACE_TERMINAL( ContainerInterfaceTerminal.class, PartMonitor.class, WORLD, SecurityPermissions.BUILD ),

	GUI_CRAFTING_STATUS( ContainerCraftingStatus.class, ITerminalHost.class, ITEM_OR_WORLD, SecurityPermissions.CRAFT );

	private final Class Tile;
	private final Class Container;
	private Class Gui;
	private GuiHostType type;
	private SecurityPermissions requiredPermission;

	GuiBridge()
	{
		this.Tile = null;
		this.Gui = null;
		this.Container = null;
	}

	GuiBridge( Class _Container, SecurityPermissions requiredPermission )
	{
		this.requiredPermission = requiredPermission;
		this.Container = _Container;
		this.Tile = null;
		this.getGui();
	}

	/**
	 * I honestly wish I could just use the GuiClass Names myself, but I can't access them without MC's Server
	 * Exploding.
	 */
	private void getGui()
	{
		if( Platform.isClient() )
		{
			final String start = this.Container.getName();
			String guiClass = start.replaceFirst( "container.", "client.gui." ).replace( ".Container", ".Gui" );

			if( start.equals( guiClass ) )
			{
				throw new IllegalStateException( "Unable to find gui class" );
			}
			this.Gui = ReflectionHelper.getClass( this.getClass().getClassLoader(), guiClass );
			if( this.Gui == null )
			{
				throw new IllegalStateException( "Cannot Load class: " + guiClass );
			}
		}
	}

	GuiBridge( Class _Container, Class _Tile, GuiHostType type, SecurityPermissions requiredPermission )
	{
		this.requiredPermission = requiredPermission;
		this.Container = _Container;
		this.type = type;
		this.Tile = _Tile;
		this.getGui();
	}

	@Override
	public Object getServerGuiElement( int ID_ORDINAL, EntityPlayer player, World w, int x, int y, int z )
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID_ORDINAL & 0x07 );
		GuiBridge ID = values()[ID_ORDINAL >> 4];
		boolean stem = ( ( ID_ORDINAL >> 3 ) & 1 ) == 1;
		if( ID.type.isItem() )
		{
			ItemStack it = null;
			if( stem )
			{
				it = player.inventory.getCurrentItem();
			}
			else if( x >= 0 && x < player.inventory.mainInventory.length )
			{
				it = player.inventory.getStackInSlot( x );
			}
			Object myItem = this.getGuiObject( it, player, w, x, y, z );
			if( myItem != null && ID.CorrectTileOrPart( myItem ) )
			{
				return this.updateGui( ID.ConstructContainer( player.inventory, side, myItem ), w, x, y, z, side, myItem );
			}
		}
		if( ID.type.isTile() )
		{
			TileEntity TE = w.getTileEntity( x, y, z );
			if( TE instanceof IPartHost )
			{
				( (IPartHost) TE ).getPart( side );
				IPart part = ( (IPartHost) TE ).getPart( side );
				if( ID.CorrectTileOrPart( part ) )
				{
					return this.updateGui( ID.ConstructContainer( player.inventory, side, part ), w, x, y, z, side, part );
				}
			}
			else
			{
				if( ID.CorrectTileOrPart( TE ) )
				{
					return this.updateGui( ID.ConstructContainer( player.inventory, side, TE ), w, x, y, z, side, TE );
				}
			}
		}
		return new ContainerNull();
	}

	private Object getGuiObject( ItemStack it, EntityPlayer player, World w, int x, int y, int z )
	{
		if( it != null )
		{
			if( it.getItem() instanceof IGuiItem )
			{
				return ( (IGuiItem) it.getItem() ).getGuiObject( it, w, x, y, z );
			}

			IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler( it );
			if( wh != null )
			{
				return new WirelessTerminalGuiObject( wh, it, player, w, x, y, z );
			}
		}

		return null;
	}

	public boolean CorrectTileOrPart( Object tE )
	{
		if( this.Tile == null )
		{
			throw new IllegalArgumentException( "This Gui Cannot use the standard Handler." );
		}

		return this.Tile.isInstance( tE );
	}

	private Object updateGui( Object newContainer, World w, int x, int y, int z, ForgeDirection side, Object myItem )
	{
		if( newContainer instanceof AEBaseContainer )
		{
			AEBaseContainer bc = (AEBaseContainer) newContainer;
			bc.openContext = new ContainerOpenContext( myItem );
			bc.openContext.w = w;
			bc.openContext.x = x;
			bc.openContext.y = y;
			bc.openContext.z = z;
			bc.openContext.side = side;
		}

		return newContainer;
	}

	public Object ConstructContainer( InventoryPlayer inventory, ForgeDirection side, Object tE )
	{
		try
		{
			Constructor[] c = this.Container.getConstructors();
			if( c.length == 0 )
			{
				throw new AppEngException( "Invalid Gui Class" );
			}

			Constructor target = this.findConstructor( c, inventory, tE );

			if( target == null )
			{
				throw new IllegalStateException( "Cannot find " + this.Container.getName() + "( " + this.typeName( inventory ) + ", " + this.typeName( tE ) + " )" );
			}

			Object o = target.newInstance( inventory, tE );

			/**
			 * triggers achievement when the player sees presses.
			 */
			if( o instanceof AEBaseContainer )
			{
				AEBaseContainer bc = (AEBaseContainer) o;
				for( Object so : bc.inventorySlots )
				{
					if( so instanceof Slot )
					{
						ItemStack is = ( (Slot) so ).getStack();

						final IMaterials materials = AEApi.instance().definitions().materials();
						this.addPressAchievementToPlayer( is, materials, inventory.player );
					}
				}
			}

			return o;
		}
		catch( Throwable t )
		{
			throw new IllegalStateException( t );
		}
	}

	private Constructor findConstructor( Constructor[] c, InventoryPlayer inventory, Object tE )
	{
		for( Constructor con : c )
		{
			Class[] types = con.getParameterTypes();
			if( types.length == 2 )
			{
				if( types[0].isAssignableFrom( inventory.getClass() ) && types[1].isAssignableFrom( tE.getClass() ) )
				{
					return con;
				}
			}
		}
		return null;
	}

	private String typeName( Object inventory )
	{
		if( inventory == null )
		{
			return "NULL";
		}

		return inventory.getClass().getName();
	}

	private void addPressAchievementToPlayer( ItemStack newItem, IMaterials possibleMaterials, EntityPlayer player )
	{
		final IComparableDefinition logic = possibleMaterials.logicProcessorPress();
		final IComparableDefinition eng = possibleMaterials.engProcessorPress();
		final IComparableDefinition calc = possibleMaterials.calcProcessorPress();
		final IComparableDefinition silicon = possibleMaterials.siliconPress();

		final List<IComparableDefinition> presses = Lists.newArrayList( logic, eng, calc, silicon );

		for( IComparableDefinition press : presses )
		{
			if( press.isSameAs( newItem ) )
			{
				Achievements.Presses.addToPlayer( player );

				return;
			}
		}
	}

	@Override
	public Object getClientGuiElement( int ID_ORDINAL, EntityPlayer player, World w, int x, int y, int z )
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID_ORDINAL & 0x07 );
		GuiBridge ID = values()[ID_ORDINAL >> 4];
		boolean stem = ( ( ID_ORDINAL >> 3 ) & 1 ) == 1;
		if( ID.type.isItem() )
		{
			ItemStack it = null;
			if( stem )
			{
				it = player.inventory.getCurrentItem();
			}
			else if( x >= 0 && x < player.inventory.mainInventory.length )
			{
				it = player.inventory.getStackInSlot( x );
			}
			Object myItem = this.getGuiObject( it, player, w, x, y, z );
			if( myItem != null && ID.CorrectTileOrPart( myItem ) )
			{
				return ID.ConstructGui( player.inventory, side, myItem );
			}
		}
		if( ID.type.isTile() )
		{
			TileEntity TE = w.getTileEntity( x, y, z );
			if( TE instanceof IPartHost )
			{
				( (IPartHost) TE ).getPart( side );
				IPart part = ( (IPartHost) TE ).getPart( side );
				if( ID.CorrectTileOrPart( part ) )
				{
					return ID.ConstructGui( player.inventory, side, part );
				}
			}
			else
			{
				if( ID.CorrectTileOrPart( TE ) )
				{
					return ID.ConstructGui( player.inventory, side, TE );
				}
			}
		}
		return new GuiNull( new ContainerNull() );
	}

	public Object ConstructGui( InventoryPlayer inventory, ForgeDirection side, Object tE )
	{
		try
		{
			Constructor[] c = this.Gui.getConstructors();
			if( c.length == 0 )
			{
				throw new AppEngException( "Invalid Gui Class" );
			}

			Constructor target = this.findConstructor( c, inventory, tE );

			if( target == null )
			{
				throw new IllegalStateException( "Cannot find " + this.Container.getName() + "( " + this.typeName( inventory ) + ", " + this.typeName( tE ) + " )" );
			}

			return target.newInstance( inventory, tE );
		}
		catch( Throwable t )
		{
			throw new IllegalStateException( t );
		}
	}

	public boolean hasPermissions( TileEntity te, int x, int y, int z, ForgeDirection side, EntityPlayer player )
	{
		World w = player.getEntityWorld();

		if( Platform.hasPermissions( te != null ? new DimensionalCoord( te ) : new DimensionalCoord( player.worldObj, x, y, z ), player ) )
		{
			if( this.type.isItem() )
			{
				ItemStack it = player.inventory.getCurrentItem();
				if( it != null && it.getItem() instanceof IGuiItem )
				{
					Object myItem = ( (IGuiItem) it.getItem() ).getGuiObject( it, w, x, y, z );
					if( this.CorrectTileOrPart( myItem ) )
					{
						return true;
					}
				}
			}

			if( this.type.isTile() )
			{
				TileEntity TE = w.getTileEntity( x, y, z );
				if( TE instanceof IPartHost )
				{
					( (IPartHost) TE ).getPart( side );
					IPart part = ( (IPartHost) TE ).getPart( side );
					if( this.CorrectTileOrPart( part ) )
					{
						return this.securityCheck( part, player );
					}
				}
				else
				{
					if( this.CorrectTileOrPart( TE ) )
					{
						return this.securityCheck( TE, player );
					}
				}
			}
		}
		return false;
	}

	private boolean securityCheck( Object te, EntityPlayer player )
	{
		if( te instanceof IActionHost && this.requiredPermission != null )
		{
			boolean requirePower = false;

			IGridNode gn = ( (IActionHost) te ).getActionableNode();
			if( gn != null )
			{
				IGrid g = gn.getGrid();
				if( g != null )
				{
					if( requirePower )
					{
						IEnergyGrid eg = g.getCache( IEnergyGrid.class );
						if( !eg.isNetworkPowered() )
						{
							return false;
						}
					}

					ISecurityGrid sg = g.getCache( ISecurityGrid.class );
					if( sg.hasPermission( player, this.requiredPermission ) )
					{
						return true;
					}
				}
			}

			return false;
		}
		return true;
	}

	public GuiHostType getType()
	{
		return this.type;
	}

}
