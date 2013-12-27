package appeng.core.features.registries.entries;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.util.Platform;
import cpw.mods.fml.common.network.Player;

public class BasicTerminalHandler implements IWirelessTermHandler
{

	@Override
	public boolean canHandle(ItemStack is)
	{
		if ( is == null )
			return false;

		if ( AEApi.instance().items().itemWirelessTerminal.sameAs( is ) )
			return true;

		return false;
	}

	@Override
	public boolean usePower(Player player, float amount, ItemStack is)
	{
		return false;
	}

	@Override
	public boolean hasPower(Player player, ItemStack is)
	{
		return false;
	}

	@Override
	public String getEncryptionKey(ItemStack i)
	{
		if ( i == null )
			return null;
		NBTTagCompound tag = Platform.openNbtData( i );
		if ( tag != null )
		{
			return tag.getString( "encKey" );
		}
		return null;
	}

	@Override
	public void setEncryptionKey(ItemStack i, String encKey, String name)
	{
		if ( i == null )
			return;
		NBTTagCompound tag = Platform.openNbtData( i );
		if ( tag != null )
		{
			tag.setString( "encKey", encKey );
		}
	}

}
