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

package appeng.client.gui.implementations;


import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.parts.automation.PartImportBus;


public class GuiUpgradeable extends AEBaseGui
{

	final ContainerUpgradeable cvb;
	final IUpgradeableHost bc;

	GuiImgButton redstoneMode;
	GuiImgButton fuzzyMode;
	GuiImgButton craftMode;

	public GuiUpgradeable( InventoryPlayer inventoryPlayer, IUpgradeableHost te )
	{
		this( new ContainerUpgradeable( inventoryPlayer, te ) );
	}

	public GuiUpgradeable( ContainerUpgradeable te )
	{
		super( te );
		this.cvb = te;

		this.bc = (IUpgradeableHost) te.getTarget();
		this.xSize = this.hasToolbox() ? 246 : 211;
		this.ySize = 184;
	}

	protected boolean hasToolbox()
	{
		return ( (ContainerUpgradeable) this.inventorySlots ).hasToolbox();
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.addButtons();
	}

	protected void addButtons()
	{
		this.redstoneMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.craftMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.CRAFT_ONLY, YesNo.NO );

		this.buttonList.add( this.craftMode );
		this.buttonList.add( this.redstoneMode );
		this.buttonList.add( this.fuzzyMode );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( this.getName().getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if( this.redstoneMode != null )
			this.redstoneMode.set( this.cvb.rsMode );

		if( this.fuzzyMode != null )
			this.fuzzyMode.set( this.cvb.fzMode );

		if( this.craftMode != null )
			this.craftMode.set( this.cvb.cMode );
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.handleButtonVisibility();

		this.bindTexture( this.getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, this.ySize );
		if( this.drawUpgrades() )
			this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 14 + this.cvb.availableUpgrades() * 18 );
		if( this.hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68 );
	}

	protected void handleButtonVisibility()
	{
		if( this.redstoneMode != null )
			this.redstoneMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 );
		if( this.fuzzyMode != null )
			this.fuzzyMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
		if( this.craftMode != null )
			this.craftMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 );
	}

	protected String getBackground()
	{
		return "guis/bus.png";
	}

	protected boolean drawUpgrades()
	{
		return true;
	}

	protected GuiText getName()
	{
		return this.bc instanceof PartImportBus ? GuiText.ImportBus : GuiText.ExportBus;
	}

	@Override
	protected void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.redstoneMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.redstoneMode.getSetting(), backwards ) );

		if( btn == this.craftMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.craftMode.getSetting(), backwards ) );

		if( btn == this.fuzzyMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.fuzzyMode.getSetting(), backwards ) );
	}
}
