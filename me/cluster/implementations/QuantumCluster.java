package appeng.me.cluster.implementations;

import java.util.Iterator;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.AEApi;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.iterators.ChainedIterator;

public class QuantumCluster implements ILocatable, IAECluster
{

	public WorldCoord min;
	public WorldCoord max;
	public boolean isDestroyed = false;

	boolean registered = false;
	private long thisSide;
	private long otherSide;

	public TileQuantumBridge Ring[];
	private TileQuantumBridge center;

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return new ChainedIterator<IGridHost>( Ring[0], Ring[1], Ring[2], Ring[3], Ring[4], Ring[5], Ring[6], Ring[7], center );
	}

	public void setCenter(TileQuantumBridge c)
	{
		registered = true;
		MinecraftForge.EVENT_BUS.register( this );
		center = c;
	}

	public QuantumCluster(WorldCoord _min, WorldCoord _max) {
		min = _min;
		max = _max;
		Ring = new TileQuantumBridge[8];
	}

	public boolean canUseNode(long qe)
	{
		QuantumCluster qc = (QuantumCluster) AEApi.instance().registries().locateable().findLocateableBySerial( qe );
		if ( qc.center instanceof TileQuantumBridge )
		{
			if ( !qc.isDestroyed )
			{
				Chunk c = qc.center.worldObj.getChunkFromBlockCoords( qc.center.xCoord, qc.center.zCoord );
				if ( c.isChunkLoaded )
					return false;
			}
		}
		return true;
	}

	@ForgeSubscribe
	public void onUnload(WorldEvent.Unload e)
	{
		if ( center.worldObj == e.world )
		{
			destroy();
		}
	}

	@Override
	public void updateStatus(boolean updateGrid)
	{
		long oldOtherSide = otherSide;
		long qe;

		qe = center.getQEDest();

		if ( thisSide != qe && thisSide != -qe )
		{
			if ( qe != 0 )
			{
				if ( thisSide != 0 )
					MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );

				if ( canUseNode( -qe ) )
				{
					otherSide = qe;
					thisSide = -qe;
				}
				else if ( canUseNode( qe ) )
				{
					thisSide = qe;
					otherSide = -qe;
				}

				MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Register ) );
			}
			else
			{
				MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );

				otherSide = 0;
				thisSide = 0;
			}
		}

		if ( updateGrid )
		{
			updateGrid( this );
			Object myOtherSide = otherSide == 0 ? null : AEApi.instance().registries().locateable().findLocateableBySerial( otherSide );
			Object oldMyOtherSide = oldOtherSide == 0 ? null : AEApi.instance().registries().locateable().findLocateableBySerial( oldOtherSide );
			if ( myOtherSide instanceof QuantumCluster )
				updateGrid( (QuantumCluster) myOtherSide );
			if ( oldMyOtherSide instanceof QuantumCluster )
				updateGrid( (QuantumCluster) oldMyOtherSide );
		}
	}

	public void updateRender()
	{
		for (int x = 0; x < Ring.length; x++)
		{

		}
	}

	private void updateGrid(QuantumCluster c)
	{

	}

	@Override
	public void destroy()
	{
		if ( isDestroyed )
			return;
		isDestroyed = true;

		if ( registered )
		{
			MinecraftForge.EVENT_BUS.unregister( this );
			registered = false;
		}

		if ( getLocatableSerial() != 0 )
		{
			updateGrid( this );
			Object myOtherSide = otherSide == 0 ? null : AEApi.instance().registries().locateable().findLocateableBySerial( otherSide );
			if ( myOtherSide instanceof QuantumCluster )
				updateGrid( (QuantumCluster) myOtherSide );

			MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		}

		// updateStatus( true );

		center.updateStatus( null, (byte) -1 );

		for (TileQuantumBridge r : Ring)
		{
			r.updateStatus( null, (byte) -1 );
		}

		center = null;
		Ring = new TileQuantumBridge[8];
	}

	public boolean isCorner(TileQuantumBridge tileQuantumBridge)
	{
		return Ring[0] == tileQuantumBridge || Ring[2] == tileQuantumBridge || Ring[4] == tileQuantumBridge || Ring[6] == tileQuantumBridge;

	}

	public DimensionalCoord[] getOtherSide()
	{
		Object myOtherSide = AEApi.instance().registries().locateable().findLocateableBySerial( otherSide );
		if ( myOtherSide != null )
		{
			try
			{
				QuantumCluster qc = (QuantumCluster) myOtherSide;
				DimensionalCoord[] out = new DimensionalCoord[4];
				out[0] = new DimensionalCoord( qc.Ring[1] );
				out[1] = new DimensionalCoord( qc.Ring[3] );
				out[2] = new DimensionalCoord( qc.Ring[4] );
				out[3] = new DimensionalCoord( qc.Ring[6] );
				return out;
			}
			catch (Throwable t)
			{
			}
		}
		return new DimensionalCoord[0];
	}

	@Override
	public long getLocatableSerial()
	{
		return thisSide;
	}

	public boolean hasPower()
	{
		return center.bridgePowered;
	}

	public TileQuantumBridge getCenter()
	{
		return center;
	}

	public boolean hasQES()
	{
		return getLocatableSerial() != 0;
	}

}
