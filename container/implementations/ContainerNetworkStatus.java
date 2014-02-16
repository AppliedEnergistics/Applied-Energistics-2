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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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

	int lo_avgAddition, hi_avgAddition;
	int lo_powerUsage, hi_powerUsage;

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int idx, int value)
	{
		switch (idx)
		{
		case 0:
			lo_avgAddition = value;
			break;
		case 1:
			hi_avgAddition = value;
			break;
		case 2:
			lo_powerUsage = value;
			break;
		case 3:
			hi_powerUsage = value;
			break;
		}

		avgAddition = ((long) hi_avgAddition) << 32 | lo_avgAddition;
		powerUsage = ((long) hi_powerUsage) << 32 | lo_powerUsage;
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

				lo_avgAddition = (int) (avgAddition & 0xffffffffL);
				hi_avgAddition = (int) (avgAddition >> 32L);

				lo_powerUsage = (int) (powerUsage & 0xffffffffL);
				hi_powerUsage = (int) (powerUsage >> 32L);

				for (Object c : this.crafters)
				{
					ICrafting icrafting = (ICrafting) c;
					try
					{
						NetworkHandler.instance.sendTo( new PacketProgressBar( 0, (int) lo_avgAddition ), (EntityPlayerMP) icrafting );
						NetworkHandler.instance.sendTo( new PacketProgressBar( 1, (int) hi_avgAddition ), (EntityPlayerMP) icrafting );
						NetworkHandler.instance.sendTo( new PacketProgressBar( 2, (int) lo_powerUsage ), (EntityPlayerMP) icrafting );
						NetworkHandler.instance.sendTo( new PacketProgressBar( 3, (int) hi_powerUsage ), (EntityPlayerMP) icrafting );
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
