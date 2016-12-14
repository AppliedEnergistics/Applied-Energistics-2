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

package appeng.client.gui;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotDisconnected;
import appeng.client.me.SlotME;
import appeng.client.render.StackSizeRenderer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngCraftingSlot;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.AppEngSlot.hasCalculatedValidness;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketSwapSlots;
import appeng.helpers.InventoryAction;


public abstract class AEBaseGui extends GuiContainer
{
	private static boolean switchingGuis;
	private final List<InternalSlotME> meSlots = new LinkedList<InternalSlotME>();
	// drag y
	private final Set<Slot> drag_click = new HashSet<Slot>();
	private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
	private GuiScrollbar myScrollBar = null;
	private boolean disableShiftClick = false;
	private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
	private ItemStack dbl_whichItem;
	private Slot bl_clicked;
	private boolean subGui;

	public AEBaseGui( final Container container )
	{
		super( container );
		this.subGui = switchingGuis;
		switchingGuis = false;
	}

	protected static String join( final Collection<String> toolTip, final String delimiter )
	{
		final Joiner joiner = Joiner.on( delimiter );

		return joiner.join( toolTip );
	}

	protected int getQty( final GuiButton btn )
	{
		try
		{
			final DecimalFormat df = new DecimalFormat( "+#;-#" );
			return df.parse( btn.displayString ).intValue();
		}
		catch( final ParseException e )
		{
			return 0;
		}
	}

	public boolean isSubGui()
	{
		return this.subGui;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		final List<Slot> slots = this.getInventorySlots();
		final Iterator<Slot> i = slots.iterator();
		while( i.hasNext() )
		{
			if( i.next() instanceof SlotME )
			{
				i.remove();
			}
		}

		for( final InternalSlotME me : this.meSlots )
		{
			slots.add( new SlotME( me ) );
		}
	}

