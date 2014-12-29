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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
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
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

public class PartImportBus extends PartSharedItemBus implements IInventoryDestination
{

	final BaseActionSource mySrc;
	IMEInventory<IAEItemStack> destination = null;
	IAEItemStack lastItemChecked = null;

	public PartImportBus(ItemStack is) {
		super( PartImportBus.class, is );
		this.settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.settings.registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.mySrc = new MachineSource( this );
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( player, this.getHost().getTile(), this.side, GuiBridge.GUI_BUS );
			return true;
		}

		return false;
	}

	@Override
	public boolean canInsert(ItemStack stack)
	{
		if ( stack == null || stack.getItem() == null )
			return false;
		
		IAEItemStack out = this.destination.injectItems( this.lastItemChecked = AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE, this.mySrc );
		if ( out == null )
			return true;
		return out.getStackSize() != stack.stackSize;
	}

	private IInventoryDestination configDestination( IMEMonitor<IAEItemStack> itemInventory )
	{
		this.destination = itemInventory;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				this.is.getIconIndex(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 4, 4, 14, 12, 12, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				this.is.getIconIndex(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon() );

		rh.setBounds( 4, 4, 14, 12, 12, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 12, 10, 10, 13 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 11, 10, 10, 12 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 6, 6, 11, 10, 10, 13 );
		bch.addBox( 5, 5, 13, 11, 11, 14 );
		bch.addBox( 4, 4, 14, 12, 12, 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.ImportBus.min, TickRates.ImportBus.max, this.getHandler() == null, false );
	}

	private int itemToSend; // used in tickingRequest
	private boolean worked; // used in tickingRequest

	@Override
	TickRateModulation doBusWork()
	{
		if ( !this.proxy.isActive() )
			return TickRateModulation.IDLE;

		this.worked = false;

		InventoryAdaptor myAdaptor = this.getHandler();
		FuzzyMode fzMode = (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE );

		if ( myAdaptor != null )
		{
			try
			{
				switch (this.getInstalledUpgrades( Upgrades.SPEED ))
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

				this.itemToSend = Math.min( this.itemToSend, (int) (0.01 + this.proxy.getEnergy().extractAEPower( this.itemToSend, Actionable.SIMULATE, PowerMultiplier.CONFIG )) );
				IMEMonitor<IAEItemStack> inv = this.proxy.getStorage().getItemInventory();
				IEnergyGrid energy = this.proxy.getEnergy();

				boolean Configured = false;
				for (int x = 0; x < this.availableSlots(); x++)
				{
					IAEItemStack ais = this.config.getAEStackInSlot( x );
					if ( ais != null && this.itemToSend > 0 )
					{
						Configured = true;
						while (this.itemToSend > 0)
						{
							if ( this.importStuff( myAdaptor, ais, inv, energy, fzMode ) )
								break;
						}
					}
				}

				if ( !Configured )
				{
					while (this.itemToSend > 0)
					{
						if ( this.importStuff( myAdaptor, null, inv, energy, fzMode ) )
							break;
					}
				}
			}
			catch (GridAccessException e)
			{
				// :3
			}
		}
		else
			return TickRateModulation.SLEEP;

		return this.worked ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return this.doBusWork();
	}

	private boolean importStuff(InventoryAdaptor myAdaptor, IAEItemStack whatToImport, IMEMonitor<IAEItemStack> inv, IEnergySource energy, FuzzyMode fzMode)
	{
		int toSend = this.itemToSend;

		if ( toSend > 64 )
			toSend = 64;

		ItemStack newItems;
		if ( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
			newItems = myAdaptor.removeSimilarItems( toSend, whatToImport == null ? null : whatToImport.getItemStack(), fzMode, this.configDestination( inv ) );
		else
			newItems = myAdaptor.removeItems( toSend, whatToImport == null ? null : whatToImport.getItemStack(), this.configDestination( inv ) );

		if ( newItems != null )
		{
			newItems.stackSize = (int) (Math.min( newItems.stackSize, energy.extractAEPower( newItems.stackSize, Actionable.SIMULATE, PowerMultiplier.CONFIG ) ) + 0.01);
			this.itemToSend -= newItems.stackSize;

			if ( this.lastItemChecked == null || !this.lastItemChecked.isSameType( newItems ) )
				this.lastItemChecked = AEApi.instance().storage().createItemStack( newItems );
			else
				this.lastItemChecked.setStackSize( newItems.stackSize );

			IAEItemStack failed = Platform.poweredInsert( energy, this.destination, this.lastItemChecked, this.mySrc );
			// destination.injectItems( lastItemChecked, Actionable.MODULATE );
			if ( failed != null )
			{
				myAdaptor.addItems( failed.getItemStack() );
				return true;
			}
			else
				this.worked = true;
		}
		else
			return true;

		return false;
	}

	@Override
	public RedstoneMode getRSMode()
	{
		return (RedstoneMode) this.settings.getSetting( Settings.REDSTONE_CONTROLLED );
	}

	@Override
	protected boolean isSleeping()
	{
		return this.getHandler() == null || super.isSleeping();
	}

}
