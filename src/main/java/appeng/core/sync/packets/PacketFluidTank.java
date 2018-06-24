
package appeng.core.sync.packets;


import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.container.ContainerFluidInterface;


public class PacketFluidTank extends AppEngPacket
{
	private final Map<Integer, NBTTagCompound> updateMap;

	public PacketFluidTank( final ByteBuf stream )
	{
		this.updateMap = new HashMap<>();
		NBTTagCompound tags = ByteBufUtils.readTag( stream );

		for( final String key : tags.getKeySet() )
		{
			updateMap.put( Integer.parseInt( key ), tags.getCompoundTag( key ) );
		}
	}

	// api
	public PacketFluidTank( final Map<Integer, NBTTagCompound> updateMap )
	{
		this.updateMap = updateMap;

		final NBTTagCompound tag = new NBTTagCompound();
		for( Map.Entry<Integer, NBTTagCompound> e : updateMap.entrySet() )
		{
			tag.setTag( e.getKey().toString(), e.getValue() );
		}
		final ByteBuf data = Unpooled.buffer();
		data.writeInt( this.getPacketID() );
		ByteBufUtils.writeTag( data, tag );
		this.configureWrite( data );
	}

	public Map<Integer, NBTTagCompound> getUpdateMap()
	{
		return this.updateMap;
	}

	@Override
	public void clientPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		final Container c = player.openContainer;
		if( c instanceof ContainerFluidInterface )
		{
			( (ContainerFluidInterface) c ).receiveTankInfo( this.updateMap );
		}
	}
}
