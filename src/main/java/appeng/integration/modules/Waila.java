package appeng.integration.modules;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaFMPAccessor;
import mcp.mobius.waila.api.IWailaFMPProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayer;
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
import appeng.core.localization.GuiText;
import appeng.core.localization.WailaText;
import appeng.integration.BaseModule;
import appeng.integration.IntegrationType;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartDenseCable;
import appeng.tile.misc.TileCharger;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileEnergyCell;
import appeng.util.Platform;
import cpw.mods.fml.common.event.FMLInterModComms;

public class Waila extends BaseModule implements IWailaDataProvider, IWailaFMPProvider
{

	public static Waila instance;

	public static void register(IWailaRegistrar registrar)
	{
		Waila w = (Waila) AppEng.instance.getIntegration( IntegrationType.Waila );

		registrar.registerBodyProvider( w, AEBaseBlock.class );
		registrar.registerBodyProvider( w, "ae2_cablebus" );

		registrar.registerSyncedNBTKey( "internalCurrentPower", TileEnergyCell.class );
		registrar.registerSyncedNBTKey( "extra:6.usedChannels", TileCableBus.class );
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
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity te = accessor.getTileEntity();
		MovingObjectPosition mop = accessor.getPosition();

		NBTTagCompound nbt = null;

		try
		{
			nbt = accessor.getNBTData();
		}
		catch (NullPointerException npe)
		{
		}

		return getBody( itemStack, currenttip, accessor.getPlayer(), nbt, te, mop );
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity te = accessor.getTileEntity();
		MovingObjectPosition mop = accessor.getPosition();

		NBTTagCompound nbt = null;

		try
		{
			nbt = accessor.getNBTData();
		}
		catch (NullPointerException npe)
		{
		}

		return getBody( itemStack, currenttip, accessor.getPlayer(), nbt, te, mop );
	}

	public List<String> getBody(ItemStack itemStack, List<String> currenttip, EntityPlayer player, NBTTagCompound nbt, TileEntity te, MovingObjectPosition mop)
	{

		Object ThingOfInterest = te;
		if ( te instanceof IPartHost )
		{
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

		try
		{
			if ( ThingOfInterest instanceof PartCableSmart || ThingOfInterest instanceof PartDenseCable )
			{
				NBTTagCompound c = nbt;
				if ( c != null && c.hasKey( "extra:6" ) )
				{
					NBTTagCompound ic = c.getCompoundTag( "extra:6" );
					if ( ic != null && ic.hasKey( "usedChannels" ) )
					{
						int channels = ic.getByte( "usedChannels" );
						currenttip.add( channels + " " + GuiText.Of.getLocal() + " " + (ThingOfInterest instanceof PartDenseCable ? 32 : 8) + " "
								+ WailaText.Channels.getLocal() );
					}
				}
			}

			if ( ThingOfInterest instanceof TileEnergyCell )
			{
				NBTTagCompound c = nbt;
				if ( c != null && c.hasKey( "internalCurrentPower" ) )
				{
					TileEnergyCell tec = (TileEnergyCell) ThingOfInterest;
					long power = (long) (100 * c.getDouble( "internalCurrentPower" ));
					currenttip.add( WailaText.Contains + ": " + Platform.formatPowerLong( power, false ) + " / "
							+ Platform.formatPowerLong( (long) (100 * tec.getAEMaxPower()), false ) );
				}
			}
		}
		catch (NullPointerException ex)
		{
			// :P
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
				currenttip.add( WailaText.Showing.getLocal() + ": " + ais.getFluid().getLocalizedName( ais.getFluidStack() ) );
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
				is.getItem().addInformation( is, player, currenttip, true );
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
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{

		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{

		return currenttip;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

}
