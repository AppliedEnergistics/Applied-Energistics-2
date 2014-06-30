package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.crafting.CraftingJob;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PacketCraftRequest extends AppEngPacket
{

	final public IAEItemStack slotItem;
	final public boolean heldShift;
	final public static ExecutorService craftingPool;

	static
	{
		ThreadFactory factory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable ar)
			{
				return new Thread( ar, "AE Crafting Calculator" );
			}

		};

		craftingPool = Executors.newCachedThreadPool( factory );
	}

	// automatic.
	public PacketCraftRequest(ByteBuf stream) throws IOException {
		heldShift = stream.readBoolean();
		slotItem = AEItemStack.loadItemStackFromPacket( stream );
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		if ( player.openContainer instanceof ContainerCraftAmount )
		{
			ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
			Object targ = cca.getTarget();
			if ( targ instanceof IGridHost )
			{
				IGridHost gh = (IGridHost) targ;
				IGridNode gn = gh.getGridNode( ForgeDirection.UNKNOWN );
				if ( gn == null )
					return;

				IGrid g = gn.getGrid();
				if ( g == null )
					return;

				try
				{
					CraftingJob cj = new CraftingJob( cca.getWorld(), cca, slotItem, Actionable.SIMULATE );

					ContainerOpenContext context = cca.openContext;
					if ( context != null )
					{
						TileEntity te = context.w.getTileEntity( context.x, context.y, context.z );
						Platform.openGUI( player, te, cca.openContext.side, GuiBridge.GUI_CRAFTING_CONFIRM );

						if ( player.openContainer instanceof ContainerCraftConfirm )
						{
							ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
							ccc.autoStart = heldShift;
							ccc.job = craftingPool.submit( cj, cj );
							cca.detectAndSendChanges();
						}
					}

				}
				catch (Throwable e)
				{
					AELog.error( e );
				}
			}
		}
	}

	public PacketCraftRequest(ItemStack stack, int parseInt, boolean shift) throws IOException {
		this.slotItem = AEApi.instance().storage().createItemStack( stack );
		this.slotItem.setStackSize( parseInt );
		this.heldShift = shift;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeBoolean( shift );
		slotItem.writeToPacket( data );

		configureWrite( data );
	}

}
