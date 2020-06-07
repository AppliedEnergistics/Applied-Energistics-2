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


import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.AppEngSlot;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;


public class GuiPatternTerm extends GuiMEMonitorable<ContainerPatternTerm>
{

	private static final String BACKGROUND_CRAFTING_MODE = "guis/pattern.png";
	private static final String BACKGROUND_PROCESSING_MODE = "guis/pattern2.png";

	private static final String SUBSITUTION_DISABLE = "0";
	private static final String SUBSITUTION_ENABLE = "1";

	private static final String CRAFTMODE_CRFTING = "1";
	private static final String CRAFTMODE_PROCESSING = "0";

	private GuiTabButton tabCraftButton;
	private GuiTabButton tabProcessButton;
	private GuiImgButton substitutionsEnabledBtn;
	private GuiImgButton substitutionsDisabledBtn;

	public GuiPatternTerm(ContainerPatternTerm container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.setReservedSpace( 81 );
	}

	@Override
	public void init()
	{
		super.init();

		this.tabCraftButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.CRAFTING_TABLE ), GuiText.CraftingPattern
				.getLocal(), this.itemRenderer, btn -> toggleCraftMode(CRAFTMODE_PROCESSING) );
		this.addButton( this.tabCraftButton );

		this.tabProcessButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.FURNACE ), GuiText.ProcessingPattern
				.getLocal(), this.itemRenderer, btn -> toggleCraftMode(CRAFTMODE_CRFTING) );
		this.addButton( this.tabProcessButton );

		this.substitutionsEnabledBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.ENABLED, btn -> toggleSubstitutions(SUBSITUTION_DISABLE) );
		this.substitutionsEnabledBtn.setHalfSize( true );
		this.addButton( this.substitutionsEnabledBtn );

		this.substitutionsDisabledBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.DISABLED, btn -> toggleSubstitutions(SUBSITUTION_ENABLE) );
		this.substitutionsDisabledBtn.setHalfSize( true );
		this.addButton( this.substitutionsDisabledBtn );

		GuiImgButton clearBtn = new GuiImgButton(this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE, btn -> clear());
		clearBtn.setHalfSize( true );
		this.addButton(clearBtn);

		GuiImgButton encodeBtn = new GuiImgButton(this.guiLeft + 147, this.guiTop + this.ySize - 142, Settings.ACTIONS, ActionItems.ENCODE, btn -> encode());
		this.addButton(encodeBtn);
	}

	private void toggleCraftMode(String mode) {
		NetworkHandler.instance().sendToServer(new PacketValueConfig( "PatternTerminal.CraftMode", mode ) );
	}

	private void toggleSubstitutions(String mode) {
		NetworkHandler.instance().sendToServer(new PacketValueConfig( "PatternTerminal.Substitute", mode ) );
	}

	private void encode() {
		NetworkHandler.instance().sendToServer( new PacketValueConfig( "PatternTerminal.Encode", "1" ) );
	}

	private void clear() {
		NetworkHandler.instance().sendToServer( new PacketValueConfig( "PatternTerminal.Clear", "1" ) );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		if( this.container.isCraftingMode() )
		{
			this.tabCraftButton.visible = true;
			this.tabProcessButton.visible = false;

			if( this.container.substitute )
			{
				this.substitutionsEnabledBtn.visible = true;
				this.substitutionsDisabledBtn.visible = false;
			}
			else
			{
				this.substitutionsEnabledBtn.visible = false;
				this.substitutionsDisabledBtn.visible = true;
			}
		}
		else
		{
			this.tabCraftButton.visible = false;
			this.tabProcessButton.visible = true;
			this.substitutionsEnabledBtn.visible = false;
			this.substitutionsDisabledBtn.visible = false;
		}

		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		this.font.drawString( GuiText.PatternTerminal.getLocal(), 8, this.ySize - 96 + 2 - this.getReservedSpace(), 4210752 );
	}

	@Override
	protected String getBackground()
	{
		if( this.container.isCraftingMode() )
		{
			return BACKGROUND_CRAFTING_MODE;
		}

		return BACKGROUND_PROCESSING_MODE;
	}

	@Override
	protected void repositionSlot( final AppEngSlot s )
	{
		final int offsetPlayerSide = s.isPlayerSide() ? 5 : 3;

		s.yPos = s.getY() + this.ySize - 78 - offsetPlayerSide;
	}
}
