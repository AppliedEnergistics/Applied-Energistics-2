package appeng.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import appeng.container.slot.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotDisconnected;
import appeng.client.me.SlotME;
import appeng.client.render.AppEngRenderItem;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot.hasCalculatedValidness;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketSwapSlots;
import appeng.helpers.InventoryAction;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.INEI;
import appeng.util.Platform;

import com.google.common.base.Stopwatch;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

public abstract class AEBaseGui extends GuiContainer
{

	protected final List<InternalSlotME> meSlots = new LinkedList<InternalSlotME>();
	protected GuiScrollbar myScrollBar = null;
	static public boolean switchingGuis;
	private boolean subGui;

	public AEBaseGui(Container container)
	{
		super( container );
		subGui = switchingGuis;
		switchingGuis = false;
	}

	protected int getQty(GuiButton btn)
	{
		try
		{
			DecimalFormat df = new DecimalFormat( "+#;-#" );
			return df.parse( btn.displayString ).intValue();
		}
		catch (ParseException e)
		{
			return 0;
		}
	}

	public boolean isSubGui()
	{
		return subGui;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		Iterator<Slot> i = inventorySlots.inventorySlots.iterator();
		while (i.hasNext())
			if ( i.next() instanceof SlotME )
				i.remove();

		for (InternalSlotME me : meSlots)
			inventorySlots.inventorySlots.add( new SlotME( me ) );
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();

		int i = Mouse.getEventDWheel();
		if ( i != 0 && isShiftKeyDown() )
		{
			int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			mouseWheelEvent( x, y, i / Math.abs( i ) );
		}
		else if ( i != 0 && myScrollBar != null )
			myScrollBar.wheel( i );
	}

