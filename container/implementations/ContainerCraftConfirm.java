package appeng.container.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;
import appeng.util.ItemSorters;
import appeng.util.Platform;

import com.google.common.collect.ImmutableSet;

public class ContainerCraftConfirm extends AEBaseContainer
{

	ITerminalHost priHost;
	public Future<ICraftingJob> job;
	public ICraftingJob result;

	@GuiSync(0)
	public long bytesUsed;

	@GuiSync(1)
	public long cpuBytesAvail;

	@GuiSync(2)
	public int cpuCoProcessors;

	@GuiSync(3)
	public boolean autoStart = false;

	@GuiSync(4)
	public boolean simulation = true;

	@GuiSync(5)
	public int selectedCpu = -1;

	@GuiSync(6)
	public boolean noCPU = true;

	@GuiSync(7)
	public String myName = "";

	protected long cpuIdx = Long.MIN_VALUE;

	public class CraftingCPURecord implements Comparable<CraftingCPURecord>
	{

		ICraftingCPU cpu;

		long id = cpuIdx++;
		long size;
		int processors;

		public String myName;

		CraftingCPURecord(long size, int proc, ICraftingCPU server) {
			this.size = size;
			this.processors = proc;
			this.cpu = server;
			myName = server.getName();
		}

		@Override
		public int compareTo(CraftingCPURecord o)
		{
			int a = ItemSorters.compareLong( o.processors, processors );
			if ( a != 0 )
				return a;
			return ItemSorters.compareLong( o.size, size );
		}

	};

	public ArrayList<CraftingCPURecord> cpus = new ArrayList();

	public ContainerCraftConfirm(InventoryPlayer ip, ITerminalHost te) {
		super( ip, te );
		priHost = te;
	}

	private void sendCPUs()
	{
		Collections.sort( cpus );

		if ( selectedCpu >= cpus.size() )
		{
			selectedCpu = -1;
			cpuBytesAvail = 0;
			cpuCoProcessors = 0;
			myName = "";
		}
		else if ( selectedCpu != -1 )
		{
			myName = cpus.get( selectedCpu ).myName;
			cpuBytesAvail = cpus.get( selectedCpu ).size;
			cpuCoProcessors = cpus.get( selectedCpu ).processors;
		}
	}

