package appeng.client.gui.implementations;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;
import appeng.util.Platform;

import com.google.common.base.Joiner;

public class GuiCraftConfirm extends AEBaseGui
{

	final ContainerCraftConfirm ccc;

	final int rows = 5;

	final IItemList<IAEItemStack> storage = AEApi.instance().storage().createItemList();
	final IItemList<IAEItemStack> pending = AEApi.instance().storage().createItemList();
	final IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();

	final List<IAEItemStack> visual = new ArrayList<IAEItemStack>();

	GuiBridge OriginalGui;

	boolean isAutoStart()
	{
		return ((ContainerCraftConfirm) inventorySlots).autoStart;
	}

	boolean isSimulation()
	{
		return ((ContainerCraftConfirm) inventorySlots).simulation;
	}

	public GuiCraftConfirm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftConfirm( inventoryPlayer, te ) );
		xSize = 238;
		ySize = 206;
		myScrollBar = new GuiScrollbar();

		ccc = (ContainerCraftConfirm) this.inventorySlots;

		if ( te instanceof WirelessTerminalGuiObject )
			OriginalGui = GuiBridge.GUI_WIRELESS_TERM;

		if ( te instanceof PartTerminal )
			OriginalGui = GuiBridge.GUI_ME;

		if ( te instanceof PartCraftingTerminal )
			OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;

		if ( te instanceof PartPatternTerminal )
			OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;

	}

	GuiButton cancel;
	GuiButton start;
	GuiButton selectCPU;

	@Override
	public void initGui()
	{
		super.initGui();

		start = new GuiButton( 0, this.guiLeft + 162, this.guiTop + ySize - 25, 50, 20, GuiText.Start.getLocal() );
		start.enabled = false;
		buttonList.add( start );

		selectCPU = new GuiButton( 0, this.guiLeft + (219 - 180) / 2, this.guiTop + ySize - 68, 180, 20, GuiText.CraftingCPU.getLocal() + ": "
				+ GuiText.Automatic );
		selectCPU.enabled = false;
		buttonList.add( selectCPU );

		if ( OriginalGui != null )
			cancel = new GuiButton( 0, this.guiLeft + 6, this.guiTop + ySize - 25, 50, 20, GuiText.Cancel.getLocal() );

		buttonList.add( cancel );
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.CraftingCPU.getLocal() + ": " + GuiText.Automatic.getLocal();
		if ( ccc.selectedCpu >= 0 )// && ccc.selectedCpu < ccc.cpus.size() )
		{
			if ( ccc.myName.length() > 0 )
			{
				String name = ccc.myName.substring( 0, Math.min( 20, ccc.myName.length() ) );
				btnTextText = GuiText.CraftingCPU.getLocal() + ": " + name;
			}
			else
				btnTextText = GuiText.CraftingCPU.getLocal() + ": #" + ccc.selectedCpu;
		}

		if ( ccc.noCPU )
			btnTextText = GuiText.NoCraftingCPUs.getLocal();

		selectCPU.displayString = btnTextText;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == selectCPU )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		if ( btn == cancel )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( OriginalGui ) );
		}

		if ( btn == start )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Start", "Start" ) );
			}
			catch (Throwable e)
			{
				AELog.error( e );
			}
		}

	}

	private long getTotal(IAEItemStack is)
	{
		IAEItemStack a = storage.findPrecise( is );
		IAEItemStack c = pending.findPrecise( is );
		IAEItemStack m = missing.findPrecise( is );

		long total = 0;

		if ( a != null )
			total += a.getStackSize();

		if ( c != null )
			total += c.getStackSize();

		if ( m != null )
			total += m.getStackSize();

		return total;
	}

	public void postUpdate(List<IAEItemStack> list, byte ref)
	{
		switch (ref)
		{
		case 0:
			for (IAEItemStack l : list)
				handleInput( storage, l );
			break;

		case 1:
			for (IAEItemStack l : list)
				handleInput( pending, l );
			break;

		case 2:
			for (IAEItemStack l : list)
				handleInput( missing, l );
			break;
		}

		for (IAEItemStack l : list)
		{
			long amt = getTotal( l );

			if ( amt <= 0 )
				deleteVisualStack( l );
			else
			{
				IAEItemStack is = findVisualStack( l );
				is.setStackSize( amt );
			}
		}

		setScrollBar();
	}

	private void handleInput(IItemList<IAEItemStack> s, IAEItemStack l)
	{
		IAEItemStack a = s.findPrecise( l );

		if ( l.getStackSize() <= 0 )
		{
			if ( a != null )
				a.reset();
		}
		else
		{
			if ( a == null )
			{
				s.add( l.copy() );
				a = s.findPrecise( l );
			}

			if ( a != null )
				a.setStackSize( l.getStackSize() );
		}
	}

	private IAEItemStack findVisualStack(IAEItemStack l)
	{
		for (IAEItemStack o : visual)
		{
			if ( o.equals( l ) )
			{
				return o;
			}
		}

		IAEItemStack stack = l.copy();
		visual.add( stack );
		return stack;
	}

	private void deleteVisualStack(IAEItemStack l)
	{
		Iterator<IAEItemStack> i = visual.iterator();
		while (i.hasNext())
		{
			IAEItemStack o = i.next();
			if ( o.equals( l ) )
			{
				i.remove();
				return;
			}
		}
	}

	private void setScrollBar()
	{
		int size = visual.size();

		myScrollBar.setTop( 19 ).setLeft( 218 ).setHeight( 114 );
		myScrollBar.setRange( 0, (size + 2) / 3 - rows, 1 );
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( key == 28 )
			{
				actionPerformed( start );
			}
			super.keyTyped( character, key );
		}
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		setScrollBar();
		bindTexture( "guis/craftingreport.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	int tooltip = -1;

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		updateCPUButtonText();

		start.enabled = ccc.noCPU || isSimulation() ? false : true;
		selectCPU.enabled = isSimulation() ? false : true;

		int x = 0;
		int y = 0;

		int gx = (width - xSize) / 2;
		int gy = (height - ySize) / 2;
		int offY = 23;

		tooltip = -1;

		for (int z = 0; z <= 4 * 5; z++)
		{
			int minX = gx + 9 + x * 67;
			int minY = gy + 22 + y * offY;

			if ( minX < mouse_x && minX + 67 > mouse_x )
			{
				if ( minY < mouse_y && minY + offY - 2 > mouse_y )
				{
					tooltip = z;
					break;
				}

			}

			x++;

			if ( x > 2 )
			{
				y++;
				x = 0;
			}
		}

		super.drawScreen( mouse_x, mouse_y, btn );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		long BytesUsed = ccc.bytesUsed;
		String byteUsed = NumberFormat.getInstance().format( BytesUsed );
		String Add = BytesUsed > 0 ? (byteUsed + " " + GuiText.BytesUsed.getLocal()) : GuiText.CalculatingWait.getLocal();
		fontRendererObj.drawString( GuiText.CraftingPlan.getLocal() + " - " + Add, 8, 7, 4210752 );

		String dsp = null;

		if ( isSimulation() )
			dsp = GuiText.Simulation.getLocal();
		else
			dsp = ccc.cpuBytesAvail > 0 ? (GuiText.Bytes.getLocal() + ": " + ccc.cpuBytesAvail + " : " + GuiText.CoProcessors.getLocal() + ": " + ccc.cpuCoProcessors)
					: GuiText.Bytes.getLocal() + ": N/A : " + GuiText.CoProcessors.getLocal() + ": N/A";

		int offset = (219 - fontRendererObj.getStringWidth( dsp )) / 2;
		fontRendererObj.drawString( dsp, offset, 165, 4210752 );

		int sectionLength = 67;

		int x = 0;
		int y = 0;
		int xo = 9;
		int yo = 22;
		int viewStart = myScrollBar.getCurrentScroll() * 3;
		int viewEnd = viewStart + 3 * rows;

		String dspToolTip = "";
		List<String> lineList = new LinkedList<String>();
		int toolPosX = 0;
		int toolPosY = 0;

		int offY = 23;

		for (int z = viewStart; z < Math.min( viewEnd, visual.size() ); z++)
		{
			IAEItemStack refStack = visual.get( z );// repo.getReferenceItem( z );
			if ( refStack != null )
			{
				GL11.glPushMatrix();
				GL11.glScaled( 0.5, 0.5, 0.5 );

				IAEItemStack stored = storage.findPrecise( refStack );
				IAEItemStack pendingStack = pending.findPrecise( refStack );
				IAEItemStack missingStack = missing.findPrecise( refStack );

				int lines = 0;

				if ( stored != null && stored.getStackSize() > 0 )
					lines++;
				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
					lines++;
				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
					lines++;

				int negY = ((lines - 1) * 5) / 2;
				int downY = 0;
				boolean red = false;

				if ( stored != null && stored.getStackSize() > 0 )
				{
					String str = Long.toString( stored.getStackSize() );
					if ( stored.getStackSize() >= 10000 )
						str = Long.toString( stored.getStackSize() / 1000 ) + "k";
					if ( stored.getStackSize() >= 10000000 )
						str = Long.toString( stored.getStackSize() / 1000000 ) + "m";

					str = GuiText.FromStorage.getLocal() + ": " + str;
					int w = 4 + fontRendererObj.getStringWidth( str );
					fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( tooltip == z - viewStart )
						lineList.add( GuiText.FromStorage.getLocal() + ": " + Long.toString( stored.getStackSize() ) );

					downY += 5;
				}

				if ( missingStack != null && missingStack.getStackSize() > 0 )
				{
					String str = Long.toString( missingStack.getStackSize() );
					if ( missingStack.getStackSize() >= 10000 )
						str = Long.toString( missingStack.getStackSize() / 1000 ) + "k";
					if ( missingStack.getStackSize() >= 10000000 )
						str = Long.toString( missingStack.getStackSize() / 1000000 ) + "m";

					str = GuiText.Missing.getLocal() + ": " + str;
					int w = 4 + fontRendererObj.getStringWidth( str );
					fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( tooltip == z - viewStart )
						lineList.add( GuiText.Missing.getLocal() + ": " + Long.toString( missingStack.getStackSize() ) );

					red = true;
					downY += 5;
				}

				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
				{
					String str = Long.toString( pendingStack.getStackSize() );
					if ( pendingStack.getStackSize() >= 10000 )
						str = Long.toString( pendingStack.getStackSize() / 1000 ) + "k";
					if ( pendingStack.getStackSize() >= 10000000 )
						str = Long.toString( pendingStack.getStackSize() / 1000000 ) + "m";

					str = GuiText.ToCraft.getLocal() + ": " + str;
					int w = 4 + fontRendererObj.getStringWidth( str );
					fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( tooltip == z - viewStart )
						lineList.add( GuiText.ToCraft.getLocal() + ": " + Long.toString( pendingStack.getStackSize() ) );

				}

				GL11.glPopMatrix();
				int posX = x * (1 + sectionLength) + xo + sectionLength - 19;
				int posY = y * offY + yo;

				ItemStack is = refStack.copy().getItemStack();

				if ( tooltip == z - viewStart )
				{
					dspToolTip = Platform.getItemDisplayName( is );

					if ( lineList.size() > 0 )
						dspToolTip = dspToolTip + "\n" + Joiner.on( "\n" ).join( lineList );

					toolPosX = x * (1 + sectionLength) + xo + sectionLength - 8;
					toolPosY = y * offY + yo;
				}

				drawItem( posX, posY, is );

				if ( red )
				{
					int startX = x * (1 + sectionLength) + xo;
					int startY = posY - 4;
					drawRect( startX, startY, startX + sectionLength, startY + offY, 0x1AFF0000 );
				}

				x++;

				if ( x > 2 )
				{
					y++;
					x = 0;
				}
			}

		}

		if ( tooltip >= 0 && dspToolTip.length() > 0 )
		{
			GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
			drawTooltip( toolPosX, toolPosY + 10, 0, dspToolTip );
			GL11.glPopAttrib();
		}

	}

}
