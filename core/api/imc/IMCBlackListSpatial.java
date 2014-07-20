package appeng.core.api.imc;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.core.AELog;
import appeng.core.api.IIMCHandler;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCBlackListSpatial implements IIMCHandler
{

	@Override
	public void post(IMCMessage m)
	{

		ItemStack is = m.getItemStackValue();
		if ( is != null )
		{
			Block blk = Block.getBlockFromItem( is.getItem() );
			if ( blk != null )
			{
				AEApi.instance().registries().moveable().blacklistBlock( blk );
				return;
			}
		}

		AELog.info( "Bad Block blacklisted by " + m.getSender() );

	}

}
