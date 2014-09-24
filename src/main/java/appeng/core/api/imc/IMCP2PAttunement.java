/* Example:

FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-me", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-bc-power", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-ic2-power", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-redstone", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-fluid", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-item", new ItemStack( myBlockOrItem ) );

 */
package appeng.core.api.imc;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.core.api.IIMCHandler;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCP2PAttunement implements IIMCHandler
{

	@Override
	public void post(IMCMessage m)
	{
		String key = m.key.substring( "add-p2p-attunement-".length() ).replace( '-', '_' ).toUpperCase();

		TunnelType type = TunnelType.valueOf( key );

		if ( type != null )
		{
			ItemStack is = m.getItemStackValue();
			if ( is != null )
				AEApi.instance().registries().p2pTunnel().addNewAttunement( is, type );
			else
				throw new RuntimeException( "invalid item" );
		}
		else
			throw new RuntimeException( "invalid type" );
	}

}
