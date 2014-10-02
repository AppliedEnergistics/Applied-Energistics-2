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
	public List<String> getWailaBody(ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity te = accessor.getTileEntity();
		MovingObjectPosition mop = accessor.getPosition();

		NBTTagCompound nbt = null;

		try
		{
			nbt = accessor.getNBTData();
		}
		catch (NullPointerException ignored)
		{
		}

		return getBody( itemStack, currentToolTip, accessor.getPlayer(), nbt, te, mop );
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currentToolTip, IWailaFMPAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity te = accessor.getTileEntity();
		MovingObjectPosition mop = accessor.getPosition();

		NBTTagCompound nbt = null;

		try
		{
			nbt = accessor.getNBTData();
		}
		catch (NullPointerException ignored)
		{
		}

		return getBody( itemStack, currentToolTip, accessor.getPlayer(), nbt, te, mop );
	}

	public List<String> getBody(ItemStack itemStack, List<String> currentToolTip, EntityPlayer player, NBTTagCompound nbt, TileEntity te, MovingObjectPosition mop)
	{

		Object ThingOfInterest = te;
		if ( te instanceof IPartHost )
		{
			Vec3 Pos = mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ );
			SelectedPart sp = ((IPartHost) te).selectPart( Pos );
			if ( sp.facade != null )
			{
				ThingOfInterest = sp.facade;
			}
			else if ( sp.part != null )
			{
				ThingOfInterest = sp.part;
			}
		}

		try
		{
			if ( ThingOfInterest instanceof PartCableSmart || ThingOfInterest instanceof PartDenseCable )
			{
				if ( nbt != null && nbt.hasKey( "extra:6" ) )
				{
					NBTTagCompound ic = nbt.getCompoundTag( "extra:6" );
					if ( ic != null && ic.hasKey( "usedChannels" ) )
					{
						int channels = ic.getByte( "usedChannels" );
						currentToolTip.add( channels + " " + GuiText.Of.getLocal() + " " + (ThingOfInterest instanceof PartDenseCable ? 32 : 8) + " "
								+ WailaText.Channels.getLocal() );
					}
				}
			}

			if ( ThingOfInterest instanceof TileEnergyCell )
			{
				if ( nbt != null && nbt.hasKey( "internalCurrentPower" ) )
				{
					TileEnergyCell tec = (TileEnergyCell) ThingOfInterest;
					long power = (long) (100 * nbt.getDouble( "internalCurrentPower" ));
					currentToolTip.add( WailaText.Contains + ": " + Platform.formatPowerLong( power, false ) + " / "
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
				currentToolTip.add( WailaText.Showing.getLocal() + ": " + ais.getItemStack().getDisplayName() );
			}

			if ( stack instanceof IAEFluidStack )
			{
				IAEFluidStack ais = (IAEFluidStack) stack;
				currentToolTip.add( WailaText.Showing.getLocal() + ": " + ais.getFluid().getLocalizedName( ais.getFluidStack() ) );
			}

			if ( isLocked )
				currentToolTip.add( WailaText.Locked.getLocal() );
			else
				currentToolTip.add( WailaText.Unlocked.getLocal() );
		}

		if ( ThingOfInterest instanceof TileCharger )
		{
			TileCharger tc = (TileCharger) ThingOfInterest;
			IInventory inv = tc.getInternalInventory();
			ItemStack is = inv.getStackInSlot( 0 );
			if ( is != null )
			{
				currentToolTip.add( WailaText.Contains + ": " + is.getDisplayName() );
				is.getItem().addInformation( is, player, currentToolTip, true );
			}
		}

		if ( ThingOfInterest instanceof IPowerChannelState )
		{
			IPowerChannelState pbs = (IPowerChannelState) ThingOfInterest;
			if ( pbs.isActive() && pbs.isPowered() )
				currentToolTip.add( WailaText.DeviceOnline.getLocal() );
			else if ( pbs.isPowered() )
				currentToolTip.add( WailaText.DeviceMissingChannel.getLocal() );
			else
				currentToolTip.add( WailaText.DeviceOffline.getLocal() );
		}

		return currentToolTip;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currentToolTip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currentToolTip;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currentToolTip, IWailaFMPAccessor accessor, IWailaConfigHandler config)
	{
		return currentToolTip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currentToolTip, IWailaFMPAccessor accessor, IWailaConfigHandler config)
	{
		return currentToolTip;
	}

}
