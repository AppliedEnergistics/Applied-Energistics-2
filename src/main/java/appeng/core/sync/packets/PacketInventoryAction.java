package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PacketInventoryAction extends AppEngPacket
{

	final public InventoryAction action;
	final public int slot;
	final public long id;
	final public IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction(ByteBuf stream) throws IOException {
		action = InventoryAction.values()[stream.readInt()];
		slot = stream.readInt();
		id = stream.readLong();
		boolean hasItem = stream.readBoolean();
		if ( hasItem )
			slotItem = AEItemStack.loadItemStackFromPacket( stream );
		else
			slotItem = null;
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		if ( sender.openContainer instanceof AEBaseContainer )
		{
			AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
			if ( action == InventoryAction.AUTO_CRAFT )
			{
				ContainerOpenContext context = baseContainer.openContext;
				if ( context != null )
				{
					TileEntity te = context.getTile();
					Platform.openGUI( sender, te, baseContainer.openContext.side, GuiBridge.GUI_CRAFTING_AMOUNT );

					if ( sender.openContainer instanceof ContainerCraftAmount )
					{
						ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;

						if ( baseContainer.getTargetStack() != null )
						{
							cca.craftingItem.putStack( baseContainer.getTargetStack().getItemStack() );
							cca.whatToMake = baseContainer.getTargetStack();
						}

						cca.detectAndSendChanges();
					}
				}
			}
			else
			{
				baseContainer.doAction( sender, action, slot, id );
			}
		}
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		if ( action == InventoryAction.UPDATE_HAND )
		{
			if ( slotItem == null )
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( null );
			else
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( slotItem.getItemStack() );
		}
	}

	// api
	public PacketInventoryAction(InventoryAction action, int slot, IAEItemStack slotItem) throws IOException {

		if ( Platform.isClient() )
			throw new RuntimeException( "invalid packet, client cannot post inv actions with stacks." );

		this.action = action;
		this.slot = slot;
		this.id = 0;
		this.slotItem = slotItem;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( id );

		if ( slotItem == null )
			data.writeBoolean( false );
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}

		configureWrite( data );
	}

	// api
	public PacketInventoryAction(InventoryAction action, int slot, long id) throws IOException {

		this.action = action;
		this.slot = slot;
		this.id = id;
		this.slotItem = null;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( id );
		data.writeBoolean( false );

		configureWrite( data );
	}
}
