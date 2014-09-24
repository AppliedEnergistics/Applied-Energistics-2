package appeng.core.api;

import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public interface IIMCHandler
{

	void post(IMCMessage m);

}
