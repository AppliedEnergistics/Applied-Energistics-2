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


import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.SlotFake;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.block.Block;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
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
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartImportBus;


public class GuiUpgradeable extends AEBaseGui implements IJEIGhostIngredients
{
	private final Map<Target<?>,Object> mapTargetSlot = new HashMap<>();
	protected final ContainerUpgradeable cvb;
	protected final IUpgradeableHost bc;

	protected GuiImgButton redstoneMode;
	protected GuiImgButton fuzzyMode;
	protected GuiImgButton craftMode;
	protected GuiImgButton schedulingMode;

	public GuiUpgradeable( final InventoryPlayer inventoryPlayer, final IUpgradeableHost te )
	{
		this( new ContainerUpgradeable( inventoryPlayer, te ) );
	}

	public GuiUpgradeable( final ContainerUpgradeable te )
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
		this.schedulingMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 68, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT );

		this.buttonList.add( this.craftMode );
		this.buttonList.add( this.redstoneMode );
		this.buttonList.add( this.fuzzyMode );
		this.buttonList.add( this.schedulingMode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.fontRenderer.drawString( this.getGuiDisplayName( this.getName().getLocal() ), 8, 6, 4210752 );
		this.fontRenderer.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if( this.redstoneMode != null )
		{
			this.redstoneMode.set( this.cvb.getRedStoneMode() );
		}

		if( this.fuzzyMode != null )
		{
			this.fuzzyMode.set( this.cvb.getFuzzyMode() );
		}

		if( this.craftMode != null )
		{
			this.craftMode.set( this.cvb.getCraftingMode() );
		}

		if( this.schedulingMode != null )
		{
			this.schedulingMode.set( this.cvb.getSchedulingMode() );
		}
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.handleButtonVisibility();

		this.bindTexture( this.getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, this.ySize );
		if( this.drawUpgrades() )
		{
			this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 14 + this.cvb.availableUpgrades() * 18 );
		}
		if( this.hasToolbox() )
		{
			this.drawTexturedModalRect( offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68 );
		}
	}

	protected void handleButtonVisibility()
	{
		if( this.redstoneMode != null )
		{
			this.redstoneMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 );
		}
		if( this.fuzzyMode != null )
		{
			this.fuzzyMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
		}
		if( this.craftMode != null )
		{
			this.craftMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 );
		}
		if( this.schedulingMode != null )
		{
			this.schedulingMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.CAPACITY ) > 0 && this.bc instanceof PartExportBus );
		}
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
	protected void actionPerformed( final GuiButton btn ) throws IOException
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.redstoneMode )
		{
			NetworkHandler.instance().sendToServer( new PacketConfigButton( this.redstoneMode.getSetting(), backwards ) );
		}

		if( btn == this.craftMode )
		{
			NetworkHandler.instance().sendToServer( new PacketConfigButton( this.craftMode.getSetting(), backwards ) );
		}

		if( btn == this.fuzzyMode )
		{
			NetworkHandler.instance().sendToServer( new PacketConfigButton( this.fuzzyMode.getSetting(), backwards ) );
		}

		if( btn == this.schedulingMode )
		{
			NetworkHandler.instance().sendToServer( new PacketConfigButton( this.schedulingMode.getSetting(), backwards ) );
		}
	}

	@Override
	public List<Target<?>> getPhantomTargets(Object ingredient)
	{
		mapTargetSlot.clear();

		FluidStack fluidStack = null;
		ItemStack itemStack = ItemStack.EMPTY;

		if( ingredient instanceof ItemStack )
		{
			itemStack = (ItemStack) ingredient;

			if( itemStack.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) )
			{
				fluidStack = ( itemStack.getCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ).drain( Integer.MAX_VALUE, false ) );
			}
			if( fluidStack == null )
			{
				NBTTagCompound tagCompound = itemStack.getTagCompound();
				if( tagCompound != null && tagCompound.hasKey( "Fluid" ) )
				{
					Fluid fluid = FluidRegistry.getFluid( tagCompound.getString( "Fluid" ) );
					fluidStack = new FluidStack( fluid, 1000 );
				}
			}
			if( fluidStack == null )
			{
				Block maybeFluidBlock = Block.getBlockFromItem( itemStack.getItem() );
				if( Block.getBlockFromItem( itemStack.getItem() ) instanceof BlockFluidBase )
				{
					Fluid fluid = ( (BlockFluidBase) maybeFluidBlock ).getFluid();
					fluidStack = new FluidStack( fluid, 1000 );
				}
			}
		}
		else if( ingredient instanceof FluidStack )
		{
			fluidStack = (FluidStack) ingredient;
		}

		List<Target<?>> targets = new ArrayList<>();

		if( !itemStack.isEmpty() )
		{
			for( Slot slot : this.inventorySlots.inventorySlots )
			{
				if( slot instanceof SlotFake )
				{
					ItemStack finalItemStack = itemStack;
					Target<Object> targetItem = new Target<Object>()
					{
						@Override
						public Rectangle getArea()
						{
							return new Rectangle( getGuiLeft() + slot.xPos, getGuiTop() + slot.yPos, 16, 16 );
						}

						@Override
						public void accept( Object ingredient )
						{
							final PacketInventoryAction p;
							try
							{
								p = new PacketInventoryAction( InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotFake) slot, AEItemStack.fromItemStack( finalItemStack ) );
								NetworkHandler.instance().sendToServer( p );

							}
							catch( IOException e )
							{
								e.printStackTrace();
							}
						}
					};
					targets.add( targetItem );
					mapTargetSlot.putIfAbsent( targetItem, slot );
				}
			}
		}
		if( fluidStack != null )
		{
			for( GuiCustomSlot guiCustomSlot : this.guiSlots )
			{
				if( guiCustomSlot instanceof GuiFluidSlot )
				{
					FluidStack finalFluidStack = fluidStack;
					Target<Object> targetFluid = new Target<Object>()
					{
						@Override
						public Rectangle getArea()
						{
							return new Rectangle( getGuiLeft() + guiCustomSlot.xPos(), getGuiTop() + guiCustomSlot.yPos(), 16, 16 );
						}

						@Override
						public void accept( Object ingredient )
						{
							final PacketInventoryAction p;
							try
							{
								p = new PacketInventoryAction( InventoryAction.PLACE_JEI_GHOST_ITEM, (GuiFluidSlot) guiCustomSlot, AEItemStack.fromItemStack(AEFluidStack.fromFluidStack( finalFluidStack ).asItemStackRepresentation() ));
								NetworkHandler.instance().sendToServer( p );

							}
							catch( IOException e )
							{
								e.printStackTrace();
							}
						}
					};
					targets.add( targetFluid );
					mapTargetSlot.putIfAbsent( targetFluid, guiCustomSlot );
				}
			}
		}
		return targets;
	}

	@Override
	public Map<Target<?>, Object> getFakeSlotTargetMap()
	{
		return mapTargetSlot;
	}
}
