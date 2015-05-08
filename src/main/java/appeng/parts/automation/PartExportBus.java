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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.helpers.MultiCraftingTracker;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class PartExportBus extends PartSharedItemBus implements ICraftingRequester
{
	final MultiCraftingTracker cratingTracker = new MultiCraftingTracker( this, 9 );
	final BaseActionSource mySrc;
	long itemToSend = 1;
	boolean didSomething = false;

	@Reflected
	public PartExportBus( ItemStack is )
	{
		super( is );

		this.getConfigManager().registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.CRAFT_ONLY, YesNo.NO );
		this.mySrc = new MachineSource( this );
	}

	@Override
	public void readFromNBT( NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.cratingTracker.readFromNBT( extra );
	}

	@Override
	public void writeToNBT( NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.cratingTracker.writeToNBT( extra );
	}

	@Override
	TickRateModulation doBusWork()
	{
		if( !this.proxy.isActive() )
		{
			return TickRateModulation.IDLE;
		}

		this.itemToSend = 1;
		this.didSomething = false;

		switch( this.getInstalledUpgrades( Upgrades.SPEED ) )
		{
			default:
			case 0:
				this.itemToSend = 1;
				break;
			case 1:
				this.itemToSend = 8;
				break;
			case 2:
				this.itemToSend = 32;
				break;
			case 3:
				this.itemToSend = 64;
				break;
			case 4:
				this.itemToSend = 96;
				break;
		}

		try
		{
			InventoryAdaptor d = this.getHandler();
			IMEMonitor<IAEItemStack> inv = this.proxy.getStorage().getItemInventory();
			IEnergyGrid energy = this.proxy.getEnergy();
			ICraftingGrid cg = this.proxy.getCrafting();
			FuzzyMode fzMode = (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE );

			if( d != null )
			{
				for( int x = 0; x < this.availableSlots() && this.itemToSend > 0; x++ )
				{
					IAEItemStack ais = this.config.getAEStackInSlot( x );
					if( ais == null || this.itemToSend <= 0 || this.craftOnly() )
					{
						if( this.isCraftingEnabled() )
						{
							this.didSomething = this.cratingTracker.handleCrafting( x, this.itemToSend, ais, d, this.getTile().getWorldObj(), this.proxy.getGrid(), cg, this.mySrc ) || this.didSomething;
						}
						continue;
					}

					long before = this.itemToSend;

					if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
					{
						for( IAEItemStack o : ImmutableList.copyOf( inv.getStorageList().findFuzzy( ais, fzMode ) ) )
						{
							this.pushItemIntoTarget( d, energy, inv, o );
							if( this.itemToSend <= 0 )
							{
								break;
							}
						}
					}
					else
					{
						this.pushItemIntoTarget( d, energy, inv, ais );
					}

					if( this.itemToSend == before && this.isCraftingEnabled() )
					{
						this.didSomething = this.cratingTracker.handleCrafting( x, this.itemToSend, ais, d, this.getTile().getWorldObj(), this.proxy.getGrid(), cg, this.mySrc ) || this.didSomething;
					}
				}
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}

		return this.didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 4, 4, 12, 12, 12, 14 );
		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( 6, 6, 15, 10, 10, 16 );
		bch.addBox( 6, 6, 11, 10, 10, 12 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{

		rh.setTexture( CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 11, 10, 10, 12 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
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

			Platform.openGUI( player, this.getHost().getTile(), this.side, GuiBridge.GUI_BUS );
			return true;
		}

		return false;
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( TickRates.ExportBus.min, TickRates.ExportBus.max, this.isSleeping(), false );
	}

	@Override
	protected boolean isSleeping()
	{
		return this.getHandler() == null || super.isSleeping();
	}

	@Override
	public RedstoneMode getRSMode()
	{
		return (RedstoneMode) this.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		return this.doBusWork();
	}

	private boolean craftOnly()
	{
		return this.getConfigManager().getSetting( Settings.CRAFT_ONLY ) == YesNo.YES;
	}

	private boolean isCraftingEnabled()
	{
		return this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0;
	}

	private void pushItemIntoTarget( InventoryAdaptor d, IEnergyGrid energy, IMEInventory<IAEItemStack> inv, IAEItemStack ais )
	{
		ItemStack is = ais.getItemStack();
		is.stackSize = (int) this.itemToSend;

		ItemStack o = d.simulateAdd( is );
		long canFit = o == null ? this.itemToSend : this.itemToSend - o.stackSize;

		if( canFit > 0 )
		{
			ais = ais.copy();
			ais.setStackSize( canFit );
			IAEItemStack itemsToAdd = Platform.poweredExtraction( energy, inv, ais, this.mySrc );

			if( itemsToAdd != null )
			{
				this.itemToSend -= itemsToAdd.getStackSize();

				ItemStack failed = d.addItems( itemsToAdd.getItemStack() );
				if( failed != null )
				{
					ais.setStackSize( failed.stackSize );
					inv.injectItems( ais, Actionable.MODULATE, this.mySrc );
				}
				else
				{
					this.didSomething = true;
				}
			}
		}
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return this.cratingTracker.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems( ICraftingLink link, IAEItemStack items, Actionable mode )
	{
		InventoryAdaptor d = this.getHandler();

		try
		{
			if( d != null && this.proxy.isActive() )
			{
				IEnergyGrid energy = this.proxy.getEnergy();

				double power = items.getStackSize();
				if( energy.extractAEPower( power, mode, PowerMultiplier.CONFIG ) > power - 0.01 )
				{
					if( mode == Actionable.MODULATE )
					{
						return AEItemStack.create( d.addItems( items.getItemStack() ) );
					}
					return AEItemStack.create( d.simulateAdd( items.getItemStack() ) );
				}
			}
		}
		catch( GridAccessException e )
		{
			AELog.error( e );
		}

		return items;
	}

	@Override
	public void jobStateChange( ICraftingLink link )
	{
		this.cratingTracker.jobStateChange( link );
	}
}
