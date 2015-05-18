/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.container.implementations;


import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.parts.IPart;
import appeng.api.util.IConfigManager;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.tools.ToolNetworkTool;
import appeng.parts.automation.PartExportBus;
import appeng.util.Platform;


public class ContainerUpgradeable extends AEBaseContainer implements IOptionalSlotHost
{

	final IUpgradeableHost upgradeable;
	@GuiSync( 0 )
	public RedstoneMode rsMode = RedstoneMode.IGNORE;
	@GuiSync( 1 )
	public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;
	@GuiSync( 5 )
	public YesNo cMode = YesNo.NO;
	int tbSlot;
	NetworkToolViewer tbInventory;

	public ContainerUpgradeable( InventoryPlayer ip, IUpgradeableHost te )
	{
		super( ip, (TileEntity) ( te instanceof TileEntity ? te : null ), (IPart) ( te instanceof IPart ? te : null ) );
		this.upgradeable = te;

		World w = null;
		int xCoord = 0;
		int yCoord = 0;
		int zCoord = 0;

		if( te instanceof TileEntity )
		{
			TileEntity myTile = (TileEntity) te;
			w = myTile.getWorldObj();
			xCoord = myTile.xCoord;
			yCoord = myTile.yCoord;
			zCoord = myTile.zCoord;
		}

		if( te instanceof IPart )
		{
			TileEntity mk = te.getTile();
			w = mk.getWorldObj();
			xCoord = mk.xCoord;
			yCoord = mk.yCoord;
			zCoord = mk.zCoord;
		}

		IInventory pi = this.getPlayerInv();
		for( int x = 0; x < pi.getSizeInventory(); x++ )
		{
			ItemStack pii = pi.getStackInSlot( x );
			if( pii != null && pii.getItem() instanceof ToolNetworkTool )
			{
				this.lockPlayerInventorySlot( x );
				this.tbSlot = x;
				this.tbInventory = (NetworkToolViewer) ( (IGuiItem) pii.getItem() ).getGuiObject( pii, w, xCoord, yCoord, zCoord );
				break;
			}
		}

		if( this.hasToolbox() )
		{
			for( int v = 0; v < 3; v++ )
			{
				for( int u = 0; u < 3; u++ )
				{
					this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, this.tbInventory, u + v * 3, 186 + u * 18, this.getHeight() - 82 + v * 18, this.invPlayer ) ).setPlayerSide() );
				}
			}
		}

		this.setupConfig();

		this.bindPlayerInventory( ip, 0, this.getHeight() - /* height of player inventory */82 );
	}

	public final boolean hasToolbox()
	{
		return this.tbInventory != null;
	}

	protected int getHeight()
	{
		return 184;
	}

	protected void setupConfig()
	{
		int x = 80;
		int y = 40;
		this.setupUpgrades();

		IInventory inv = this.upgradeable.getInventoryByName( "config" );
		this.addSlotToContainer( new SlotFakeTypeOnly( inv, 0, x, y ) );

		if( this.supportCapacity() )
		{
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 1, x, y, -1, 0, 1 ) );
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 2, x, y, 1, 0, 1 ) );
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 3, x, y, 0, -1, 1 ) );
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 4, x, y, 0, 1, 1 ) );

			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 5, x, y, -1, -1, 2 ) );
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 6, x, y, 1, -1, 2 ) );
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 7, x, y, -1, 1, 2 ) );
			this.addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 8, x, y, 1, 1, 2 ) );
		}
	}

	protected final void setupUpgrades()
	{
		IInventory upgrades = this.upgradeable.getInventoryByName( "upgrades" );
		if( this.availableUpgrades() > 0 )
		{
			this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.invPlayer ) ).setNotDraggable() );
		}
		if( this.availableUpgrades() > 1 )
		{
			this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.invPlayer ) ).setNotDraggable() );
		}
		if( this.availableUpgrades() > 2 )
		{
			this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.invPlayer ) ).setNotDraggable() );
		}
		if( this.availableUpgrades() > 3 )
		{
			this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.invPlayer ) ).setNotDraggable() );
		}
	}

	protected boolean supportCapacity()
	{
		return true;
	}

	public int availableUpgrades()
	{
		return 4;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			IConfigManager cm = this.upgradeable.getConfigManager();
			this.loadSettingsFromHost( cm );
		}

		this.checkToolbox();

		for( Object o : this.inventorySlots )
		{
			if( o instanceof OptionalSlotFake )
			{
				OptionalSlotFake fs = (OptionalSlotFake) o;
				if( !fs.isEnabled() && fs.getDisplayStack() != null )
				{
					fs.clearStack();
				}
			}
		}

		this.standardDetectAndSendChanges();
	}

	protected void loadSettingsFromHost( IConfigManager cm )
	{
		this.fzMode = (FuzzyMode) cm.getSetting( Settings.FUZZY_MODE );
		this.rsMode = (RedstoneMode) cm.getSetting( Settings.REDSTONE_CONTROLLED );
		if( this.upgradeable instanceof PartExportBus )
		{
			this.cMode = (YesNo) cm.getSetting( Settings.CRAFT_ONLY );
		}
	}

	public final void checkToolbox()
	{
		if( this.hasToolbox() )
		{
			ItemStack currentItem = this.getPlayerInv().getStackInSlot( this.tbSlot );

			if( currentItem != this.tbInventory.getItemStack() )
			{
				if( currentItem != null )
				{
					if( Platform.isSameItem( this.tbInventory.getItemStack(), currentItem ) )
					{
						this.getPlayerInv().setInventorySlotContents( this.tbSlot, this.tbInventory.getItemStack() );
					}
					else
					{
						this.isContainerValid = false;
					}
				}
				else
				{
					this.isContainerValid = false;
				}
			}
		}
	}

	protected final void standardDetectAndSendChanges()
	{
		super.detectAndSendChanges();
	}

	@Override
	public boolean isSlotEnabled( int idx )
	{
		int upgrades = this.upgradeable.getInstalledUpgrades( Upgrades.CAPACITY );

		if( idx == 1 && upgrades > 0 )
		{
			return true;
		}
		if( idx == 2 && upgrades > 1 )
		{
			return true;
		}

		return false;
	}
}
