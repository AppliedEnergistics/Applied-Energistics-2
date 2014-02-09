package appeng.core.features.registries;

import net.minecraft.entity.player.EntityPlayer;
import appeng.api.features.IPlayerRegistry;
import appeng.core.WorldSettings;

public class PlayerRegistry implements IPlayerRegistry
{

	@Override
	public int getID(String username)
	{
		return WorldSettings.getInstance().getPlayerID( username );
	}

	@Override
	public int getID(EntityPlayer player)
	{
		return WorldSettings.getInstance().getPlayerID( player.getCommandSenderName() );
	}

	@Override
	public String getUsername(int id)
	{
		return WorldSettings.getInstance().getUsername( id );
	}

}
