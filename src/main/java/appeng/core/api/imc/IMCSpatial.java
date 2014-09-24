/* Example:

FMLInterModComms.sendMessage( "appliedenergistics2", "whitelist-spatial", "mymod.tileentities.MyTileEntity" );

 */
package appeng.core.api.imc;

import appeng.api.AEApi;
import appeng.core.AELog;
import appeng.core.api.IIMCHandler;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCSpatial implements IIMCHandler
{

	@Override
	public void post(IMCMessage m)
	{

		try
		{
			Class classInstance = Class.forName( m.getStringValue() );
			AEApi.instance().registries().movable().whiteListTileEntity( classInstance );
		}
		catch (ClassNotFoundException e)
		{
			AELog.info( "Bad Class Registered: " + m.getStringValue() + " by " + m.getSender() );
		}

	}

}
