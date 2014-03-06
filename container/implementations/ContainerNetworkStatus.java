package appeng.container.implementations;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketProgressBar;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ContainerNetworkStatus extends AEBaseContainer
{

	IGrid network;

	public ContainerNetworkStatus(InventoryPlayer ip, INetworkTool te) {
		super( ip, null, null );
		IGridHost host = te.getGridHost();

		if ( host != null )
		{
			findNode( host, ForgeDirection.UNKNOWN );
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
				findNode( host, d );
		}

		if ( network == null && Platform.isServer() )
			isContainerValid = false;
	}

	private void findNode(IGridHost host, ForgeDirection d)
	{
		if ( network == null )
		{
			IGridNode node = host.getGridNode( d );
			if ( node != null )
				network = node.getGrid();
		}
	}

	int delay = 40;

	public long avgAddition;
	public long powerUsage;
	public long currentPower;
	public long maxPower;

	@Override
	public void updateFullProgressBar(int id, long value)
	{
		if ( id == 0 )
			avgAddition = value;

		if ( id == 1 )
			powerUsage = value;

		if ( id == 2 )
			currentPower = value;

		if ( id == 3 )
			maxPower = value;
	}

	@Override
	public void detectAndSendChanges()
	{
		delay++;
		if ( Platform.isServer() && delay > 15 && network != null )
		{
			delay = 0;

			IEnergyGrid eg = network.getCache( IEnergyGrid.class );
			if ( eg != null )
			{
				avgAddition = (long) (100.0 * eg.getAvgPowerInjection());
				powerUsage = (long) (100.0 * eg.getAvgPowerUsage());
				currentPower = (long) (100.0 * eg.getStoredPower());
				maxPower = (long) (100.0 * eg.getMaxStoredPower());

				for (Object c : this.crafters)
				{
					ICrafting icrafting = (ICrafting) c;
					try
					{
						NetworkHandler.instance.sendTo( new PacketProgressBar( 0, avgAddition ), (EntityPlayerMP) icrafting );
						NetworkHandler.instance.sendTo( new PacketProgressBar( 1, powerUsage ), (EntityPlayerMP) icrafting );
						NetworkHandler.instance.sendTo( new PacketProgressBar( 2, currentPower ), (EntityPlayerMP) icrafting );
						NetworkHandler.instance.sendTo( new PacketProgressBar( 3, maxPower ), (EntityPlayerMP) icrafting );
					}
					catch (IOException e)
					{
						AELog.error( e );
					}
				}
			}

			PacketMEInventoryUpdate piu;
			try
			{
				piu = new PacketMEInventoryUpdate();

				for (Class<? extends IGridHost> machineClass : network.getMachinesClasses())
				{
					IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
					for (IGridNode machine : network.getMachines( machineClass ))
					{
						IGridBlock blk = machine.getGridBlock();
						ItemStack is = blk.getMachineRepresentation();
						if ( is != null && is.getItem() != null )
						{
							IAEItemStack ais = AEItemStack.create( is );
							ais.setStackSize( 1 );
							ais.setCountRequestable( (long) (blk.getIdlePowerUsage() * 100.0) );
							list.add( ais );
						}
					}

					for (IAEItemStack ais : list)
						piu.appendItem( ais );
				}

				for (Object c : this.crafters)
				{
					if ( c instanceof EntityPlayer )
						NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );
				}
			}
			catch (IOException e)
			{
				// :P
			}

		}
		super.detectAndSendChanges();
	}
}
