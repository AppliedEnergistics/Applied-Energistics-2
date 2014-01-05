package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.util.Platform;

public class PacketConfigButton extends AppEngPacket
{

	final public Settings option;
	final public boolean rotationDirection;

	// automatic.
	public PacketConfigButton(DataInputStream stream) throws IOException {
		option = Settings.values()[stream.readInt()];
		rotationDirection = stream.readBoolean();
	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		AEBaseContainer aebc = (AEBaseContainer) sender.openContainer;
		if ( aebc.getTarget() instanceof IConfigureableObject )
		{
			IConfigManager cm = ((IConfigureableObject) aebc.getTarget()).getConfigManager();
			Enum newState = Platform.rotateEnum( cm.getSetting( option ), rotationDirection, option.getPossibleValues() );
			cm.putSetting( option, newState );
		}
	}

	// api
	public PacketConfigButton(Settings option, boolean rotationDirection) throws IOException {
		this.option = option;
		this.rotationDirection = rotationDirection;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeInt( option.ordinal() );
		data.writeBoolean( rotationDirection );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}
}