	public void cycleCpu(boolean next)
	{
		if ( next )
			selectedCpu++;
		else
			selectedCpu--;

		if ( selectedCpu < -1 )
			selectedCpu = cpus.size() - 1;
		else if ( selectedCpu >= cpus.size() )
			selectedCpu = -1;

		if ( selectedCpu == -1 )
		{
			cpuBytesAvail = 0;
			cpuCoProcessors = 0;
			myName = "";
		}
		else
		{
			myName = cpus.get( selectedCpu ).myName;
			cpuBytesAvail = cpus.get( selectedCpu ).size;
			cpuCoProcessors = cpus.get( selectedCpu ).processors;
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isClient() )
			return;

		ICraftingGrid cc = getGrid().getCache( ICraftingGrid.class );
		ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

		int matches = 0;
		boolean changed = false;
		for (ICraftingCPU c : cpuSet)
		{
			boolean found = false;
			for (CraftingCPURecord ccr : cpus)
				if ( ccr.cpu == c )
					found = true;

			boolean matched = cpuMatches( c );

			if ( matched )
				matches++;

			if ( !found != matched )
				changed = true;
		}

		if ( changed || cpus.size() != matches )
		{
			cpus.clear();
			for (ICraftingCPU c : cpuSet)
			{
				if ( cpuMatches( c ) )
					cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
			}

			sendCPUs();
		}

		noCPU = cpus.size() == 0;

		super.detectAndSendChanges();

		if ( job != null && job.isDone() )
		{
			try
			{
				result = job.get();

				if ( !result.isSimulation() )
				{
					simulation = false;
					if ( autoStart )
					{
						startJob();
						return;
					}
				}
				else
					simulation = true;

				try
				{
					PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
					PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );
					PacketMEInventoryUpdate c = result.isSimulation() ? new PacketMEInventoryUpdate( (byte) 2 ) : null;

					IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
					result.populatePlan( plan );

					bytesUsed = result.getByteTotal();

					for (IAEItemStack out : plan)
					{
						IAEItemStack m = null;

						IAEItemStack o = out.copy();
						o.reset();
						o.setStackSize( out.getStackSize() );

						IAEItemStack p = out.copy();
						p.reset();
						p.setStackSize( out.getCountRequestable() );

						IStorageGrid sg = getGrid().getCache( IStorageGrid.class );
						IMEInventory<IAEItemStack> itemsg = sg.getItemInventory();

						if ( c != null && result.isSimulation() )
						{
							m = o.copy();
							o = itemsg.extractItems( o, Actionable.SIMULATE, mySrc );

							if ( o == null )
							{
								o = m.copy();
								o.setStackSize( 0 );
							}

							m.setStackSize( m.getStackSize() - o.getStackSize() );
						}

						if ( o.getStackSize() > 0 )
							a.appendItem( o );

						if ( p.getStackSize() > 0 )
							b.appendItem( p );

						if ( c != null && m != null && m.getStackSize() > 0 )
							c.appendItem( m );
					}

					for (Object g : this.crafters)
					{
						if ( g instanceof EntityPlayer )
						{
							NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );
							NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );
							if ( c != null )
								NetworkHandler.instance.sendTo( c, (EntityPlayerMP) g );
						}
					}
				}
				catch (IOException e)
				{
					// :P
				}
			}
			catch (Throwable e)
			{
				getPlayerInv().player.addChatMessage( new ChatComponentText( "Error: " + e.toString() ) );
				AELog.error( e );
				this.isContainerValid = false;
				result = null;
			}

			job = null;
		}
		verifyPermissions( SecurityPermissions.CRAFT, false );
	}

	private boolean cpuMatches(ICraftingCPU c)
	{
		return c.getAvailableStorage() >= bytesUsed && !c.isBusy();
	}

	public void startJob()
	{
		GuiBridge OriginalGui = null;

		IActionHost ah = getActionHost();
		if ( ah instanceof WirelessTerminalGuiObject )
			OriginalGui = GuiBridge.GUI_WIRELESS_TERM;

		if ( ah instanceof PartTerminal )
			OriginalGui = GuiBridge.GUI_ME;

		if ( ah instanceof PartCraftingTerminal )
			OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;

		if ( ah instanceof PartPatternTerminal )
			OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;

		if ( result != null && simulation == false )
		{
			ICraftingGrid cc = getGrid().getCache( ICraftingGrid.class );
			ICraftingLink g = cc.submitJob( result, null, selectedCpu == -1 ? null : cpus.get( selectedCpu ).cpu, getActionSrc() );
			autoStart = false;
			if ( g != null && OriginalGui != null && openContext != null )
			{
				try
				{
					NetworkHandler.instance.sendTo( new PacketSwitchGuis( OriginalGui ), (EntityPlayerMP) invPlayer.player );
				}
				catch (IOException e)
				{
					// :(
				}

				TileEntity te = openContext.getTile();
				Platform.openGUI( invPlayer.player, te, openContext.side, OriginalGui );
			}
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed( par1EntityPlayer );
		if ( job != null )
		{
			job.cancel( true );
			job = null;
		}
	}

	@Override
	public void removeCraftingFromCrafters(ICrafting c)
	{
		super.removeCraftingFromCrafters( c );
		if ( job != null )
		{
			job.cancel( true );
			job = null;
		}
	}

	public IGrid getGrid()
	{
		IActionHost h = ((IActionHost) this.getTarget());
		return h.getActionableNode().getGrid();
	}

	public World getWorld()
	{
		return getPlayerInv().player.worldObj;
	}

	public BaseActionSource getActionSrc()
	{
		return new PlayerSource( getPlayerInv().player, (IActionHost) getTarget() );
	}
}
