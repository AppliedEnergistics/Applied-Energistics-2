package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;

public class PacketCraftRequest extends AppEngPacket
{

	final public long amount;
	final public boolean heldShift;

	// automatic.
	public PacketCraftRequest(ByteBuf stream)
	{
		heldShift = stream.readBoolean();
		amount = stream.readLong();
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		if ( player.openContainer instanceof ContainerCraftAmount )
		{
			ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
			Object target = cca.getTarget();
			if ( target instanceof IGridHost )
			{
				IGridHost gh = (IGridHost) target;
				IGridNode gn = gh.getGridNode( ForgeDirection.UNKNOWN );
				if ( gn == null )
					return;

				IGrid g = gn.getGrid();
				if ( g == null || cca.whatToMake == null )
					return;

				Future<ICraftingJob> futureJob = null;

				cca.whatToMake.setStackSize( amount );

				try
				{
					ICraftingGrid cg = g.getCache( ICraftingGrid.class );
					futureJob = cg.beginCraftingJob( cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.whatToMake, null );

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

	public PacketCraftRequest(int craftAmt, boolean shift)
	{
		this.amount = craftAmt;
		this.heldShift = shift;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeBoolean( shift );
		data.writeLong( amount );

		configureWrite( data );
	}

}
