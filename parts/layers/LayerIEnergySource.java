package appeng.api.parts.layers;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerBase;
import appeng.api.parts.LayerFlags;
import appeng.util.Platform;

public class LayerIEnergySource extends LayerBase implements IEnergySource
{

	private boolean isInIC2()
	{
		return getLayerFlags().contains( LayerFlags.IC2_ENET );
	}

	private TileEntity getEnergySourceTile()
	{
		IPartHost host = (IPartHost) this;
		return host.getTile();
	}

	private World getEnergySourceWorld()
	{
		if ( getEnergySourceTile() == null )
			return null;
		return getEnergySourceTile().getWorldObj();
	}

	final private void addToENet()
	{
		if ( getEnergySourceWorld() == null )
			return;

		// re-add
		removeFromENet();

		if ( !isInIC2() && Platform.isServer() )
		{
			getLayerFlags().add( LayerFlags.IC2_ENET );
			MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileLoadEvent( (IEnergySink) getEnergySourceTile() ) );
		}
	}

	final private void removeFromENet()
	{
		if ( getEnergySourceWorld() == null )
			return;

		if ( isInIC2() && Platform.isServer() )
		{
			getLayerFlags().remove( LayerFlags.IC2_ENET );
			MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileUnloadEvent( (IEnergySink) getEnergySourceTile() ) );
		}
	}

	final private boolean interestedInIC2()
	{
		int interested = 0;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart part = getPart( dir );
			if ( part instanceof IEnergyTile )
			{
				interested++;
			}
		}
		return interested == 1;// if more then one tile is interested we need to abandonship...
	}

	@Override
	public void partChanged()
	{
		super.partChanged();

		if ( interestedInIC2() )
			addToENet();
		else
			removeFromENet();
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		if ( !isInIC2() )
			return false;

		IPart part = getPart( direction );
		if ( part instanceof IEnergySink )
			return ((IEnergyEmitter) part).emitsEnergyTo( receiver, direction );
		return false;
	}

	@Override
	public double getOfferedEnergy()
	{
		if ( !isInIC2() )
			return 0;

		// this is a flawed implementation, that requires a change to the IC2 API.

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart part = getPart( dir );
			if ( part instanceof IEnergySource )
			{
				// use lower number cause ic2 deletes power it sends that isn't recieved.
				return ((IEnergySource) part).getOfferedEnergy();
			}
		}

		return 0;
	}

	@Override
	public void drawEnergy(double amount)
	{
		// this is a flawed implementation, that requires a change to the IC2 API.

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart part = getPart( dir );
			if ( part instanceof IEnergySource )
			{
				((IEnergySource) part).drawEnergy( amount );
				return;
			}
		}
	}

}
