package mekanism.api.transmitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IClientTicker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class DynamicNetwork<A, N extends DynamicNetwork<A, N>> implements ITransmitterNetwork<A, N>, IClientTicker, INetworkDataHandler
{
	public LinkedHashSet<IGridTransmitter<N>> transmitters = new LinkedHashSet<IGridTransmitter<N>>();

	public HashMap<Coord4D, A> possibleAcceptors = new HashMap<Coord4D, A>();
	public HashMap<A, ForgeDirection> acceptorDirections = new HashMap<A, ForgeDirection>();

	private List<DelayQueue> updateQueue = new ArrayList<DelayQueue>();
	
	protected AxisAlignedBB packetRange = null;

	protected int ticksSinceCreate = 0;
	
	protected int capacity = 0;
	protected double meanCapacity = 0;
	
	protected boolean fixed = false;

	protected boolean needsUpdate = false;

	protected abstract ITransmitterNetwork<A, N> create(IGridTransmitter<N>... varTransmitters);

	protected abstract ITransmitterNetwork<A, N> create(Collection<IGridTransmitter<N>> collection);

	protected abstract ITransmitterNetwork<A, N> create(Set<N> networks);
	
	protected void clearAround(IGridTransmitter<N> transmitter)
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D coord = Coord4D.get(transmitter.getTile()).getFromSide(side);
			possibleAcceptors.remove(coord);
			acceptorDirections.remove(coord.getTileEntity(transmitter.getTile().getWorldObj()));
		}
	}

	public void addAllTransmitters(Set<IGridTransmitter<N>> newTransmitters)
	{
		transmitters.addAll(newTransmitters);
		updateCapacity();
	}

	public boolean isFirst(IGridTransmitter<N> transmitter)
	{
		return transmitters.iterator().next().equals(transmitter);
	}
	
	@Override
	public void fullRefresh()
	{
		possibleAcceptors.clear();
		acceptorDirections.clear();
		
		for(IGridTransmitter<N> transmitter : transmitters)
		{
			refresh(transmitter);
		}
		
		refresh();
	}
	
	public AxisAlignedBB getPacketRange()
	{
		if(packetRange == null)
		{
			return genPacketRange();
		}
		
		return packetRange;
	}
	
	public int getDimension()
	{
		if(getSize() == 0)
		{
			return 0;
		}
		
		return transmitters.iterator().next().getTile().getWorldObj().provider.dimensionId;
	}
	
	protected AxisAlignedBB genPacketRange()
	{
		if(getSize() == 0)
		{
			deregister();
			return null;
		}
		
		Coord4D initCoord = Coord4D.get(transmitters.iterator().next().getTile());
		
		int minX = initCoord.xCoord;
		int minY = initCoord.yCoord;
		int minZ = initCoord.zCoord;
		int maxX = initCoord.xCoord;
		int maxY = initCoord.yCoord;
		int maxZ = initCoord.zCoord;
		
		for(IGridTransmitter transmitter : transmitters)
		{
			Coord4D coord = Coord4D.get(transmitter.getTile());
			
			if(coord.xCoord < minX) minX = coord.xCoord;
			if(coord.yCoord < minY) minY = coord.yCoord;
			if(coord.zCoord < minZ) minZ = coord.zCoord;
			if(coord.xCoord > maxX) maxX = coord.xCoord;
			if(coord.yCoord > maxY) maxY = coord.yCoord;
			if(coord.zCoord > maxZ) maxZ = coord.zCoord;
		}
		
		minX -= 40;
		minY -= 40;
		minZ -= 40;
		maxX += 40;
		maxY += 40;
		maxZ += 40;
		
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void removeTransmitter(IGridTransmitter<N> transmitter)
	{
		transmitters.remove(transmitter);
		updateCapacity();
		
		if(transmitters.size() == 0)
		{
			deregister();
		}
	}

	@Override
	public void register()
	{
		try {
			IGridTransmitter<N> aTransmitter = transmitters.iterator().next();

			if(aTransmitter instanceof TileEntity)
			{
				if(!((TileEntity)aTransmitter).getWorldObj().isRemote)
				{
					TransmitterNetworkRegistry.getInstance().registerNetwork(this);
				}
				else {
					MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte)1));
				}
			}
		} catch(NoSuchElementException e) {}
	}

	@Override
	public void deregister()
	{
		transmitters.clear();

		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			TransmitterNetworkRegistry.getInstance().removeNetwork(this);
		}
		else {
			MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte)0));
		}
	}

	@Override
	public int getSize()
	{
		return transmitters.size();
	}

	@Override
	public int getAcceptorSize()
	{
		return possibleAcceptors.size();
	}

	public synchronized void updateCapacity() {
		updateMeanCapacity();
		capacity = (int)meanCapacity * transmitters.size();
	}

    /**
     * Override this if things can have variable capacity along the network.
     * @return An 'average' value of capacity. Calculate it how you will.
     */
	protected synchronized void updateMeanCapacity() 
	{
		if(transmitters.size() > 0) 
		{
			meanCapacity = transmitters.iterator().next().getCapacity();
		} 
		else {
			meanCapacity = 0;
		}
	}
	
    public int getCapacity()
    {
    	return capacity;
    }

    public double getMeanCapacity()
    {
    	return meanCapacity;
    }
	
	@Override
	public void tick()
	{
		boolean didFix = false;

		if(!fixed)
		{
			ticksSinceCreate++;

			if(transmitters.size() == 0)
			{
				deregister();
				return;
			}

			if(ticksSinceCreate > 1200)
			{
				ticksSinceCreate = 0;
				fixMessedUpNetwork(transmitters.iterator().next());
				didFix = true;
			}
		}

		if(!didFix)
		{
			onUpdate();
		}
	}

	public void onUpdate()
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			Iterator<DelayQueue> i = updateQueue.iterator();

			try {
				while(i.hasNext())
				{
					DelayQueue q = i.next();

					if(q.delay > 0)
					{
						q.delay--;
					}
					else {
						needsUpdate = true;
						i.remove();
					}
				}
			} catch(Exception e) {}
		}
	}

	@Override
	public synchronized void fixMessedUpNetwork(IGridTransmitter<N> transmitter)
	{
		if(transmitter instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)transmitter).getWorldObj(), getTransmissionType(), Coord4D.get((TileEntity)transmitter));
			List<Coord4D> partNetwork = finder.exploreNetwork();
			Set<IGridTransmitter<N>> newTransporters = new HashSet<IGridTransmitter<N>>();

			for(Coord4D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)transmitter).getWorldObj());

				if(TransmissionType.checkTransmissionType(nodeTile, getTransmissionType(), (TileEntity)transmitter))
				{
					((IGridTransmitter<N>)nodeTile).removeFromTransmitterNetwork();
					newTransporters.add((IGridTransmitter<N>)nodeTile);
				}
			}

			ITransmitterNetwork<A, N> newNetwork = create(newTransporters);
			newNetwork.fullRefresh();
			newNetwork.setFixed(true);
			deregister();
		}
	}

	@Override
	public synchronized void split(IGridTransmitter<N> splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			removeTransmitter(splitPoint);

			TileEntity[] connectedBlocks = new TileEntity[6];
			boolean[] dealtWith = {false, false, false, false, false, false};
			List<ITransmitterNetwork<A, N>> newNetworks = new ArrayList<ITransmitterNetwork<A, N>>();

			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity sideTile = Coord4D.get((TileEntity)splitPoint).getFromSide(side).getTileEntity(((TileEntity)splitPoint).getWorldObj());

				if(sideTile != null)
				{
					connectedBlocks[side.ordinal()] = sideTile;
				}
			}

			for(int count = 0; count < connectedBlocks.length; count++)
			{
				TileEntity connectedBlockA = connectedBlocks[count];

				if(TransmissionType.checkTransmissionType(connectedBlockA, getTransmissionType()) && !dealtWith[count])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).getWorldObj(), getTransmissionType(), Coord4D.get(connectedBlockA), Coord4D.get((TileEntity)splitPoint));
					List<Coord4D> partNetwork = finder.exploreNetwork();

					for(int check = count; check < connectedBlocks.length; check++)
					{
						if(check == count)
						{
							continue;
						}

						TileEntity connectedBlockB = connectedBlocks[check];

						if(TransmissionType.checkTransmissionType(connectedBlockB, getTransmissionType()) && !dealtWith[check])
						{
							if(partNetwork.contains(Coord4D.get(connectedBlockB)))
							{
								dealtWith[check] = true;
							}
						}
					}

					Set<IGridTransmitter<N>> newNetCables = new HashSet<IGridTransmitter<N>>();

					for(Coord4D node : finder.iterated)
					{
						TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).getWorldObj());

						if(TransmissionType.checkTransmissionType(nodeTile, getTransmissionType()))
						{
							if(nodeTile != splitPoint)
							{
								newNetCables.add((IGridTransmitter<N>)nodeTile);
							}
						}
					}

					newNetworks.add(create(newNetCables));
				}
			}

			if(newNetworks.size() > 0)
			{
				onNetworksCreated((List)newNetworks);

				for(ITransmitterNetwork<A, N> network : newNetworks)
				{
					network.fullRefresh();
				}
			}

			deregister();
		}
	}

	@Override
	public void onNetworksCreated(List<N> networks) {}

	@Override
	public void setFixed(boolean value)
	{
		fixed = value;
	}

	@Override
	public boolean needsTicks()
	{
		return getSize() > 0;
	}

	@Override
	public void clientTick()
	{
		ticksSinceCreate++;

		if(ticksSinceCreate == 5 && getSize() > 0)
		{
			TileEntity tile = (TileEntity)transmitters.iterator().next();
			MinecraftForge.EVENT_BUS.post(new NetworkClientRequest(tile));
		}
	}

	@Override
	public boolean canMerge(List<ITransmitterNetwork<?, ?>> networks)
	{
		return true;
	}

	public static class ClientTickUpdate extends Event
	{
		public DynamicNetwork network;
		public byte operation; /*0 remove, 1 add*/

		public ClientTickUpdate(DynamicNetwork net, byte b)
		{
			network = net;
			operation = b;
		}
	}

	public static class NetworkClientRequest extends Event
	{
		public TileEntity tileEntity;

		public NetworkClientRequest(TileEntity tile)
		{
			tileEntity = tile;
		}
	}

	public void addUpdate(EntityPlayer player)
	{
		updateQueue.add(new DelayQueue(player));
	}

	public static class NetworkFinder
	{
		public TransmissionType transmissionType;

		public World worldObj;
		public Coord4D start;

		public List<Coord4D> iterated = new ArrayList<Coord4D>();
		public List<Coord4D> toIgnore = new ArrayList<Coord4D>();

		public NetworkFinder(World world, TransmissionType type, Coord4D location, Coord4D... ignore)
		{
			worldObj = world;
			start = location;

			transmissionType = type;

			if(ignore != null)
			{
				for(int i = 0; i < ignore.length; i++)
				{
					toIgnore.add(ignore[i]);
				}
			}
		}

		public void loopAll(Coord4D location)
		{
			if(TransmissionType.checkTransmissionType(location.getTileEntity(worldObj), transmissionType))
			{
				iterated.add(location);
			}
			else {
				toIgnore.add(location);
			}

			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Coord4D obj = location.getFromSide(direction);

				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = obj.getTileEntity(worldObj);

					if(!(tileEntity instanceof IBlockableConnection) || ((IBlockableConnection)tileEntity).canConnectMutual(direction.getOpposite()))
					{
						if(TransmissionType.checkTransmissionType(tileEntity, transmissionType, location.getTileEntity(worldObj)))
						{
							loopAll(obj);
						}
					}
				}
			}
		}

		public List<Coord4D> exploreNetwork()
		{
			loopAll(start);

			return iterated;
		}
	}

	public static class DelayQueue
	{
		public EntityPlayer player;
		public int delay;

		public DelayQueue(EntityPlayer p)
		{
			player = p;
			delay = 5;
		}
	}
}
