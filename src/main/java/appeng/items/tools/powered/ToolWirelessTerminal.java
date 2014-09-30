package appeng.items.tools.powered;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class ToolWirelessTerminal extends AEBasePoweredItem implements IWirelessTermHandler
{

	public ToolWirelessTerminal() {
		super( ToolWirelessTerminal.class, null );
		setFeature( EnumSet.of( AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools ) );
		maxStoredPower = AEConfig.instance.wirelessTerminalBattery;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player)
	{
		AEApi.instance().registries().wireless().openWirelessTerminalGui( item, w, player );
		return item;
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World w, int x, int y, int z, int side,
			float hitX, float hitY, float hitZ)
	{
		onItemRightClick( item, w, player );
		return true;
	}

	@Override
	public void addInformation(ItemStack i, EntityPlayer p, List l, boolean b)
	{
		super.addInformation( i, p, l, b );

		if ( i.hasTagCompound() )
		{
			NBTTagCompound tag = Platform.openNbtData( i );
			if ( tag != null )
			{
				String encKey = tag.getString( "encryptionKey" );

				if ( encKey == null || encKey.equals( "" ) )
					l.add( GuiText.Unlinked.getLocal() );
				else
					l.add( GuiText.Linked.getLocal() );
			}
		}
		else
			l.add( StatCollector.translateToLocal( "AppEng.GuiITooltip.Unlinked" ) );
	}

	@Override
	public boolean canHandle(ItemStack is)
	{
		return AEApi.instance().items().itemWirelessTerminal.sameAsStack( is );
	}

	@Override
	public boolean usePower(EntityPlayer player, double amount, ItemStack is)
	{
		return this.extractAEPower( is, amount ) >= amount - 0.5;
	}

	@Override
	public boolean hasPower(EntityPlayer player, double amt, ItemStack is)
	{
		return getAECurrentPower( is ) >= amt;
	}

	@Override
	public String getEncryptionKey(ItemStack item)
	{
		NBTTagCompound tag = Platform.openNbtData( item );
		return tag.getString( "encryptionKey" );
	}

	@Override
	public void setEncryptionKey(ItemStack item, String encKey, String name)
	{
		NBTTagCompound tag = Platform.openNbtData( item );
		tag.setString( "encryptionKey", encKey );
		tag.setString( "name", name );
	}

	@Override
	public IConfigManager getConfigManager(final ItemStack target)
	{
		final ConfigManager out = new ConfigManager( new IConfigManagerHost() {

			@Override
			public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
			{
				NBTTagCompound data = Platform.openNbtData( target );
				manager.writeToNBT( data );
			}

		} );

		out.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		out.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		out.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		out.readFromNBT( (NBTTagCompound) Platform.openNbtData( target ).copy() );
		return out;
	}

}
