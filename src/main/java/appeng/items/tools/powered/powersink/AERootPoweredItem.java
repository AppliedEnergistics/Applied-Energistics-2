package appeng.items.tools.powered.powersink;

import java.text.MessageFormat;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class AERootPoweredItem extends AEBaseItem implements IAEItemPowerStorage
{

	private enum batteryOperation
	{
		STORAGE, INJECT, EXTRACT
	}

	public double maxStoredPower = 200000;

	public AERootPoweredItem(Class c, String subname) {
		super( c, subname );
		setMaxDamage( 32 );
		hasSubtypes = false;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List lines, boolean advancedItemTooltips)
	{
		NBTTagCompound tag = is.getTagCompound();
		double internalCurrentPower = 0;
		double internalMaxPower = getAEMaxPower( is );

		if ( tag != null )
		{
			internalCurrentPower = tag.getDouble( "internalCurrentPower" );
		}

		double percent = internalCurrentPower / internalMaxPower;

		lines.add( GuiText.StoredEnergy.getLocal() + ":" + MessageFormat.format( " {0,number,#} ", internalCurrentPower )
				+ Platform.gui_localize( PowerUnits.AE.unlocalizedName ) + " - " + MessageFormat.format( " {0,number,#.##%} ", percent ) );

	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public boolean isDamaged(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public void setDamage(ItemStack stack, int damage)
	{

	}

	final String EnergyVar = "internalCurrentPower";

	private double getInternalBattery(ItemStack is, batteryOperation op, double adjustment)
	{
		NBTTagCompound data = Platform.openNbtData( is );

		double currentStorage = data.getDouble( EnergyVar );
		double maxStorage = getAEMaxPower( is );

		switch (op)
		{
		case INJECT:
			currentStorage += adjustment;
			if ( currentStorage > maxStorage )
			{
				double diff = currentStorage - maxStorage;
				data.setDouble( EnergyVar, maxStorage );
				return diff;
			}
			data.setDouble( EnergyVar, currentStorage );
			return 0;
		case EXTRACT:
			if ( currentStorage > adjustment )
			{
				currentStorage -= adjustment;
				data.setDouble( EnergyVar, currentStorage );
				return adjustment;
			}
			data.setDouble( EnergyVar, 0 );
			return currentStorage;
		default:
			break;
		}

		return currentStorage;
	}

	/**
	 * inject external
	 */
	double injectExternalPower(PowerUnits input, ItemStack is, double amount, boolean simulate)
	{
		if ( simulate )
		{
			int requiredEU = (int) PowerUnits.AE.convertTo( PowerUnits.EU, getAEMaxPower( is ) - getAECurrentPower( is ) );
			if ( amount < requiredEU )
				return 0;
			return amount - requiredEU;
		}
		else
		{
			double powerRemainder = injectAEPower( is, PowerUnits.EU.convertTo( PowerUnits.AE, amount ) );
			return PowerUnits.AE.convertTo( PowerUnits.EU, powerRemainder );
		}
	}

	@Override
	public double injectAEPower(ItemStack is, double amt)
	{
		return getInternalBattery( is, batteryOperation.INJECT, amt );
	}

	@Override
	public double extractAEPower(ItemStack is, double amt)
	{
		return getInternalBattery( is, batteryOperation.EXTRACT, amt );
	}

	@Override
	public double getAEMaxPower(ItemStack is)
	{
		return maxStoredPower;
	}

	@Override
	public double getAECurrentPower(ItemStack is)
	{
		return getInternalBattery( is, batteryOperation.STORAGE, 0 );
	}

	@Override
	public AccessRestriction getPowerFlow(ItemStack is)
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public int getDisplayDamage(ItemStack is)
	{
		return 32 - (int) (32 * (getAECurrentPower( is ) / getAEMaxPower( is )));
	}

	@Override
	public void getSubItems(Item id, CreativeTabs tab, List list)
	{
		super.getSubItems( id, tab, list );

		ItemStack charged = new ItemStack( this, 1 );
		NBTTagCompound tag = Platform.openNbtData( charged );
		tag.setDouble( "internalCurrentPower", getAEMaxPower( charged ) );
		tag.setDouble( "internalMaxPower", getAEMaxPower( charged ) );
		list.add( charged );
	}

}
