package appeng.integration.modules;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.block.AEBaseBlock;
import appeng.core.AppEng;
import appeng.core.localization.WailaText;
import appeng.integration.BaseModule;
import appeng.tile.misc.TileCharger;
import appeng.tile.networking.TileEnergyCell;
import appeng.util.Platform;
import cpw.mods.fml.common.event.FMLInterModComms;

public class Waila extends BaseModule implements IWailaDataProvider
{

	public static Waila instance;

	public static void register(IWailaRegistrar registrar)
	{
		// registrar.registerHeadProvider( (Waila) AppEng.instance.getIntegration( "Waila" ), AEBaseBlock.class );
		registrar.registerBodyProvider( (Waila) AppEng.instance.getIntegration( "Waila" ), AEBaseBlock.class );
		registrar.registerSyncedNBTKey( "internalCurrentPower", TileEnergyCell.class );
		// registrar.registerTailProvider( (Waila) AppEng.instance.getIntegration( "Waila" ), AEBaseBlock.class );
	}

	@Override
	public void Init() throws Throwable
	{
		TestClass( IWailaDataProvider.class );
		TestClass( IWailaRegistrar.class );
		FMLInterModComms.sendMessage( "Waila", "register", this.getClass().getName() + ".register" );
	}

	@Override
	public void PostInit() throws Throwable
	{
		// :P
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{

		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{

		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity te = accessor.getTileEntity();

		Object ThingOfInterest = te;
		if ( te instanceof IPartHost )
		{
			MovingObjectPosition mop = accessor.getPosition();
			Vec3 Pos = mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ );
			SelectedPart sp = ((IPartHost) te).selectPart( Pos );
			if ( sp.facade != null )
			{
				IFacadePart fp = sp.facade;
				ThingOfInterest = fp;
			}
			else if ( sp.part != null )
			{
				IPart part = sp.part;
				ThingOfInterest = part;
			}
		}

		if ( ThingOfInterest instanceof TileEnergyCell )
		{
			NBTTagCompound c = accessor.getNBTData();
			if ( c != null && c.hasKey( "internalCurrentPower" ) )
			{
				TileEnergyCell tec = (TileEnergyCell) ThingOfInterest;
				long power = (long) (100 * c.getDouble( "internalCurrentPower" ));
				currenttip.add( WailaText.Contains + ": " + Platform.formatPowerLong( power, false ) + " / "
						+ Platform.formatPowerLong( (long) (100 * tec.getAEMaxPower()), false ) );
			}
		}

		if ( ThingOfInterest instanceof IPartStorageMonitor )
		{
			IPartStorageMonitor psm = (IPartStorageMonitor) ThingOfInterest;
			IAEStack stack = psm.getDisplayed();
			boolean isLocked = psm.isLocked();

			if ( stack instanceof IAEItemStack )
			{
				IAEItemStack ais = (IAEItemStack) stack;
				currenttip.add( WailaText.Showing.getLocal() + ": " + ais.getItemStack().getDisplayName() );
			}

			if ( stack instanceof IAEFluidStack )
			{
				IAEFluidStack ais = (IAEFluidStack) stack;
				currenttip.add( WailaText.Showing.getLocal() + ": " + ais.getFluid().getLocalizedName() );
			}

			if ( isLocked )
				currenttip.add( WailaText.Locked.getLocal() );
			else
				currenttip.add( WailaText.Unlocked.getLocal() );
		}

		if ( ThingOfInterest instanceof TileCharger )
		{
			TileCharger tc = (TileCharger) ThingOfInterest;
			IInventory inv = tc.getInternalInventory();
			ItemStack is = inv.getStackInSlot( 0 );
			if ( is != null )
			{
				currenttip.add( WailaText.Contains + ": " + is.getDisplayName() );
				is.getItem().addInformation( is, accessor.getPlayer(), currenttip, true );
			}
		}

		if ( ThingOfInterest instanceof IPowerChannelState )
		{
			IPowerChannelState pbs = (IPowerChannelState) ThingOfInterest;
			if ( pbs.isActive() && pbs.isPowered() )
				currenttip.add( WailaText.DeviceOnline.getLocal() );
			else if ( pbs.isPowered() )
				currenttip.add( WailaText.DeviceMissingChannel.getLocal() );
			else
				currenttip.add( WailaText.DeviceOffline.getLocal() );
		}

		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{

		return currenttip;
	}

}
