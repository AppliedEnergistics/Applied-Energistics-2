package appeng.integration.modules;

import gregtechmod.api.interfaces.IDigitalChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.storage.IMEInventory;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IGT;
import appeng.integration.modules.helpers.GregTechQuantumChest;
import appeng.util.InventoryAdaptor;

public class GT implements IGT, IIntegrationModule
{

	public static GT instance;

	@Override
	public IMEInventory getQuantumChest(TileEntity te)
	{
		return new GregTechQuantumChest( (IInventory) te, InventoryAdaptor.getAdaptor( te, ForgeDirection.NORTH ) );
	}

	@Override
	public boolean isQuantumChest(TileEntity te)
	{
		if ( te instanceof IDigitalChest )
			return ((IDigitalChest) te).isDigitalChest();
		return false;
	}

	@Override
	public void Init()
	{

	}

	@Override
	public void PostInit()
	{

	}

}
