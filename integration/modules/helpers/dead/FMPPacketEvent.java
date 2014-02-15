package appeng.integration.modules.helpers.dead;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.Event;

public class FMPPacketEvent extends Event
{

	public final EntityPlayerMP sender;

	public FMPPacketEvent(EntityPlayerMP sender) {
		this.sender = sender;
	}

}
