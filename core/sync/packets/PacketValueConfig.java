package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import appeng.api.config.FuzzyMode;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerPriority;
import appeng.core.sync.AppEngPacket;

public class PacketValueConfig extends AppEngPacket
{

	final public String Name;
	final public String Value;

	// automatic.
	public PacketValueConfig(DataInputStream stream) throws IOException {
		Name = stream.readUTF();
		Value = stream.readUTF();
	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;

		if ( Name.equals( "PriorityHost.Priority" ) && c instanceof ContainerPriority )
		{
			ContainerPriority pc = (ContainerPriority) c;
			pc.setPriority( Integer.parseInt( Value ), player );
			return;
		}
		else if ( Name.equals( "LevelEmitter.Value" ) && c instanceof ContainerLevelEmitter )
		{
			ContainerLevelEmitter lvc = (ContainerLevelEmitter) c;
			lvc.setLevel( Integer.parseInt( Value ), player );
			return;
		}
		else if ( Name.startsWith( "CellWorkbench." ) && c instanceof ContainerCellWorkbench )
		{
			ContainerCellWorkbench ccw = (ContainerCellWorkbench) c;
			if ( Name.equals( "CellWorkbench.Action" ) )
			{
				if ( Value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if ( Value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
			else if ( Name.equals( "CellWorkbench.Fuzzy" ) )
			{
				ccw.setFuzzy( FuzzyMode.valueOf( Value ) );
			}
		}

	}

	// api
	public PacketValueConfig(String Name, String Value) throws IOException {
		this.Name = Name;
		this.Value = Value;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeUTF( Name );
		data.writeUTF( Value );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}
}
