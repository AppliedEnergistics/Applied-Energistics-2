package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PacketCraftRequest extends AppEngPacket
{

	final public IAEItemStack slotItem;
	final public boolean heldShift;

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

				Future<ICraftingJob> futureJob = null;

				try
				{
					ICraftingGrid cg = g.getCache( ICraftingGrid.class );
					futureJob = cg.beginCraftingJob( cca.getWorld(), cca.getGrid(), cca.getActionSrc(), slotItem, null );

					ContainerOpenContext context = cca.openContext;
					if ( context != null )
					{
						TileEntity te = context.getTile();
						Platform.openGUI( player, te, cca.openContext.side, GuiBridge.GUI_CRAFTING_CONFIRM );

						if ( player.openContainer instanceof ContainerCraftConfirm )
						{
							ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
							ccc.autoStart = heldShift;
							ccc.job = futureJob;
							cca.detectAndSendChanges();
						}
					}

				}
				catch (Throwable e)
				{
					if ( futureJob != null )
						futureJob.cancel( true );
					AELog.error( e );
				}
			}
		}
	}

	public PacketCraftRequest(IAEItemStack stack, int parseInt, boolean shift) throws IOException {
		this.slotItem = stack;
		this.slotItem.setStackSize( parseInt );
		this.heldShift = shift;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeBoolean( shift );
		slotItem.writeToPacket( data );

		configureWrite( data );
	}

}
