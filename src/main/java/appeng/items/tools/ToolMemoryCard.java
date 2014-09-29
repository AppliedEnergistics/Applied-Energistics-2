package appeng.items.tools;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolMemoryCard extends AEBaseItem implements IMemoryCard
{

	public ToolMemoryCard() {
		super( ToolMemoryCard.class );
		setFeature( EnumSet.of( AEFeature.Core ) );
		setMaxStackSize( 1 );
	}

	/**
	 * Find the localized string...
	 *
	 * @param name possible names for the localized string
	 * @return localized name
	 */
	private String getLocalizedName(String... name)
	{
		for (String n : name)
		{
			String l = StatCollector.translateToLocal( n );
			if ( !l.equals( n ) )
				return l;
		}

		for (String n : name)
			return n;

		return "";
	}

	@Override
	public void addInformation(ItemStack i, EntityPlayer p, List l, boolean b)
	{
		l.add( getLocalizedName( getSettingsName( i ) + ".name", getSettingsName( i ) ) );

		NBTTagCompound data = getData( i );
		if ( data.hasKey( "tooltip" ) )
			l.add( StatCollector.translateToLocal( getLocalizedName( data.getString( "tooltip" ) + ".name", data.getString( "tooltip" ) ) ) );
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	@Override
	public void setMemoryCardContents(ItemStack is, String SettingsName, NBTTagCompound data)
	{
		NBTTagCompound c = Platform.openNbtData( is );
		c.setString( "Config", SettingsName );
		c.setTag( "Data", data );
	}

	@Override
	public String getSettingsName(ItemStack is)
	{
		NBTTagCompound c = Platform.openNbtData( is );
		String name = c.getString( "Config" );
		return name == null || name == "" ? GuiText.Blank.getUnlocalized() : name;
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
	public boolean onItemUse(ItemStack is, EntityPlayer player, World w, int x, int y, int z, int side, float hx, float hy, float hz)
	{
		if ( player.isSneaking() && !w.isRemote )
		{
			IMemoryCard mem = (IMemoryCard) is.getItem();
			mem.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );
			is.setTagCompound( null );
			return true;
		}
		else
			return super.onItemUse( is, player, w, x, y, z, side, hx, hy, hz );
	}

	@Override
	public void notifyUser(EntityPlayer player, MemoryCardMessages msg)
	{
		if ( Platform.isClient() )
			return;

		switch (msg)
		{
		case SETTINGS_CLEARED:
			player.addChatMessage( PlayerMessages.SettingCleared.get() );
			break;
		case INVALID_MACHINE:
			player.addChatMessage( PlayerMessages.InvalidMachine.get() );
			break;
		case SETTINGS_LOADED:
			player.addChatMessage( PlayerMessages.LoadedSettings.get() );
			break;
		case SETTINGS_SAVED:
			player.addChatMessage( PlayerMessages.SavedSettings.get() );
			break;
		default:
		}
	}

}
