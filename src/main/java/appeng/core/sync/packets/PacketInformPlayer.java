package appeng.core.sync.packets;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class PacketInformPlayer extends AppEngPacket
{
	private IAEItemStack actualItem = null;
	private IAEItemStack reportedItem = null;
	private final InfoType type;

	public PacketInformPlayer( ByteBuf stream ) throws IOException
	{
		this.type = InfoType.values()[stream.readInt()];
		switch ( type )
		{
			case PARTIAL_ITEM_EXTRACTION:
				this.reportedItem = AEItemStack.fromPacket( stream );
				this.actualItem = AEItemStack.fromPacket( stream );
				break;
			case NO_ITEMS_EXTRACTED:
				this.reportedItem = AEItemStack.fromPacket( stream );
				break;
		}
	}

	public PacketInformPlayer( IAEItemStack extra, IAEItemStack result, InfoType type ) throws IOException
	{
		this.reportedItem = extra;
		this.actualItem = result;
		this.type = type;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );

		data.writeInt( type.ordinal() );

		reportedItem.writeToPacket( data );
		if( actualItem != null )
		{
			actualItem.writeToPacket( data );
		}

		this.configureWrite( data );
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player )
	{
		TextComponentString msg = null;

		if( this.type == InfoType.PARTIAL_ITEM_EXTRACTION )
		{
			AppEng.proxy.getPlayers().get( 0 ).sendStatusMessage( new TextComponentString( "System reported " + reportedItem.getStackSize() + " " + reportedItem.getItem().getItemStackDisplayName( reportedItem.getDefinition() ) + " available but could only extract" + actualItem.getStackSize() ), false );
		}
		else if( this.type == InfoType.NO_ITEMS_EXTRACTED )
		{
			AppEng.proxy.getPlayers().get( 0 ).sendStatusMessage( new TextComponentString( "System reported " + reportedItem.getStackSize() + " " + reportedItem.getItem().getItemStackDisplayName( reportedItem.getDefinition() ) + " available but could not extract anything" ), false );
		}
	}

	public enum InfoType
	{
		PARTIAL_ITEM_EXTRACTION, NO_ITEMS_EXTRACTED
	}
}
