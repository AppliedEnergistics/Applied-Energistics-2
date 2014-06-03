package appeng.items.tools.powered.powersink;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.transformer.annotations.integration.Method;

@InterfaceList(value = { @Interface(iface = "ic2.api.item.ISpecialElectricItem", iname = "IC2"),
		@Interface(iface = "ic2.api.item.IElectricItemManager", iname = "IC2") })
public class IC2 extends AERootPoweredItem implements IElectricItemManager, ISpecialElectricItem
{

	public IC2(Class c, String subname) {
		super( c, subname );
	}

	@Override
	public int charge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		int addedAmt = amount;
		int limit = getTransferLimit( is );

		if ( !ignoreTransferLimit && amount > limit )
			addedAmt = limit;

		return addedAmt - ((int) injectExternalPower( PowerUnits.EU, is, addedAmt, simulate ));
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

	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return false;
	}

	@Override
	public Item getChargedItem(ItemStack itemStack)
	{
		return itemStack.getItem();
	}

	@Override
	public Item getEmptyItem(ItemStack itemStack)
	{
		return itemStack.getItem();
	}

	@Override
	public int getMaxCharge(ItemStack itemStack)
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.EU, getAEMaxPower( itemStack ) );
	}

	@Override
	public int getTier(ItemStack itemStack)
	{
		return 1;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return Math.max( 32, getMaxCharge( itemStack ) / 200 );
	}

	@Override
	@Method(iname = "IC2")
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return this;
	}

}