	@SuppressWarnings( "unchecked" )
	private List<Slot> getInventorySlots()
	{
		return this.inventorySlots.inventorySlots;
	}

	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float btn )
	{
		super.drawScreen( mouseX, mouseY, btn );

		for( final Object c : this.buttonList )
		{
			if( c instanceof ITooltip )
			{
				final ITooltip tooltip = (ITooltip) c;
				final int x = tooltip.xPos(); // ((GuiImgButton) c).xPosition;
				int y = tooltip.yPos(); // ((GuiImgButton) c).yPosition;

				if( x < mouseX && x + tooltip.getWidth() > mouseX && tooltip.isVisible() )
				{
					if( y < mouseY && y + tooltip.getHeight() > mouseY )
					{
						if( y < 15 )
						{
							y = 15;
						}

						final String msg = tooltip.getMessage();
						if( msg != null )
						{
							this.drawTooltip( x + 11, y + 4, msg );
						}
					}
				}
			}
		}
	}

	protected void drawTooltip( int x, int y, String message )
	{
		String[] lines = message.split( "\n" );
		this.drawTooltip( x, y, Arrays.asList( lines ) );
	}

	protected void drawTooltip( int x, int y, List<String> lines )
	{
		if( lines.isEmpty() )
		{
			return;
		}

		// For an explanation of the formatting codes, see http://minecraft.gamepedia.com/Formatting_codes
		lines = Lists.newArrayList( lines ); // Make a copy

		// Make the first line white
		lines.set( 0, TextFormatting.WHITE + lines.get( 0 ) );

		// All lines after the first are colored gray
		for( int i = 1; i < lines.size(); i++ )
		{
			lines.set( i, TextFormatting.GRAY + lines.get( i ) );
		}

		this.drawHoveringText( lines, x, y, fontRendererObj );
	}

	@Override
	protected final void drawGuiContainerForegroundLayer( final int x, final int y )
	{
		final int ox = this.guiLeft; // (width - xSize) / 2;
		final int oy = this.guiTop; // (height - ySize) / 2;
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );

		if( this.getScrollBar() != null )
		{
			this.getScrollBar().draw( this );
		}

		this.drawFG( ox, oy, x, y );
	}

	public abstract void drawFG( int offsetX, int offsetY, int mouseX, int mouseY );

	@Override
	protected final void drawGuiContainerBackgroundLayer( final float f, final int x, final int y )
	{
		final int ox = this.guiLeft; // (width - xSize) / 2;
		final int oy = this.guiTop; // (height - ySize) / 2;
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		this.drawBG( ox, oy, x, y );

		final List<Slot> slots = this.getInventorySlots();
		for( final Slot slot : slots )
		{
			if( slot instanceof OptionalSlotFake )
			{
				final OptionalSlotFake fs = (OptionalSlotFake) slot;
				if( fs.renderDisabled() )
				{
					if( fs.isEnabled() )
					{
						this.drawTexturedModalRect( ox + fs.xDisplayPosition - 1, oy + fs.yDisplayPosition - 1, fs.getSourceX() - 1, fs.getSourceY() - 1, 18,
								18 );
					}
					else
					{
						GlStateManager.color( 1.0F, 1.0F, 1.0F, 0.4F );
						GlStateManager.enableBlend();
						this.drawTexturedModalRect( ox + fs.xDisplayPosition - 1, oy + fs.yDisplayPosition - 1, fs.getSourceX() - 1, fs.getSourceY() - 1, 18,
								18 );
						GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
					}
				}
			}
		}
	}

	@Override
	protected void mouseClicked( final int xCoord, final int yCoord, final int btn ) throws IOException
	{
		this.drag_click.clear();

		if( btn == 1 )
		{
			for( final Object o : this.buttonList )
			{
				final GuiButton guibutton = (GuiButton) o;
				if( guibutton.mousePressed( this.mc, xCoord, yCoord ) )
				{
					super.mouseClicked( xCoord, yCoord, 0 );
					return;
				}
			}
		}

		if( this.getScrollBar() != null )
		{
			this.getScrollBar().click( this, xCoord - this.guiLeft, yCoord - this.guiTop );
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	protected void mouseClickMove( final int x, final int y, final int c, final long d )
	{
		final Slot slot = this.getSlot( x, y );
		final ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

		if( this.getScrollBar() != null )
		{
			this.getScrollBar().click( this, x - this.guiLeft, y - this.guiTop );
		}

		if( slot instanceof SlotFake && itemstack != null )
		{
			this.drag_click.add( slot );
			if( this.drag_click.size() > 1 )
			{
				for( final Slot dr : this.drag_click )
				{
					final PacketInventoryAction p = new PacketInventoryAction( c == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.slotNumber, 0 );
					NetworkHandler.instance().sendToServer( p );
				}
			}
		}
		else
		{
			super.mouseClickMove( x, y, c, d );
		}
	}

	// TODO 1.9.4 aftermath - Whole ClickType thing, to be checked.
	@Override
	protected void handleMouseClick( final Slot slot, final int slotIdx, final int mouseButton, final ClickType clickType )
	{
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		if( slot instanceof SlotFake )
		{
			final InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;

			if( this.drag_click.size() > 1 )
			{
				return;
			}

			final PacketInventoryAction p = new PacketInventoryAction( action, slotIdx, 0 );
			NetworkHandler.instance().sendToServer( p );

			return;
		}

		if( slot instanceof SlotPatternTerm )
		{
			if( mouseButton == 6 )
			{
				return; // prevent weird double clicks..
			}

			try
			{
				NetworkHandler.instance().sendToServer( ( (SlotPatternTerm) slot ).getRequest( isShiftKeyDown() ) );
			}
			catch( final IOException e )
			{
				AELog.debug( e );
			}
		}
		else if( slot instanceof SlotCraftingTerm )
		{
			if( mouseButton == 6 )
			{
				return; // prevent weird double clicks..
			}

			InventoryAction action = null;
			if( isShiftKeyDown() )
			{
				action = InventoryAction.CRAFT_SHIFT;
			}
			else
			{
				// Craft stack on right-click, craft single on left-click
				action = ( mouseButton == 1 ) ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
			}

			final PacketInventoryAction p = new PacketInventoryAction( action, slotIdx, 0 );
			NetworkHandler.instance().sendToServer( p );

			return;
		}

		if( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) )
		{
			if( this.enableSpaceClicking() )
			{
				IAEItemStack stack = null;
				if( slot instanceof SlotME )
				{
					stack = ( (SlotME) slot ).getAEStack();
				}

				int slotNum = this.getInventorySlots().size();

				if( !( slot instanceof SlotME ) && slot != null )
				{
					slotNum = slot.slotNumber;
				}

				( (AEBaseContainer) this.inventorySlots ).setTargetStack( stack );
				final PacketInventoryAction p = new PacketInventoryAction( InventoryAction.MOVE_REGION, slotNum, 0 );
				NetworkHandler.instance().sendToServer( p );
				return;
			}
		}

		if( slot instanceof SlotDisconnected )
		{
			InventoryAction action = null;

			switch( clickType )
			{
				case PICKUP: // pickup / set-down.
					action = ( mouseButton == 1 ) ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
					break;
				case QUICK_MOVE:
					action = ( mouseButton == 1 ) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
					break;

				case CLONE: // creative dupe:

					if( player.capabilities.isCreativeMode )
					{
						action = InventoryAction.CREATIVE_DUPLICATE;
					}

					break;

				default:
				case THROW: // drop item:
			}

			if( action != null )
			{
				final PacketInventoryAction p = new PacketInventoryAction( action, slot.getSlotIndex(), ( (SlotDisconnected) slot ).getSlot().getId() );
				NetworkHandler.instance().sendToServer( p );
			}

			return;
		}

		if( slot instanceof SlotME )
		{
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch( clickType )
			{
				case PICKUP: // pickup / set-down.
					action = ( mouseButton == 1 ) ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
					stack = ( (SlotME) slot ).getAEStack();

					if( stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack() == null )
					{
						action = InventoryAction.AUTO_CRAFT;
					}

					break;
				case QUICK_MOVE:
					action = ( mouseButton == 1 ) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
					stack = ( (SlotME) slot ).getAEStack();
					break;

				case CLONE: // creative dupe:

					stack = ( (SlotME) slot ).getAEStack();
					if( stack != null && stack.isCraftable() )
					{
						action = InventoryAction.AUTO_CRAFT;
					}
					else if( player.capabilities.isCreativeMode )
					{
						final IAEItemStack slotItem = ( (SlotME) slot ).getAEStack();
						if( slotItem != null )
						{
							action = InventoryAction.CREATIVE_DUPLICATE;
						}
					}
					break;

				default:
				case THROW: // drop item:
			}

			if( action != null )
			{
				( (AEBaseContainer) this.inventorySlots ).setTargetStack( stack );
				final PacketInventoryAction p = new PacketInventoryAction( action, this.getInventorySlots().size(), 0 );
				NetworkHandler.instance().sendToServer( p );
			}

			return;
		}

		if( !this.disableShiftClick && isShiftKeyDown() )
		{
			this.disableShiftClick = true;

			if( this.dbl_whichItem == null || this.bl_clicked != slot || this.dbl_clickTimer.elapsed( TimeUnit.MILLISECONDS ) > 150 )
			{
				// some simple double click logic.
				this.bl_clicked = slot;
				this.dbl_clickTimer = Stopwatch.createStarted();
				if( slot != null )
				{
					this.dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : null;
				}
				else
				{
					this.dbl_whichItem = null;
				}
			}
			else if( this.dbl_whichItem != null )
			{
				// a replica of the weird broken vanilla feature.

				final List<Slot> slots = this.getInventorySlots();
				for( final Slot inventorySlot : slots )
				{
					if( inventorySlot != null && inventorySlot.canTakeStack(
							this.mc.thePlayer ) && inventorySlot.getHasStack() && inventorySlot.inventory == slot.inventory && Container.canAddItemToSlot(
									inventorySlot, this.dbl_whichItem, true ) )
					{
						this.handleMouseClick( inventorySlot, inventorySlot.slotNumber, 1, clickType );
					}
				}
			}

			this.disableShiftClick = false;
		}

		super.handleMouseClick( slot, slotIdx, mouseButton, clickType );
	}

	@Override
	protected boolean checkHotbarKeys( final int keyCode )
	{
		final Slot theSlot = this.getSlotUnderMouse();

		if( this.mc.thePlayer.inventory.getItemStack() == null && theSlot != null )
		{
			for( int j = 0; j < 9; ++j )
			{
				if( keyCode == this.mc.gameSettings.keyBindsHotbar[j].getKeyCode() )
				{
					final List<Slot> slots = this.getInventorySlots();
					for( final Slot s : slots )
					{
						if( s.getSlotIndex() == j && s.inventory == ( (AEBaseContainer) this.inventorySlots ).getPlayerInv() )
						{
							if( !s.canTakeStack( ( (AEBaseContainer) this.inventorySlots ).getPlayerInv().player ) )
							{
								return false;
							}
						}
					}

					if( theSlot.getSlotStackLimit() == 64 )
					{
						this.handleMouseClick( theSlot, theSlot.slotNumber, j, ClickType.SWAP );
						return true;
					}
					else
					{
						for( final Slot s : slots )
						{
							if( s.getSlotIndex() == j && s.inventory == ( (AEBaseContainer) this.inventorySlots ).getPlayerInv() )
							{
								NetworkHandler.instance().sendToServer( new PacketSwapSlots( s.slotNumber, theSlot.slotNumber ) );
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		this.subGui = true; // in case the gui is reopened later ( i'm looking at you NEI )
	}

	protected Slot getSlot( final int mouseX, final int mouseY )
	{
		final List<Slot> slots = this.getInventorySlots();
		for( final Slot slot : slots )
		{
			// isPointInRegion
			if( this.isPointInRegion( slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY ) )
			{
				return slot;
			}
		}

		return null;
	}

	public abstract void drawBG( int offsetX, int offsetY, int mouseX, int mouseY );

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();

		final int i = Mouse.getEventDWheel();
		if( i != 0 && isShiftKeyDown() )
		{
			final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
			final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			this.mouseWheelEvent( x, y, i / Math.abs( i ) );
		}
		else if( i != 0 && this.getScrollBar() != null )
		{
			this.getScrollBar().wheel( i );
		}
	}

	private void mouseWheelEvent( final int x, final int y, final int wheel )
	{
		final Slot slot = this.getSlot( x, y );
		if( slot instanceof SlotME )
		{
			final IAEItemStack item = ( (SlotME) slot ).getAEStack();
			if( item != null )
			{
				( (AEBaseContainer) this.inventorySlots ).setTargetStack( item );
				final InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
				final int times = Math.abs( wheel );
				final int inventorySize = this.getInventorySlots().size();
				for( int h = 0; h < times; h++ )
				{
					final PacketInventoryAction p = new PacketInventoryAction( direction, inventorySize, 0 );
					NetworkHandler.instance().sendToServer( p );
				}
			}
		}
	}

	protected boolean enableSpaceClicking()
	{
		return true;
	}

	public void bindTexture( final String base, final String file )
	{
		final ResourceLocation loc = new ResourceLocation( base, "textures/" + file );
		this.mc.getTextureManager().bindTexture( loc );
	}

	protected void drawItem( final int x, final int y, final ItemStack is )
	{
		this.zLevel = 100.0F;
		this.itemRender.zLevel = 100.0F;

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableDepth();
		this.itemRender.renderItemAndEffectIntoGUI( is, x, y );
		GlStateManager.disableDepth();

		this.itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	protected String getGuiDisplayName( final String in )
	{
		return this.hasCustomInventoryName() ? this.getInventoryName() : in;
	}

	private boolean hasCustomInventoryName()
	{
		if( this.inventorySlots instanceof AEBaseContainer )
		{
			return ( (AEBaseContainer) this.inventorySlots ).getCustomName() != null;
		}
		return false;
	}

	private String getInventoryName()
	{
		return ( (AEBaseContainer) this.inventorySlots ).getCustomName();
	}

	/**
	 * This overrides the base-class method through some access transformer hackery...
	 */
	@Override
	public void drawSlot( Slot s )
	{
		if( s instanceof SlotME )
		{

			try
			{
				this.zLevel = 100.0F;
				this.itemRender.zLevel = 100.0F;

				if( !this.isPowered() )
				{
					drawRect( s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66111111 );
				}

				this.zLevel = 0.0F;
				this.itemRender.zLevel = 0.0F;

				// Annoying but easier than trying to splice into render item
				super.drawSlot( new Size1Slot( s ) );

				stackSizeRenderer.renderStackSize( fontRendererObj, ( (SlotME) s ).getAEStack(), s.getStack(), s.xDisplayPosition, s.yDisplayPosition );

			}
			catch( final Exception err )
			{
				AELog.warn( "[AppEng] AE prevented crash while drawing slot: " + err.toString() );
			}

			return;
		}
		else
		{
			try
			{
				final ItemStack is = s.getStack();
				if( s instanceof AppEngSlot && ( ( (AppEngSlot) s ).renderIconWithItem() || is == null ) && ( ( (AppEngSlot) s ).shouldDisplay() ) )
				{
					final AppEngSlot aes = (AppEngSlot) s;
					if( aes.getIcon() >= 0 )
					{
						this.bindTexture( "guis/states.png" );

						try
						{
							final int uv_y = (int) Math.floor( aes.getIcon() / 16 );
							final int uv_x = aes.getIcon() - uv_y * 16;

							GlStateManager.enableBlend();
							GlStateManager.disableLighting();
							GlStateManager.enableTexture2D();
							GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
							GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
							final float par1 = aes.xDisplayPosition;
							final float par2 = aes.yDisplayPosition;
							final float par3 = uv_x * 16;
							final float par4 = uv_y * 16;

							final Tessellator tessellator = Tessellator.getInstance();
							final VertexBuffer vb = tessellator.getBuffer();

							vb.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR );

							final float f1 = 0.00390625F;
							final float f = 0.00390625F;
							final float par6 = 16;
							vb.pos( par1 + 0, par2 + par6, this.zLevel ).tex( ( par3 + 0 ) * f, ( par4 + par6 ) * f1 ).color( 1.0f, 1.0f, 1.0f,
									aes.getOpacityOfIcon() ).endVertex();
							final float par5 = 16;
							vb.pos( par1 + par5, par2 + par6, this.zLevel ).tex( ( par3 + par5 ) * f, ( par4 + par6 ) * f1 ).color( 1.0f, 1.0f, 1.0f,
									aes.getOpacityOfIcon() ).endVertex();
							vb.pos( par1 + par5, par2 + 0, this.zLevel ).tex( ( par3 + par5 ) * f, ( par4 + 0 ) * f1 ).color( 1.0f, 1.0f, 1.0f,
									aes.getOpacityOfIcon() ).endVertex();
							vb.pos( par1 + 0, par2 + 0, this.zLevel ).tex( ( par3 + 0 ) * f, ( par4 + 0 ) * f1 ).color( 1.0f, 1.0f, 1.0f,
									aes.getOpacityOfIcon() ).endVertex();
							tessellator.draw();

						}
						catch( final Exception err )
						{
						}
					}
				}

				if( is != null && s instanceof AppEngSlot )
				{
					if( ( (AppEngSlot) s ).getIsValid() == hasCalculatedValidness.NotAvailable )
					{
						boolean isValid = s.isItemValid(
								is ) || s instanceof SlotOutput || s instanceof AppEngCraftingSlot || s instanceof SlotDisabled || s instanceof SlotInaccessible || s instanceof SlotFake || s instanceof SlotRestrictedInput || s instanceof SlotDisconnected;
						if( isValid && s instanceof SlotRestrictedInput )
						{
							try
							{
								isValid = ( (SlotRestrictedInput) s ).isValid( is, this.mc.theWorld );
							}
							catch( final Exception err )
							{
								AELog.debug( err );
							}
						}
						( (AppEngSlot) s ).setIsValid( isValid ? hasCalculatedValidness.Valid : hasCalculatedValidness.Invalid );
					}

					if( ( (AppEngSlot) s ).getIsValid() == hasCalculatedValidness.Invalid )
					{
						this.zLevel = 100.0F;
						this.itemRender.zLevel = 100.0F;

						GlStateManager.disableLighting();
						drawRect( s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66ff6666 );
						GlStateManager.enableLighting();

						this.zLevel = 0.0F;
						this.itemRender.zLevel = 0.0F;
					}
				}

				if( s instanceof AppEngSlot )
				{
					( (AppEngSlot) s ).setDisplay( true );
					super.drawSlot( s );
				}
				else
				{
					super.drawSlot( s );
				}

				return;
			}
			catch( final Exception err )
			{
				AELog.warn( "[AppEng] AE prevented crash while drawing slot: " + err.toString() );
			}
		}
		// do the usual for non-ME Slots.
		super.drawSlot( s );
	}

	protected boolean isPowered()
	{
		return true;
	}

	public void bindTexture( final String file )
	{
		final ResourceLocation loc = new ResourceLocation( AppEng.MOD_ID, "textures/" + file );
		this.mc.getTextureManager().bindTexture( loc );
	}

	protected GuiScrollbar getScrollBar()
	{
		return this.myScrollBar;
	}

	protected void setScrollBar( final GuiScrollbar myScrollBar )
	{
		this.myScrollBar = myScrollBar;
	}

	protected List<InternalSlotME> getMeSlots()
	{
		return this.meSlots;
	}

	public static final synchronized boolean isSwitchingGuis()
	{
		return switchingGuis;
	}

	public static final synchronized void setSwitchingGuis( final boolean switchingGuis )
	{
		AEBaseGui.switchingGuis = switchingGuis;
	}
}
