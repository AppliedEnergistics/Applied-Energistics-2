package appeng.integration.modules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.integration.BaseModule;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.ITE;
import cpw.mods.fml.common.event.FMLInterModComms;

public class TE extends BaseModule implements IIntegrationModule, ITE
{

	public static TE instance;

	@Override
	public void Init()
	{

	}

	@Override
	public void PostInit()
	{
		// certus quartz
		pulverizer( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		pulverizer( AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 ), AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		// fluix
		pulverizer( AEApi.instance().materials().materialFluixCrystal.stack( 1 ), AEApi.instance().materials().materialFluixDust.stack( 1 ) );

		// nether quartz
		pulverizer( new ItemStack( Item.netherQuartz ), AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
	}

	private void pulverizer(ItemStack in, ItemStack out)
	{
		NBTTagCompound toSend = new NBTTagCompound();
		toSend.setInteger( "energy", 3200 );
		toSend.setTag( "input", new NBTTagCompound() );
		toSend.setTag( "primaryOutput", new NBTTagCompound() );

		in.writeToNBT( toSend.getCompoundTag( "input" ) );
		out.writeToNBT( toSend.getCompoundTag( "primaryOutput" ) );
		FMLInterModComms.sendMessage( "ThermalExpansion", "PulverizerRecipe", toSend );
	}

	@Override
	public ItemStack addItemsToPipe(TileEntity ad, ItemStack itemstack, ForgeDirection dir)
	{
		return ((IItemConduit) ad).insertItem( dir, itemstack );
	}

	@Override
	public void addPulverizerRecipe(int i, ItemStack blkQuartz, ItemStack blockDust)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPulverizerRecipe(int i, ItemStack blkQuartzOre, ItemStack matQuartz, ItemStack matQuartzDust)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPipe(TileEntity te, ForgeDirection opposite)
	{
		return te instanceof IItemConduit;
	}

}
