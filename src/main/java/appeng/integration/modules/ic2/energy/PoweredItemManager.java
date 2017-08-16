
package appeng.integration.modules.ic2.energy;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import ic2.api.item.IBackupElectricItemManager;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;


public class PoweredItemManager implements IBackupElectricItemManager
{

	@Override
	public double charge( ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate )
	{
		final double limit = this.getTransferLimit( stack );
		final IAEItemPowerStorage poweredItem = (IAEItemPowerStorage) stack.getItem();
		final double convertedPower = PowerUnits.EU.convertTo( PowerUnits.AE, amount );

		double toAdd = convertedPower;

		if( !ignoreTransferLimit && amount > limit )
		{
			toAdd = limit;
		}

		final double overflow = poweredItem.injectAEPower( stack, toAdd, simulate ? Actionable.SIMULATE : Actionable.MODULATE );
		final double addedAmount = toAdd - (int) overflow;

		return PowerUnits.AE.convertTo( PowerUnits.EU, addedAmount );
	}

	@Override
	public double discharge( ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate )
	{
		return 0;
	}

	@Override
	public double getCharge( ItemStack stack )
	{
		final IAEItemPowerStorage poweredItem = (IAEItemPowerStorage) stack.getItem();
		return (int) PowerUnits.AE.convertTo( PowerUnits.EU, poweredItem.getAECurrentPower( stack ) );
	}

	@Override
	public double getMaxCharge( ItemStack stack )
	{
		final IAEItemPowerStorage poweredItem = (IAEItemPowerStorage) stack.getItem();
		return PowerUnits.AE.convertTo( PowerUnits.EU, poweredItem.getAEMaxPower( stack ) );
	}

	@Override
	public boolean canUse( ItemStack stack, double amount )
	{
		return this.getCharge( stack ) > amount;
	}

	@Override
	public boolean use( ItemStack stack, double amount, EntityLivingBase entity )
	{
		final IAEItemPowerStorage poweredItem = (IAEItemPowerStorage) stack.getItem();

		if( this.canUse( stack, amount ) )
		{
			final double toUse = PowerUnits.EU.convertTo( PowerUnits.AE, amount );

			poweredItem.extractAEPower( stack, toUse, Actionable.MODULATE );

			return true;
		}
		return false;
	}

	@Override
	public void chargeFromArmor( ItemStack stack, EntityLivingBase entity )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getToolTip( ItemStack stack )
	{
		return null;
	}

	@Override
	public int getTier( ItemStack stack )
	{
		return 1;
	}

	@Override
	public boolean handles( ItemStack stack )
	{
		return !stack.isEmpty() && ( stack.getItem() instanceof IAEItemPowerStorage );
	}

	private double getTransferLimit( ItemStack itemStack )
	{
		return Math.max( 32, this.getMaxCharge( itemStack ) / 200 );
	}

}
