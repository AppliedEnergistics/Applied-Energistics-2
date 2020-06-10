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


import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotDisconnected;
import appeng.client.me.SlotME;
import appeng.client.render.StackSizeRenderer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.*;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketSwapSlots;
import appeng.fluids.client.render.FluidStackSizeRenderer;
import appeng.helpers.InventoryAction;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.java.games.input.Keyboard;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public abstract class AEBaseGui extends ChestScreen
{
	private final List<InternalSlotME> meSlots = new ArrayList<>();
	// drag y
	private final Set<Slot> drag_click = new HashSet<>();
	private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
	private final FluidStackSizeRenderer fluidStackSizeRenderer = new FluidStackSizeRenderer();
	private GuiScrollbar myScrollBar = null;
	private boolean disableShiftClick = false;
	private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
	private ItemStack dbl_whichItem = ItemStack.EMPTY;
	private Slot bl_clicked;
	protected final List<GuiCustomSlot> guiSlots = new ArrayList<>();
	protected float zLevel = 0.0F;

	public AEBaseGui( final Container container )
	{
		super( container, player, title );
	}

	protected static String join( final Collection<String> toolTip, final String delimiter )
	{
		final Joiner joiner = Joiner.on( delimiter );

		return joiner.join( toolTip );
	}

	protected int getQty( final Button btn )
	{
		try
		{
			final DecimalFormat df = new DecimalFormat( "+#;-#" );
			return df.parse( btn.getMessage() ).intValue();
		}
		catch( final ParseException e )
		{
			return 0;
		}
	}

	@Override
	public void init()
	{
		super.init();

		final List<Slot> slots = this.container.inventorySlots;
		slots.removeIf( slot -> slot instanceof SlotME );

		for( final InternalSlotME me : this.meSlots )
		{
			slots.add( new SlotME( me ) );
		}
	}

	private List<Slot> getInventorySlots()
	{
		return this.container.inventorySlots;
	}

	@Override
	public void render( final int mouseX, final int mouseY, final float partialTicks )
	{
		super.renderBackground();
		super.drawGuiContainerBackgroundLayer( partialTicks, mouseX, mouseY);

		GlStateManager.pushMatrix();
		GlStateManager.translatef( this.guiLeft, this.guiTop, 0.0F );
		GlStateManager.enableDepthTest();
		for( final GuiCustomSlot c : this.guiSlots )
		{
			this.drawGuiSlot( c, mouseX, mouseY, partialTicks );
		}
		GlStateManager.disableDepthTest();
		for( final GuiCustomSlot c : this.guiSlots )
		{
			this.drawTooltip( c, mouseX - this.guiLeft, mouseY - this.guiTop );
		}
		GlStateManager.popMatrix();

		this.renderHoveredToolTip( mouseX, mouseY );

		for( final Object c : this.buttons )
		{
			if( c instanceof ITooltip )
			{
				this.drawTooltip( (ITooltip) c, mouseX, mouseY );
			}
		}
	}

	protected void drawGuiSlot( GuiCustomSlot slot, int mouseX, int mouseY, float partialTicks )
	{
		if( slot.isSlotEnabled() )
		{
			final int left = slot.xPos();
			final int top = slot.yPos();
			final int right = left + slot.getWidth();
			final int bottom = top + slot.getHeight();

			slot.drawContent( this.minecraft, mouseX, mouseY, partialTicks );

			if( this.isPointInRegion( left, top, slot.getWidth(), slot.getHeight(), mouseX, mouseY ) && slot.canClick( this.minecraft.player ) )
			{
				GlStateManager.disableLighting();
				GlStateManager.colorMask( true, true, true, false );
				this.fillGradient( left, top, right, bottom, -2130706433, -2130706433 );
				GlStateManager.colorMask( true, true, true, true );
				GlStateManager.enableLighting();
			}
		}
	}

	private void drawTooltip( ITooltip tooltip, int mouseX, int mouseY )
	{
		final int x = tooltip.xPos(); // ((GuiImgButton) c).x;
		int y = tooltip.yPos(); // ((GuiImgButton) c).y;

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

		//List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font)
		GuiUtils.drawHoveringText( lines, x, y, this.font );
	}

	@Override
	protected final void drawGuiContainerForegroundLayer( final int x, final int y )
	{
		final int ox = this.guiLeft; // (width - xSize) / 2;
		final int oy = this.guiTop; // (height - ySize) / 2;
		GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 1.0F );

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
		GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.drawBG( ox, oy, x, y );

		final List<Slot> slots = this.container.inventorySlots;
		for( final Slot slot : slots )
		{
			if( slot instanceof IOptionalSlot )
			{
				final IOptionalSlot optionalSlot = (IOptionalSlot) slot;
				if( optionalSlot.isRenderDisabled() )
				{
					final AppEngSlot aeSlot = (AppEngSlot) slot;
					if( aeSlot.isSlotEnabled() )
					{
						GuiUtils.drawTexturedModalRect( ox + aeSlot.xPos - 1, oy + aeSlot.yPos - 1,
								optionalSlot.getSourceX() - 1, optionalSlot.getSourceY() - 1,
								18,  18, this.zLevel );
					}
					else
					{
						GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 0.4F );
						GlStateManager.enableBlend();
						GuiUtils.drawTexturedModalRect( ox + aeSlot.xPos - 1, oy + aeSlot.yPos - 1,
								optionalSlot.getSourceX() - 1, optionalSlot.getSourceY() - 1,
								18, 18 , this.zLevel );
						GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
					}
				}
			}
		}

		for( final GuiCustomSlot slot : this.guiSlots )
		{
			slot.drawBackground( ox, oy );
		}

	}

	@Override public boolean mouseClicked( final double xCoord, final double yCoord, final int btn )
	{
		this.drag_click.clear();

		if( btn == 1 )
		{
			for( final Object o : this.buttons )
			{
				final Button guibutton = (Button) o;
				if( guibutton.mousePressed( this.minecraft, xCoord, yCoord ) )
				{
					return super.mouseClicked( xCoord, yCoord, 0 );
				}
			}
		}

		for( GuiCustomSlot slot : this.guiSlots )
		{
			if( this.isPointInRegion( slot.xPos(), slot.yPos(), slot.getWidth(), slot.getHeight(), xCoord, yCoord ) && slot.canClick( this.minecraft.player ) )
			{
				slot.slotClicked( this.minecraft.player.inventory.getItemStack(), btn );
			}
		}

		if( this.getScrollBar() != null )
		{
			this.getScrollBar().click( this, (int) xCoord - this.guiLeft, (int) yCoord - this.guiTop );
		}

		return super.mouseClicked( xCoord, yCoord, btn );
	}

	//TODO
	@Override
	public boolean mouseDragged( final double x, final double y, final int c, final double d, final double idk )
	{
		final Slot slot = this.getSlot( x, y );
		final ItemStack itemstack = this.minecraft.player.inventory.getItemStack();

		if( this.getScrollBar() != null )
		{
			this.getScrollBar().click( this, x - this.guiLeft, y - this.guiTop );
		}

		if( slot instanceof SlotFake && !itemstack.isEmpty() )
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
			return super.mouseDragged( x, y, c, d, idk);
		}
	}

	// TODO 1.9.4 aftermath - Whole ClickType thing, to be checked.
	// TODO oh man 1.15.2 stuff 2...
	@Override
	protected void handleMouseClick( @Nonnull final Slot slot, final int slotIdx, final int mouseButton, @Nonnull final ClickType clickType )
	{
		final PlayerEntity player = this.minecraft.player;

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

				int slotNum = this.container.inventorySlots.size();

				if( !( slot instanceof SlotME ) && slot != null )
				{
					slotNum = slot.slotNumber;
				}

				( (AEBaseContainer) this.container.inventorySlots ).setTargetStack( stack );
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

					if( stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack()
							.isEmpty() )
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
				( (AEBaseContainer) this.container.inventorySlots ).setTargetStack( stack );
				final PacketInventoryAction p = new PacketInventoryAction( action, this.container.inventorySlots.size(), 0 );
				NetworkHandler.instance().sendToServer( p );
			}

			return;
		}

		if( !this.disableShiftClick && isShiftKeyDown() && mouseButton == 0 )
		{
			this.disableShiftClick = true;

			if( this.dbl_whichItem.isEmpty() || this.bl_clicked != slot || this.dbl_clickTimer.elapsed( TimeUnit.MILLISECONDS ) > 250 )
			{
				// some simple double click logic.
				this.bl_clicked = slot;
				this.dbl_clickTimer = Stopwatch.createStarted();
				if( slot != null )
				{
					this.dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
				}
				else
				{
					this.dbl_whichItem = ItemStack.EMPTY;
				}
			}
			else if( !this.dbl_whichItem.isEmpty() )
			{
				// a replica of the weird broken vanilla feature.

				final List<Slot> slots = this.container.inventorySlots;
				for( final Slot inventorySlot : slots )
				{
					if( inventorySlot != null && inventorySlot.canTakeStack(
							this.minecraft.player ) && inventorySlot.getHasStack() && inventorySlot.isSameInventory( slot ) && Container.canAddItemToSlot(
									inventorySlot, this.dbl_whichItem, true ) )
					{
						this.handleMouseClick( inventorySlot, inventorySlot.slotNumber, 0, ClickType.QUICK_MOVE );
					}
				}
				this.dbl_whichItem = ItemStack.EMPTY;
			}

			this.disableShiftClick = false;
		}

		super.handleMouseClick( slot, slotIdx, mouseButton, clickType );
	}

	@Override
	protected boolean checkHotbarKeys( final int keyCode )
	{
		final Slot theSlot = this.getSlotUnderMouse();

		if( this.minecraft.player.inventory.getItemStack().isEmpty() && theSlot != null )
		{
			for( int j = 0; j < 9; ++j )
			{
				if( keyCode == this.minecraft.gameSettings.keyBindsHotbar[j].getKeyCode() )
				{
					final List<Slot> slots = this.container.inventorySlots;
					for( final Slot s : slots )
					{
						if( s.getSlotIndex() == j && s.inventory == ( (AEBaseContainer) this.container.inventorySlots ).getPlayerInv() )
						{
							if( !s.canTakeStack( ( (AEBaseContainer) this.container.inventorySlots ).getPlayerInv().player ) )
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
							if( s.getSlotIndex() == j && s.inventory == ( (AEBaseContainer) this.container.inventorySlots ).getPlayerInv() )
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
	}

	protected Slot getSlot( final double mouseX, final double mouseY )
	{
		final List<Slot> slots = this.container.inventorySlots;
		for( final Slot slot : slots )
		{
			// isPointInRegion
			if( this.isPointInRegion( slot.xPos, slot.yPos, 16, 16, mouseX, mouseY ) )
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
			final int x = Mouse.getEventX() * this.width / this.minecraft.displayWidth;
			final int y = this.height - Mouse.getEventY() * this.height / this.minecraft.displayHeight - 1;
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
				( (AEBaseContainer) this.container.inventorySlots ).setTargetStack( item );
				final InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
				final int times = Math.abs( wheel );
				final int inventorySize = this.container.inventorySlots.size();
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
		this.minecraft.getTextureManager().bindTexture( loc );
	}

	protected void drawItem( final int x, final int y, final ItemStack is )
	{
		this.zLevel = 100.0F;
		this.itemRenderer.zLevel = 100.0F;

		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableDepthTest();
		this.itemRenderer.renderItemAndEffectIntoGUI( is, x, y );
		GlStateManager.disableDepthTest();

		this.itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	protected String getGuiDisplayName( final String in )
	{
		return this.hasCustomInventoryName() ? this.getInventoryName() : in;
	}

	private boolean hasCustomInventoryName()
	{
		if( this.container.inventorySlots instanceof AEBaseContainer )
		{
			return ( (AEBaseContainer) this.container.inventorySlots ).getCustomName() != null;
		}
		return false;
	}

	private String getInventoryName()
	{
		return ( (AEBaseContainer) this.container.inventorySlots ).getCustomName();
	}

	/**
	 * This overrides the base-class method through some access transformer hackery...
	 */
	// TODO fix this - drawSlot is now private
	/*
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
					drawRect( s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111 );
				}

				this.zLevel = 0.0F;
				this.itemRender.zLevel = 0.0F;

				// Annoying but easier than trying to splice into render item
				super.drawSlot( new Size1Slot( (SlotME) s ) );

				this.stackSizeRenderer.renderStackSize( this.fontRenderer, ( (SlotME) s ).getAEStack(), s.xPos, s.yPos );

			}
			catch( final Exception err )
			{
				AELog.warn( "[AppEng] AE prevented crash while drawing slot: " + err.toString() );
			}

			return;
		}
		else if( s instanceof IMEFluidSlot && ( (IMEFluidSlot) s ).shouldRenderAsFluid() )
		{
			final IMEFluidSlot slot = (IMEFluidSlot) s;
			final IAEFluidStack fs = slot.getAEFluidStack();

			if( fs != null && this.isPowered() )
			{
				GlStateManager.disableLighting();
				GlStateManager.disableBlend();
				final Fluid fluid = fs.getFluid();
				Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.LOCATION_BLOCKS_TEXTURE );
				final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( fluid.getStill().toString() );

				// Set color for dynamic fluids
				// Convert int color to RGB
				float red = ( fluid.getColor() >> 16 & 255 ) / 255.0F;
				float green = ( fluid.getColor() >> 8 & 255 ) / 255.0F;
				float blue = ( fluid.getColor() & 255 ) / 255.0F;
				GlStateManager.color( red, green, blue );

				this.drawTexturedModalRect( s.xPos, s.yPos, sprite, 16, 16 );
				GlStateManager.enableLighting();
				GlStateManager.enableBlend();

				this.fluidStackSizeRenderer.renderStackSize( this.fontRenderer, fs, s.xPos, s.yPos );
			}
			else if( !this.isPowered() )
			{
				drawRect( s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111 );
			}

			return;
		}
		else
		{
			try
			{
				final ItemStack is = s.getStack();
				if( s instanceof AppEngSlot && ( ( (AppEngSlot) s ).renderIconWithItem() || is.isEmpty() ) && ( ( (AppEngSlot) s ).shouldDisplay() ) )
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
							final float par1 = aes.xPos;
							final float par2 = aes.yPos;
							final float par3 = uv_x * 16;
							final float par4 = uv_y * 16;

							final Tessellator tessellator = Tessellator.getInstance();
							final BufferBuilder vb = tessellator.getBuffer();

							vb.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR );

							final float f1 = 0.00390625F;
							final float f = 0.00390625F;
							final float par6 = 16;
							vb.pos( par1 + 0, par2 + par6, this.zLevel )
									.tex( ( par3 + 0 ) * f, ( par4 + par6 ) * f1 )
									.color( 1.0f, 1.0f, 1.0f,
											aes.getOpacityOfIcon() )
									.endVertex();
							final float par5 = 16;
							vb.pos( par1 + par5, par2 + par6, this.zLevel )
									.tex( ( par3 + par5 ) * f, ( par4 + par6 ) * f1 )
									.color( 1.0f, 1.0f, 1.0f,
											aes.getOpacityOfIcon() )
									.endVertex();
							vb.pos( par1 + par5, par2 + 0, this.zLevel )
									.tex( ( par3 + par5 ) * f, ( par4 + 0 ) * f1 )
									.color( 1.0f, 1.0f, 1.0f,
											aes.getOpacityOfIcon() )
									.endVertex();
							vb.pos( par1 + 0, par2 + 0, this.zLevel )
									.tex( ( par3 + 0 ) * f, ( par4 + 0 ) * f1 )
									.color( 1.0f, 1.0f, 1.0f,
											aes.getOpacityOfIcon() )
									.endVertex();
							tessellator.draw();

						}
						catch( final Exception ignored )
						{
						}
					}
				}

				if( !is.isEmpty() && s instanceof AppEngSlot )
				{
					if( ( (AppEngSlot) s ).getIsValid() == hasCalculatedValidness.NotAvailable )
					{
						boolean isValid = s.isItemValid(
								is ) || s instanceof SlotOutput || s instanceof AppEngCraftingSlot || s instanceof SlotDisabled || s instanceof SlotInaccessible || s instanceof SlotFake || s instanceof SlotRestrictedInput || s instanceof SlotDisconnected;
						if( isValid && s instanceof SlotRestrictedInput )
						{
							try
							{
								isValid = ( (SlotRestrictedInput) s ).isValid( is, this.minecraft.world );
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
						drawRect( s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66ff6666 );
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
	}*/

	protected boolean isPowered()
	{
		return true;
	}

	public void bindTexture( final String file )
	{
		final ResourceLocation loc = new ResourceLocation( AppEng.MOD_ID, "textures/" + file );
		this.minecraft.getTextureManager().bindTexture( loc );
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
}
