package appeng.items.tools;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import appeng.api.implementations.IMemoryCard;
import appeng.api.implementations.MemoryCardMessages;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolMemoryCard extends AEBaseItem implements IMemoryCard
{

	public ToolMemoryCard() {
		super( ToolMemoryCard.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	public void addInformation(ItemStack i, EntityPlayer p, List l, boolean b)
	{
		l.add( StatCollector.translateToLocal( getSettingsName( i ) ) );
		NBTTagCompound data = getData( i );
		if ( data.hasKey( "tooltip" ) )
		{
			l.add( StatCollector.translateToLocal( data.getString( "tooltip" ) ) );
		}
	}

	@Override
	public void setMemoryCardContents(ItemStack is, String SettingsName, NBTTagCompound data)
	{
		NBTTagCompound c = Platform.openNbtData( is );
		c.setString( "Config", SettingsName );
		;
		c.setCompoundTag( "Data", data );
	}

	@Override
	public String getSettingsName(ItemStack is)
	{
		NBTTagCompound c = Platform.openNbtData( is );
		String name = c.getString( "Config" );
		return name == null || name == "" ? "gui.appliedenergistics2.Blank" : name;
	}

	@Override
	public NBTTagCompound getData(ItemStack is)
	{
		NBTTagCompound c = Platform.openNbtData( is );
		NBTTagCompound o = c.getCompoundTag( "Data" );
		if ( o == null )
			o = new NBTTagCompound();
		return (NBTTagCompound) o.copy();
	}

	@Override
	public void notifyUser(Block blk, EntityPlayer player, MemoryCardMessages msg)
	{
		switch (msg)
		{
		case INVALID_MACHINE:
			player.sendChatToPlayer( PlayerMessages.InvalidMachine.get() );
			break;
		case SETTINGS_LOADED:
			player.sendChatToPlayer( PlayerMessages.LoadedSettings.get() );
			break;
		case SETTINGS_SAVED:
			player.sendChatToPlayer( PlayerMessages.SavedSettings.get() );
			break;
		default:
		}
	}

}
