package appeng.client.gui;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.client.render.AppEngRenderItem;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.AppEngSlot.hasCalculatedValidness;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotInaccessable;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AELog;
import appeng.core.Configuration;

public abstract class AEBaseMEGui extends AEBaseGui
{

	public AEBaseMEGui(Container container) {
		super( container );
	}

	// @Override - NEI
	public List<String> handleItemTooltip(ItemStack stack, int mousex, int mousey, List<String> currenttip)
	{
		if ( stack != null )
		{
			Slot s = getSlot( mousex, mousey );
			if ( s instanceof SlotME )
			{
				int BigNumber = Configuration.instance.useTerminalUseLargeFont() ? 999 : 9999;

				IAEItemStack myStack = null;

				try
				{
					SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch (Throwable _)
				{
				}

				if ( myStack != null )
				{
					if ( myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged()) )
						currenttip.add( "\u00a77Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );

					if ( myStack.getCountRequestable() > 0 )
						currenttip.add( "\u00a77Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );
				}
				else if ( stack.stackSize > BigNumber || (stack.stackSize > 1 && stack.isItemDamaged()) )
				{
					currenttip.add( "\u00a77Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( stack.stackSize ) );
				}
			}
			else
				currenttip.add( StatCollector.translateToLocal( "AppEng.Gui.Whitelisted" ) );
		}
		return currenttip;
	}

	// Vanillia version...
	@Override
	protected void drawItemStackTooltip(ItemStack stack, int x, int y)
	{
		Slot s = getSlot( x, y );
		if ( s instanceof SlotME && stack != null )
		{
			int BigNumber = Configuration.instance.useTerminalUseLargeFont() ? 999 : 9999;

			IAEItemStack myStack = null;

			try
			{
				SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch (Throwable _)
			{
			}

			if ( myStack != null )
			{
				List currenttip = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );

				if ( myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged()) )
					currenttip.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );

				if ( myStack.getCountRequestable() > 0 )
					currenttip.add( "Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );

				drawTooltip( x, y, 0, join( currenttip, "\n" ) );
			}
			else if ( stack != null && stack.stackSize > BigNumber )
			{
				List var4 = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );
				var4.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( stack.stackSize ) );
				drawTooltip( x, y, 0, join( var4, "\n" ) );
				return;
			}
		}
		super.drawItemStackTooltip( stack, x, y );
	}

	static String join(Collection<?> s, String delimiter)
	{
		StringBuilder builder = new StringBuilder();
		Iterator iter = s.iterator();
		while (iter.hasNext())
		{
			builder.append( iter.next() );
			if ( !iter.hasNext() )
			{
				break;
			}
			builder.append( delimiter );
		}
		return builder.toString();
	}

	private Slot getSlot(int mousex, int mousey)
	{
		for (int j1 = 0; j1 < this.inventorySlots.inventorySlots.size(); ++j1)
		{
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( j1 );
			if ( isPointInRegion( slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mousex, mousey ) )
			{
				return slot;
			}
		}

		return null;
	}

	private void safeDrawSlot(Slot s)
	{
		try
		{
			super.drawSlotInventory( s );
		}
		catch (Exception err)
		{
			Tessellator tessellator = Tessellator.instance;
			if ( tessellator.isDrawing )
				tessellator.draw();
		}
	}

	AppEngRenderItem aeri = new AppEngRenderItem();

	@Override
	protected void drawSlotInventory(Slot s)
	{
		if ( s instanceof SlotME )
		{
			RenderItem pIR = itemRenderer;
			itemRenderer = aeri;
			try
			{
				this.zLevel = 100.0F;
				itemRenderer.zLevel = 100.0F;

				if ( ! isPowered() )
				{
					GL11.glDisable( GL11.GL_LIGHTING );
					super.drawRect( s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66111111 );
					GL11.glEnable( GL11.GL_LIGHTING );
				}

				this.zLevel = 0.0F;
				itemRenderer.zLevel = 0.0F;

				if ( s instanceof SlotME )
					aeri.aestack = ((SlotME) s).getAEStack();
				else
					aeri.aestack = null;

				safeDrawSlot( s );
			}
			catch (Exception err)
			{
				AELog.warning( "[AppEng] AE prevented crash while drawing slot: " + err.toString() );
				if ( Tessellator.instance.isDrawing )
					Tessellator.instance.draw();
			}
			itemRenderer = pIR;
			return;
		}
		else
		{
			try
			{
				ItemStack is = s.getStack();
				if ( s instanceof AppEngSlot && (((AppEngSlot) s).renderIconWithItem() || is == null) )
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
							tessellator.addVertexWithUV( (double) (par1 + 0), (double) (par2 + par6), (double) this.zLevel, (double) ((float) (par3 + 0) * f),
									(double) ((float) (par4 + par6) * f1) );
							tessellator.addVertexWithUV( (double) (par1 + par5), (double) (par2 + par6), (double) this.zLevel,
									(double) ((float) (par3 + par5) * f), (double) ((float) (par4 + par6) * f1) );
							tessellator.addVertexWithUV( (double) (par1 + par5), (double) (par2 + 0), (double) this.zLevel,
									(double) ((float) (par3 + par5) * f), (double) ((float) (par4 + 0) * f1) );
							tessellator.addVertexWithUV( (double) (par1 + 0), (double) (par2 + 0), (double) this.zLevel, (double) ((float) (par3 + 0) * f),
									(double) ((float) (par4 + 0) * f1) );
							tessellator.setColorRGBA_F( 1.0f, 1.0f, 1.0f, 1.0f );
							tessellator.draw();
						}
						catch (Exception err)
						{
							if ( tessellator.isDrawing )
								tessellator.draw();
						}
						GL11.glPopAttrib();
					}
				}

				if ( is != null && s instanceof AppEngSlot )
				{
					if ( ((AppEngSlot) s).isValid == hasCalculatedValidness.NotAvailable )
					{
						boolean isValid = s.isItemValid( is ) || s instanceof SlotOutput || s instanceof SlotDisabled || s instanceof SlotInaccessable;
						if ( isValid && s instanceof SlotRestrictedInput )
						{
							try
							{
								isValid = ((SlotRestrictedInput) s).isValid( is, this.mc.theWorld );
							}
							catch (Exception err)
							{
								err.printStackTrace();
							}
						}
						((AppEngSlot) s).isValid = isValid ? hasCalculatedValidness.Valid : hasCalculatedValidness.Invalid;
					}

					if ( ((AppEngSlot) s).isValid == hasCalculatedValidness.Invalid )
					{
						this.zLevel = 100.0F;
						itemRenderer.zLevel = 100.0F;

						GL11.glDisable( GL11.GL_LIGHTING );
						super.drawRect( s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66ff6666 );
						GL11.glEnable( GL11.GL_LIGHTING );

						this.zLevel = 0.0F;
						itemRenderer.zLevel = 0.0F;
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

	private boolean isPowered() {
		return true;
	}

}