	protected void mouseWheelEvent(int x, int y, int wheel)
	{
		Slot slot = getSlot( x, y );
		if ( slot instanceof SlotME )
		{
			IAEItemStack item = ((SlotME) slot).getAEStack();
			if ( item != null )
			{
				try
				{
					((AEBaseContainer) inventorySlots).setTargetStack( item );
					InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
					int times = Math.abs( wheel );
					for (int h = 0; h < times; h++)
					{
						PacketInventoryAction p = new PacketInventoryAction( direction, inventorySlots.inventorySlots.size(), 0 );
						NetworkHandler.instance.sendToServer( p );
					}
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}
		}
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		subGui = true; // in case the gui is reopened later ( i'm looking at you NEI )
	}

	@Override
	protected void mouseClicked(int xCoord, int yCoord, int btn)
	{
		drag_click.clear();

		if ( btn == 1 )
		{
			for (Object o : this.buttonList)
			{
				GuiButton guibutton = (GuiButton) o;
				if ( guibutton.mousePressed( this.mc, xCoord, yCoord ) )
				{
					super.mouseClicked( xCoord, yCoord, 0 );
					return;
				}
			}
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	boolean disableShiftClick = false;
	Stopwatch dbl_clickTimer = Stopwatch.createStarted();
	ItemStack dbl_whichItem;
	Slot bl_clicked;

	// drag y
	final Set<Slot> drag_click = new HashSet<Slot>();

	@Override
	protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int key)
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		if ( slot instanceof SlotFake )
		{
			InventoryAction action = null;
			action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;

			if ( drag_click.size() > 1 )
				return;

			if ( action != null )
			{
				try
				{
					PacketInventoryAction p = new PacketInventoryAction( action, slotIdx, 0 );
					NetworkHandler.instance.sendToServer( p );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}

			return;
		}

		if ( slot instanceof SlotPatternTerm )
		{
			if ( key == 6 )
				return; // prevent weird double clicks..

			try
			{
				NetworkHandler.instance.sendToServer( ((SlotPatternTerm) slot).getRequest( key == 1 ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
		else if ( slot instanceof SlotCraftingTerm )
		{
			if ( key == 6 )
				return; // prevent weird double clicks..

			InventoryAction action = null;
			if ( key == 1 )
				action = InventoryAction.CRAFT_SHIFT;
			else
				action = ctrlDown == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;

			if ( action != null )
			{
				try
				{
					PacketInventoryAction p = new PacketInventoryAction( action, slotIdx, 0 );
					NetworkHandler.instance.sendToServer( p );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}

			return;
		}

		if ( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) )
		{
			if ( enableSpaceClicking() )
			{
				IAEItemStack stack = null;
				if ( slot instanceof SlotME )
					stack = ((SlotME) slot).getAEStack();

				try
				{
					int slotNum = inventorySlots.inventorySlots.size();

					if ( !(slot instanceof SlotME) && slot != null )
						slotNum = slot.slotNumber;

					((AEBaseContainer) inventorySlots).setTargetStack( stack );
					PacketInventoryAction p = new PacketInventoryAction( InventoryAction.MOVE_REGION, slotNum, 0 );
					NetworkHandler.instance.sendToServer( p );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
				return;
			}
		}

		if ( slot instanceof SlotDisconnected )
		{
			InventoryAction action = null;

			switch (key)
			{
			case 0: // pickup / set-down.
				action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				break;
			case 1:
				action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				break;

			case 3: // creative dupe:

				if ( player.capabilities.isCreativeMode )
				{
					action = InventoryAction.CREATIVE_DUPLICATE;
				}

				break;

			default:
			case 4: // drop item:
			case 6:
			}

			if ( action != null )
			{
				try
				{
					PacketInventoryAction p = new PacketInventoryAction( action, slot.getSlotIndex(), ((SlotDisconnected) slot).mySlot.id );
					NetworkHandler.instance.sendToServer( p );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}

			return;
		}

		if ( slot instanceof SlotME )
		{
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch (key)
			{
			case 0: // pickup / set-down.
				action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				stack = ((SlotME) slot).getAEStack();

				if ( stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack() == null )
					action = InventoryAction.AUTO_CRAFT;

				break;
			case 1:
				action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				stack = ((SlotME) slot).getAEStack();
				break;

			case 3: // creative dupe:

				stack = ((SlotME) slot).getAEStack();
				if ( stack != null && stack.isCraftable() )
					action = InventoryAction.AUTO_CRAFT;

				else if ( player.capabilities.isCreativeMode )
				{
					IAEItemStack slotItem = ((SlotME) slot).getAEStack();
					if ( slotItem != null )
					{
						action = InventoryAction.CREATIVE_DUPLICATE;
					}
				}
				break;

			default:
			case 4: // drop item:
			case 6:
			}

			if ( action != null )
			{
				try
				{
					((AEBaseContainer) inventorySlots).setTargetStack( stack );
					PacketInventoryAction p = new PacketInventoryAction( action, inventorySlots.inventorySlots.size(), 0 );
					NetworkHandler.instance.sendToServer( p );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}

			return;
		}

		if ( disableShiftClick == false && isShiftKeyDown() )
		{
			disableShiftClick = true;

			if ( dbl_whichItem == null || bl_clicked != slot || dbl_clickTimer.elapsed( TimeUnit.MILLISECONDS ) > 150 )
			{
				// some simple double click logic.
				bl_clicked = slot;
				dbl_clickTimer = Stopwatch.createStarted();
				if ( slot != null )
					dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : null;
				else
					dbl_whichItem = null;
			}
			else if ( dbl_whichItem != null )
			{
				// a replica of the weird broken vanilla feature.

				for (Object inventorySlot : this.inventorySlots.inventorySlots)
				{
					Slot targetSlot = (Slot) inventorySlot;

					if ( targetSlot != null && targetSlot.canTakeStack( this.mc.thePlayer ) && targetSlot.getHasStack()
							&& targetSlot.inventory == slot.inventory && Container.func_94527_a( targetSlot, dbl_whichItem, true ) )
					{
						this.handleMouseClick( targetSlot, targetSlot.slotNumber, ctrlDown, 1 );
					}
				}
			}

			disableShiftClick = false;
		}

		super.handleMouseClick( slot, slotIdx, ctrlDown, key );
	}

	@Override
	protected void mouseClickMove(int x, int y, int c, long d)
	{
		Slot slot = this.getSlot( x, y );
		ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

		if ( slot instanceof SlotFake && itemstack != null )
		{
			drag_click.add( slot );
			if ( drag_click.size() > 1 )
			{
				try
				{
					for (Slot dr : drag_click)
					{
						PacketInventoryAction p = new PacketInventoryAction( c == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE,
								dr.slotNumber, 0 );
						NetworkHandler.instance.sendToServer( p );
					}
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}
		}
		else
			super.mouseClickMove( x, y, c, d );
	}

	protected boolean enableSpaceClicking()
	{
		return true;
	}

	@Override
	protected boolean checkHotbarKeys(int p_146983_1_)
	{
		Slot theSlot;

		try
		{
			theSlot = ObfuscationReflectionHelper.getPrivateValue( GuiContainer.class, this, "theSlot", "field_147006_u", "f" );
		}
		catch (Throwable t)
		{
			return false;
		}

		if ( this.mc.thePlayer.inventory.getItemStack() == null && theSlot != null )
		{
			for (int j = 0; j < 9; ++j)
			{
				if ( p_146983_1_ == this.mc.gameSettings.keyBindsHotbar[j].getKeyCode() )
				{
					for (Slot s : (List<Slot>) inventorySlots.inventorySlots)
					{
						if ( s.getSlotIndex() == j && s.inventory == ((AEBaseContainer) inventorySlots).getPlayerInv() )
						{
							if ( !s.canTakeStack( ((AEBaseContainer) inventorySlots).getPlayerInv().player ) )
							{
								return false;
							}
						}
					}

					if ( theSlot.getSlotStackLimit() == 64 )
					{
						this.handleMouseClick( theSlot, theSlot.slotNumber, j, 2 );
						return true;
					}
					else
					{
						try
						{
							for (Slot s : (List<Slot>) inventorySlots.inventorySlots)
							{
								if ( s.getSlotIndex() == j && s.inventory == ((AEBaseContainer) inventorySlots).getPlayerInv() )
								{
									NetworkHandler.instance.sendToServer( new PacketSwapSlots( s.slotNumber, theSlot.slotNumber ) );
									return true;
								}
							}
						}
						catch (IOException e)
						{
							AELog.error( e );
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		super.drawScreen( mouse_x, mouse_y, btn );

		boolean hasClicked = Mouse.isButtonDown( 0 );
		if ( hasClicked && myScrollBar != null )
			myScrollBar.click( this, mouse_x - guiLeft, mouse_y - guiTop );

		for (Object c : buttonList)
		{
			if ( c instanceof ITooltip )
			{
				ITooltip tooltip = (ITooltip) c;
				int x = tooltip.xPos(); // ((GuiImgButton) c).xPosition;
				int y = tooltip.yPos(); // ((GuiImgButton) c).yPosition;

				if ( x < mouse_x && x + tooltip.getWidth() > mouse_x && tooltip.isVisible() )
				{
					if ( y < mouse_y && y + tooltip.getHeight() > mouse_y )
					{
						if ( y < 15 )
							y = 15;

						String msg = tooltip.getMsg();
						if ( msg != null )
							drawTooltip( x + 11, y + 4, 0, msg );
					}
				}
			}
		}
	}

	public void drawTooltip(int par2, int par3, int forceWidth, String Msg)
	{
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		String[] var4 = Msg.split( "\n" );

		if ( var4.length > 0 )
		{
			int var5 = 0;
			int var6;
			int var7;

			for (var6 = 0; var6 < var4.length; ++var6)
			{
				var7 = fontRendererObj.getStringWidth( var4[var6] );

				if ( var7 > var5 )
				{
					var5 = var7;
				}
			}

			var6 = par2 + 12;
			var7 = par3 - 12;
			int var9 = 8;

			if ( var4.length > 1 )
			{
				var9 += 2 + (var4.length - 1) * 10;
			}

			if ( this.guiTop + var7 + var9 + 6 > this.height )
			{
				var7 = this.height - var9 - this.guiTop - 6;
			}

			if ( forceWidth > 0 )
				var5 = forceWidth;

			this.zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			int var10 = -267386864;
			this.drawGradientRect( var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10 );
			this.drawGradientRect( var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10 );
			this.drawGradientRect( var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10 );
			this.drawGradientRect( var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10 );
			this.drawGradientRect( var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10 );
			int var11 = 1347420415;
			int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
			this.drawGradientRect( var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12 );
			this.drawGradientRect( var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12 );
			this.drawGradientRect( var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11 );
			this.drawGradientRect( var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12 );

			for (int var13 = 0; var13 < var4.length; ++var13)
			{
				String var14 = var4[var13];

				if ( var13 == 0 )
				{
					var14 = "\u00a7" + Integer.toHexString( 15 ) + var14;
				}
				else
				{
					var14 = "\u00a77" + var14;
				}

				this.fontRendererObj.drawStringWithShadow( var14, var6, var7, -1 );

				if ( var13 == 0 )
				{
					var7 += 2;
				}

				var7 += 10;
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
		}
		GL11.glPopAttrib();
	}

	public abstract void drawBG(int offsetX, int offsetY, int mouseX, int mouseY);

	public abstract void drawFG(int offsetX, int offsetY, int mouseX, int mouseY);

	public void bindTexture(String base, String file)
	{
		ResourceLocation loc = new ResourceLocation( base, "textures/" + file );
		this.mc.getTextureManager().bindTexture( loc );
	}

	public void bindTexture(String file)
	{
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/" + file );
		this.mc.getTextureManager().bindTexture( loc );
	}

	protected void drawItem(int x, int y, ItemStack is)
	{
		this.zLevel = 100.0F;
		itemRender.zLevel = 100.0F;

		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI( this.fontRendererObj, this.mc.renderEngine, is, x, y );
		GL11.glPopAttrib();

		itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	@Override
	final protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		int ox = guiLeft; // (width - xSize) / 2;
		int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		drawBG( ox, oy, x, y );

		for (Object o : inventorySlots.inventorySlots)
		{
			if ( o instanceof OptionalSlotFake )
			{
				OptionalSlotFake fs = (OptionalSlotFake) o;
				if ( fs.renderDisabled() )
				{
					if ( fs.isEnabled() )
					{
						this.drawTexturedModalRect( ox + fs.xDisplayPosition - 1, oy + fs.yDisplayPosition - 1, fs.srcX - 1, fs.srcY - 1, 18, 18 );
					}
					else
					{
						GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
						GL11.glColor4f( 1.0F, 1.0F, 1.0F, 0.4F );
						GL11.glEnable( GL11.GL_BLEND );
						this.drawTexturedModalRect( ox + fs.xDisplayPosition - 1, oy + fs.yDisplayPosition - 1, fs.srcX - 1, fs.srcY - 1, 18, 18 );
						GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
						GL11.glPopAttrib();
					}
				}
			}
		}
	}

	@Override
	final protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		int ox = guiLeft; // (width - xSize) / 2;
		int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		if ( myScrollBar != null )
			myScrollBar.draw( this );

		drawFG( ox, oy, x, y );
	}

	protected String getGuiDisplayName(String in)
	{
		return hasCustomInventoryName() ? getInventoryName() : in;
	}

	private String getInventoryName()
	{
		return ((AEBaseContainer) inventorySlots).customName;
	}

	private boolean hasCustomInventoryName()
	{
		if ( inventorySlots instanceof AEBaseContainer )
			return ((AEBaseContainer) inventorySlots).customName != null;
		return false;
	}

	protected Slot getSlot(int mouseX, int mouseY)
	{
		for (int j1 = 0; j1 < this.inventorySlots.inventorySlots.size(); ++j1)
		{
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( j1 );

			// isPointInRegion
			if ( func_146978_c( slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY ) )
			{
				return slot;
			}
		}

		return null;
	}

	protected static String join(Collection<?> s, String delimiter)
	{
		StringBuilder builder = new StringBuilder();
		Iterator iterator = s.iterator();
		while (iterator.hasNext())
		{
			builder.append( iterator.next() );
			if ( !iterator.hasNext() )
			{
				break;
			}
			builder.append( delimiter );
		}
		return builder.toString();
	}

	boolean useNEI = false;

	private RenderItem setItemRender(RenderItem item)
	{
		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.NEI ) )
		{
			return ((INEI) AppEng.instance.getIntegration( IntegrationType.NEI )).setItemRender( item );
		}
		else
		{
			RenderItem ri = itemRender;
			itemRender = item;
			return ri;
		}
	}

	private void safeDrawSlot(Slot s)
	{
		try
		{
			// drawSlotInventory
			// super.func_146977_a( s );r
			GuiContainer.class.getDeclaredMethod( "func_146977_a_original", Slot.class ).invoke( this, s );
		}
		catch (Exception err)
		{
			Tessellator tessellator = Tessellator.instance;
			if ( Platform.isDrawing( tessellator ) )
				tessellator.draw();
		}
	}

	final AppEngRenderItem aeRenderItem = new AppEngRenderItem();

	protected boolean isPowered()
	{
		return true;
	}

	public void a(Slot s)
	{
		drawSlot( s );
	}

	public void func_146977_a(Slot s)
	{
		drawSlot( s );
	}

	public void drawSlot(Slot s)
	{
		if ( s instanceof SlotME )
		{
			RenderItem pIR = setItemRender( aeRenderItem );
			try
			{
				this.zLevel = 100.0F;
				itemRender.zLevel = 100.0F;

				if ( !isPowered() )
				{
					GL11.glDisable( GL11.GL_LIGHTING );
					drawRect( s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66111111 );
					GL11.glEnable( GL11.GL_LIGHTING );
				}

				this.zLevel = 0.0F;
				itemRender.zLevel = 0.0F;

				if ( s instanceof SlotME )
					aeRenderItem.aeStack = ((SlotME) s).getAEStack();
				else
					aeRenderItem.aeStack = null;

				safeDrawSlot( s );
			}
			catch (Exception err)
			{
				AELog.warning( "[AppEng] AE prevented crash while drawing slot: " + err.toString() );
				if ( Platform.isDrawing( Tessellator.instance ) )
					Tessellator.instance.draw();
			}
			setItemRender( pIR );
			return;
		}
		else
		{
			try
			{
				ItemStack is = s.getStack();
				if ( s instanceof AppEngSlot && (((AppEngSlot) s).renderIconWithItem() || is == null) && (((AppEngSlot) s).shouldDisplay()) )
				{
					AppEngSlot aes = (AppEngSlot) s;
					if ( aes.getIcon() >= 0 )
					{
						bindTexture( "guis/states.png" );

						GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
						Tessellator tessellator = Tessellator.instance;
						try
						{
							int uv_y = (int) Math.floor( aes.getIcon() / 16 );
							int uv_x = aes.getIcon() - uv_y * 16;

							GL11.glEnable( GL11.GL_BLEND );
							GL11.glDisable( GL11.GL_LIGHTING );
							GL11.glEnable( GL11.GL_TEXTURE_2D );
							GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
							GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
							float par1 = aes.xDisplayPosition;
							float par2 = aes.yDisplayPosition;
							float par3 = uv_x * 16;
							float par4 = uv_y * 16;
							float par5 = 16;
							float par6 = 16;

							float f = 0.00390625F;
							float f1 = 0.00390625F;
							tessellator.startDrawingQuads();
							tessellator.setColorRGBA_F( 1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon() );
							tessellator.addVertexWithUV( par1 + 0, par2 + par6, this.zLevel, (par3 + 0) * f,
									(par4 + par6) * f1 );
							tessellator.addVertexWithUV( par1 + par5, par2 + par6, this.zLevel,
									(par3 + par5) * f, (par4 + par6) * f1 );
							tessellator.addVertexWithUV( par1 + par5, par2 + 0, this.zLevel,
									(par3 + par5) * f, (par4 + 0) * f1 );
							tessellator.addVertexWithUV( par1 + 0, par2 + 0, this.zLevel, (par3 + 0) * f,
									(par4 + 0) * f1 );
							tessellator.setColorRGBA_F( 1.0f, 1.0f, 1.0f, 1.0f );
							tessellator.draw();
						}
						catch (Exception err)
						{
							if ( Platform.isDrawing( tessellator ) )
								tessellator.draw();
						}
						GL11.glPopAttrib();
					}
				}

				if ( is != null && s instanceof AppEngSlot )
				{
					if ( ((AppEngSlot) s).isValid == hasCalculatedValidness.NotAvailable )
					{
						boolean isValid = s.isItemValid( is ) || s instanceof SlotOutput || s instanceof AppEngCraftingSlot || s instanceof SlotDisabled
								|| s instanceof SlotInaccessible || s instanceof SlotFake || s instanceof SlotRestrictedInput || s instanceof SlotDisconnected;
						if ( isValid && s instanceof SlotRestrictedInput )
						{
							try
							{
								isValid = ((SlotRestrictedInput) s).isValid( is, this.mc.theWorld );
							}
							catch (Exception err)
							{
								AELog.error( err );
							}
						}
						((AppEngSlot) s).isValid = isValid ? hasCalculatedValidness.Valid : hasCalculatedValidness.Invalid;
					}

					if ( ((AppEngSlot) s).isValid == hasCalculatedValidness.Invalid )
					{
						this.zLevel = 100.0F;
						itemRender.zLevel = 100.0F;

						GL11.glDisable( GL11.GL_LIGHTING );
						drawRect( s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66ff6666 );
						GL11.glEnable( GL11.GL_LIGHTING );

						this.zLevel = 0.0F;
						itemRender.zLevel = 0.0F;
					}
				}

				if ( s instanceof AppEngSlot )
				{
					((AppEngSlot) s).isDisplay = true;
					safeDrawSlot( s );
				}
				else
					safeDrawSlot( s );

				return;
			}
			catch (Exception err)
			{
				AELog.warning( "[AppEng] AE prevented crash while drawing slot: " + err.toString() );
			}
		}
		// do the usual for non-ME Slots.
		safeDrawSlot( s );
	}

}
