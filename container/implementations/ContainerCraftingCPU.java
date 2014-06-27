package appeng.container.implementations;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ContainerCraftingCPU extends AEBaseContainer
{

	IGrid network;

	public ContainerCraftingCPU(InventoryPlayer ip, TileCraftingTile te) {
		super( ip, null, null );
		IGridHost host = te;// .getGridHost();

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

	@Override
	public void detectAndSendChanges()
	{
		delay++;
		if ( Platform.isServer() && delay > 15 && network != null )
		{
			delay = 0;

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
