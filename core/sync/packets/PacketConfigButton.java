package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.util.Platform;
import cpw.mods.fml.common.network.Player;

public class PacketConfigButton extends AppEngPacket
{

	final public Settings option;

	// automatic.
	public PacketConfigButton(DataInputStream stream) throws IOException {
		option = Settings.values()[stream.readInt()];
	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, Player player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		AEBaseContainer aebc = (AEBaseContainer) sender.openContainer;
		TileEntity bt = aebc.getTileEntity();
		if ( bt instanceof IConfigureableObject )
		{
			IConfigManager cm = ((IConfigureableObject) bt).getConfigManager();
			Enum newState = Platform.nextEnum( cm.getSetting( option ) );
			cm.putSetting( option, newState );
		}
	}

	// api
	public PacketConfigButton(Settings option) throws IOException {
		this.option = option;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeInt( option.ordinal() );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
