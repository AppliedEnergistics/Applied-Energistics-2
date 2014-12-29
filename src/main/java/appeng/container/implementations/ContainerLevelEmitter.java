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


import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.parts.automation.PartLevelEmitter;
import appeng.util.Platform;

public class ContainerLevelEmitter extends ContainerUpgradeable
{

	final PartLevelEmitter lvlEmitter;

	@SideOnly(Side.CLIENT)
	public GuiTextField textField;

	@SideOnly(Side.CLIENT)
	public void setTextField(GuiTextField level)
	{
		this.textField = level;
		this.textField.setText( String.valueOf( this.EmitterValue ) );
	}

	public ContainerLevelEmitter(InventoryPlayer ip, PartLevelEmitter te) {
		super( ip, te );
		this.lvlEmitter = te;
	}

	@Override
	public int availableUpgrades()
	{

		return 1;
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	public void setLevel(long l, EntityPlayer player)
	{
		this.lvlEmitter.setReportingValue( l );
		this.EmitterValue = l;
	}

	@Override
	protected void setupConfig()
	{
		int x = 80 + 44;
		int y = 40;

		IInventory upgrades = this.upgradeable.getInventoryByName( "upgrades" );
		if ( this.availableUpgrades() > 0 )
			this.addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.invPlayer )).setNotDraggable() );
		if ( this.availableUpgrades() > 1 )
			this.addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.invPlayer )).setNotDraggable() );
		if ( this.availableUpgrades() > 2 )
			this.addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.invPlayer )).setNotDraggable() );
		if ( this.availableUpgrades() > 3 )
			this.addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.invPlayer )).setNotDraggable() );

		IInventory inv = this.upgradeable.getInventoryByName( "config" );
		this.addSlotToContainer( new SlotFakeTypeOnly( inv, 0, x, y ) );
	}

	@GuiSync(2)
	public LevelType lvType;

	@GuiSync(3)
	public long EmitterValue = -1;

	@GuiSync(4)
	public YesNo cmType;

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.EmitterValue = this.lvlEmitter.getReportingValue();
			this.cmType = (YesNo) this.upgradeable.getConfigManager().getSetting( Settings.CRAFT_VIA_REDSTONE );
			this.lvType = (LevelType) this.upgradeable.getConfigManager().getSetting( Settings.LEVEL_TYPE );
			this.fzMode = (FuzzyMode) this.upgradeable.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.rsMode = (RedstoneMode) this.upgradeable.getConfigManager().getSetting( Settings.REDSTONE_EMITTER );
		}

		this.standardDetectAndSendChanges();
	}

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue)
	{
		if ( field.equals( "EmitterValue" ) )
		{
			if ( this.textField != null )
				this.textField.setText( String.valueOf( this.EmitterValue ) );
		}
	}

}
