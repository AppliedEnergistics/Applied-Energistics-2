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

package appeng.container.implementations;


import appeng.items.contents.NetworkToolViewer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
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
import appeng.items.tools.ToolNetworkTool;
import appeng.parts.automation.PartExportBus;
import appeng.util.Platform;


public class ContainerUpgradeable extends AEBaseContainer implements IOptionalSlotHost
{

	private final IUpgradeableHost upgradeable;
	@GuiSync( 0 )
	public RedstoneMode rsMode = RedstoneMode.IGNORE;
	@GuiSync( 1 )
	public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;
	@GuiSync( 5 )
	public YesNo cMode = YesNo.NO;
	@GuiSync( 6 )
	public SchedulingMode schedulingMode = SchedulingMode.DEFAULT;
	private int tbSlot;
	private NetworkToolViewer tbInventory;

	public ContainerUpgradeable( final InventoryPlayer ip, final IUpgradeableHost te )
	{
		super( ip, (TileEntity) ( te instanceof TileEntity ? te : null ), (IPart) ( te instanceof IPart ? te : null ) );
		this.upgradeable = te;

		World w = null;
		int xCoord = 0;
		int yCoord = 0;
		int zCoord = 0;

		if( te instanceof TileEntity )
		{
			final TileEntity myTile = (TileEntity) te;
			w = myTile.getWorld();
			xCoord = myTile.getPos().getX();
			yCoord = myTile.getPos().getY();
			zCoord = myTile.getPos().getZ();
		}

		if( te instanceof IPart )
		{
			final TileEntity mk = te.getTile();
			w = mk.getWorld();
			xCoord = mk.getPos().getX();
			yCoord = mk.getPos().getY();
			zCoord = mk.getPos().getZ();
		}

		final IInventory pi = this.getPlayerInv();
		for( int x = 0; x < pi.getSizeInventory(); x++ )
		{
			final ItemStack pii = pi.getStackInSlot( x );
			if( !pii.isEmpty() && pii.getItem() instanceof ToolNetworkTool )
			{
				this.lockPlayerInventorySlot( x );
				this.tbSlot = x;
				this.tbInventory = (NetworkToolViewer) ( (IGuiItem) pii.getItem() ).getGuiObject( pii, w, new BlockPos( xCoord, yCoord, zCoord ) );
				break;
			}
		}

		if( this.hasToolbox() )
		{
			for( int v = 0; v < 3; v++ )
			{
				for( int u = 0; u < 3; u++ )
				{
					this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, this.tbInventory
							.getInternalInventory(), u + v * 3, 186 + u * 18, this.getHeight() - 82 + v * 18, this.getInventoryPlayer() ) ).setPlayerSide() );
				}
			}
		}

		this.setupConfig();

		this.bindPlayerInventory( ip, 0, this.getHeight() - /* height of player inventory */82 );
	}

	public boolean hasToolbox()
	{
		return this.tbInventory != null;
	}

	protected int getHeight()
	{
		return 184;
	}

	protected void setupConfig()
	{
		this.setupUpgrades();

		final IItemHandler inv = this.getUpgradeable().getInventoryByName( "config" );
		final int y = 40;
		final int x = 80;
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

	protected void setupUpgrades()
	{
		final IItemHandler upgrades = this.getUpgradeable().getInventoryByName( "upgrades" );
		if( this.availableUpgrades() > 0 )
		{
			this.addSlotToContainer(
					( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer() ) )
							.setNotDraggable() );
		}
		if( this.availableUpgrades() > 1 )
		{
			this.addSlotToContainer(
					( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.getInventoryPlayer() ) )
							.setNotDraggable() );
		}
		if( this.availableUpgrades() > 2 )
		{
			this.addSlotToContainer(
					( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.getInventoryPlayer() ) )
							.setNotDraggable() );
		}
		if( this.availableUpgrades() > 3 )
		{
			this.addSlotToContainer(
					( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.getInventoryPlayer() ) )
							.setNotDraggable() );
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
			final IConfigManager cm = this.getUpgradeable().getConfigManager();
			this.loadSettingsFromHost( cm );
		}

		this.checkToolbox();

		for( final Object o : this.inventorySlots )
		{
			if( o instanceof OptionalSlotFake )
			{
				final OptionalSlotFake fs = (OptionalSlotFake) o;
				if( !fs.isSlotEnabled() && !fs.getDisplayStack().isEmpty() )
				{
					fs.clearStack();
				}
			}
		}

		this.standardDetectAndSendChanges();
	}

	protected void loadSettingsFromHost( final IConfigManager cm )
	{
		this.setFuzzyMode( (FuzzyMode) cm.getSetting( Settings.FUZZY_MODE ) );
		this.setRedStoneMode( (RedstoneMode) cm.getSetting( Settings.REDSTONE_CONTROLLED ) );
		if( this.getUpgradeable() instanceof PartExportBus )
		{
			this.setCraftingMode( (YesNo) cm.getSetting( Settings.CRAFT_ONLY ) );
			this.setSchedulingMode( (SchedulingMode) cm.getSetting( Settings.SCHEDULING_MODE ) );
		}
	}

	protected void checkToolbox()
	{
		if( this.hasToolbox() )
		{
			final ItemStack currentItem = this.getPlayerInv().getStackInSlot( this.tbSlot );

			if( currentItem != this.tbInventory.getItemStack() )
			{
				if( !currentItem.isEmpty() )
				{
					if( ItemStack.areItemsEqual( this.tbInventory.getItemStack(), currentItem ) )
					{
						this.getPlayerInv().setInventorySlotContents( this.tbSlot, this.tbInventory.getItemStack() );
					}
					else
					{
						this.setValidContainer( false );
					}
				}
				else
				{
					this.setValidContainer( false );
				}
			}
		}
	}

	protected void standardDetectAndSendChanges()
	{
		super.detectAndSendChanges();
	}

	@Override
	public boolean isSlotEnabled( final int idx )
	{
		final int upgrades = this.getUpgradeable().getInstalledUpgrades( Upgrades.CAPACITY );

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

	public FuzzyMode getFuzzyMode()
	{
		return this.fzMode;
	}

	public void setFuzzyMode( final FuzzyMode fzMode )
	{
		this.fzMode = fzMode;
	}

	public YesNo getCraftingMode()
	{
		return this.cMode;
	}

	public void setCraftingMode( final YesNo cMode )
	{
		this.cMode = cMode;
	}

	public RedstoneMode getRedStoneMode()
	{
		return this.rsMode;
	}

	public void setRedStoneMode( final RedstoneMode rsMode )
	{
		this.rsMode = rsMode;
	}

	public SchedulingMode getSchedulingMode()
	{
		return this.schedulingMode;
	}

	private void setSchedulingMode( final SchedulingMode schedulingMode )
	{
		this.schedulingMode = schedulingMode;
	}

	protected IUpgradeableHost getUpgradeable()
	{
		return this.upgradeable;
	}
}
