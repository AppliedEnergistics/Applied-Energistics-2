package appeng.items.tools.powered.powersink;

import ic2.api.item.IElectricItemManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import appeng.api.config.PowerUnits;

public class IC2 extends AERootPoweredItem implements IElectricItemManager
{

	public IC2(Class c, String subname) {
		super( c, subname );
	}

	@Override
	public int charge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		return amount - ((int) injectExternalPower( PowerUnits.EU, is, amount, simulate ));
	}

	@Override
	public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		return 0;
	}

	@Override
	public int getCharge(ItemStack is)
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.EU, getAECurrentPower( is ) );
	}

	@Override
	public boolean canUse(ItemStack is, int amount)
	{
		return getCharge( is ) > amount;
	}

	@Override
	public boolean use(ItemStack is, int amount, EntityLivingBase entity)
	{
		if ( canUse( is, amount ) )
		{
			// use the power..
			extractAEPower( is, PowerUnits.EU.convertTo( PowerUnits.AE, amount ) );
			return true;
		}
		return false;
	}

	@Override
	public void chargeFromArmor(ItemStack itemStack, EntityLivingBase entity)
	{
		// wtf?
	}

	@Override
	public String getToolTip(ItemStack itemStack)
	{
		return null;
	}

}
