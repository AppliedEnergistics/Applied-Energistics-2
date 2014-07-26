package appeng.integration.modules.helpers;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.eventhandler.Event;

public class FMPPacketEvent extends Event
{

	public final EntityPlayerMP sender;

	public FMPPacketEvent(EntityPlayerMP sender) {
		this.sender = sender;
	}

}
