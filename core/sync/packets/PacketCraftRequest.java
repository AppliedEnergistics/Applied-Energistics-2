package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.crafting.CraftingJob;
import appeng.me.cache.CraftingCache;
import appeng.util.item.AEItemStack;

public class PacketCraftRequest extends AppEngPacket
{

	final public IAEItemStack slotItem;

	// automatic.
	public PacketCraftRequest(ByteBuf stream) throws IOException {
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

				CraftingCache cc = g.getCache( CraftingCache.class );

				try
				{
					CraftingJob cj = new CraftingJob( cca, slotItem, Actionable.SIMULATE );

				}
				catch (Throwable e)
				{
					AELog.error( e );
				}
			}
		}
	}

	public PacketCraftRequest(ItemStack stack, int parseInt) throws IOException {
		this.slotItem = AEApi.instance().storage().createItemStack( stack );
		this.slotItem.setStackSize( parseInt );

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		slotItem.writeToPacket( data );

		configureWrite( data );
	}

}
