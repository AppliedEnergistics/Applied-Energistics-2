package appeng.api.networking.security;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerSource extends BaseActionSource
{

	public final EntityPlayer player;
	public final IActionHost via;

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public PlayerSource(EntityPlayer p, IActionHost v) {
		player = p;
		via = v;
	}

}
