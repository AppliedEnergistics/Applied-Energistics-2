package appeng.block;

import java.text.MessageFormat;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class AEBaseItemBlockChargeable extends AEBaseItemBlock implements IAEItemPowerStorage
{

	public AEBaseItemBlockChargeable(Block id) {
		super( id );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addCheckedInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip, boolean advancedTooltips)
	{
		NBTTagCompound tag = itemStack.getTagCompound();
		double internalCurrentPower = 0;
		double internalMaxPower = this.getMaxEnergyCapacity();

		if ( tag != null )
		{
			internalCurrentPower = tag.getDouble( "internalCurrentPower" );
		}

		double percent = internalCurrentPower / internalMaxPower;

		toolTip.add( GuiText.StoredEnergy.getLocal() + ":" + MessageFormat.format( " {0,number,#} ", internalCurrentPower )
				+ Platform.gui_localize( PowerUnits.AE.unlocalizedName ) + " - " + MessageFormat.format( " {0,number,#.##%} ", percent ) );

	}

	private double getMaxEnergyCapacity()
	{
		Block blk = Block.getBlockFromItem( this );
		if ( blk == AEApi.instance().blocks().blockEnergyCell.block() )
			return 200000;
		else
			return 8 * 200000;
	}

	private double getInternal(ItemStack is)
	{
		NBTTagCompound nbt = Platform.openNbtData( is );
		return nbt.getDouble( "internalCurrentPower" );
	}

	private void setInternal(ItemStack is, double amt)
	{
		NBTTagCompound nbt = Platform.openNbtData( is );
		nbt.setDouble( "internalCurrentPower", amt );
	}

	@Override
	public double injectAEPower(ItemStack is, double amt)
	{
		double internalCurrentPower = getInternal( is );
		double internalMaxPower = getMaxEnergyCapacity();
		internalCurrentPower += amt;
		if ( internalCurrentPower > internalMaxPower )
		{
			amt = internalCurrentPower - internalMaxPower;
			internalCurrentPower = internalMaxPower;
			setInternal( is, internalCurrentPower );
			return amt;
		}

		setInternal( is, internalCurrentPower );
		return 0;
	}

	@Override
	public double extractAEPower(ItemStack is, double amt)
	{
		double internalCurrentPower = getInternal( is );
		if ( internalCurrentPower > amt )
		{
			internalCurrentPower -= amt;
			setInternal( is, internalCurrentPower );
			return amt;
		}

		amt = internalCurrentPower;
		setInternal( is, 0 );
		return amt;
	}

	@Override
	public double getAEMaxPower(ItemStack is)
	{
		return getMaxEnergyCapacity();
	}

	@Override
	public double getAECurrentPower(ItemStack is)
	{
		return getInternal( is );
	}

	@Override
	public AccessRestriction getPowerFlow(ItemStack is)
	{
		return AccessRestriction.WRITE;
	}

}
