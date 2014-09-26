package mekanism.api.gas;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.ListUtils;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterNetwork;
import mekanism.api.transmitters.TransmissionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * A DynamicNetwork extension created specifically for the transfer of Gasses. By default this is server-only, but if ticked on
 * the client side and if it's posted events are handled properly, it has the capability to visually display gasses network-wide.
 * @author aidancbrady
 *
 */
public class GasNetwork extends DynamicNetwork<IGasHandler, GasNetwork>
{
	public int transferDelay = 0;

	public boolean didTransfer;
	public boolean prevTransfer;

	public float gasScale;

	public Gas refGas;

	public GasStack gasStored;
	public int prevStored;

	public int prevTransferAmount = 0;

	public GasNetwork(IGridTransmitter<GasNetwork>... varPipes)
	{
		transmitters.addAll(Arrays.asList(varPipes));
		updateCapacity();
		register();
	}

	public GasNetwork(Collection<IGridTransmitter<GasNetwork>> collection)
	{
		transmitters.addAll(collection);
		updateCapacity();
		register();
	}

	public GasNetwork(Set<GasNetwork> networks)
	{
		for(GasNetwork net : networks)
		{
			if(net != null)
			{
				if(FMLCommonHandler.instance().getEffectiveSide().isClient())
				{
					if(net.refGas != null && net.gasScale > gasScale)
					{
						gasScale = net.gasScale;
						refGas = net.refGas;
						gasStored = net.gasStored;

						net.gasScale = 0;
						net.refGas = null;
						net.gasStored = null;
					}
				}
				else {
					if(net.gasStored != null)
					{
						if(gasStored == null)
						{
							gasStored = net.gasStored;
						}
						else {
							gasStored.amount += net.gasStored.amount;
						}

						net.gasStored = null;
					}
				}

				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}

		gasScale = getScale();

		updateCapacity();
		fullRefresh();
		register();
	}

	@Override
	public void onNetworksCreated(List<GasNetwork> networks)
	{
		if(gasStored != null && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			int[] caps = new int[networks.size()];
			int cap = 0;

			for(GasNetwork network : networks)
			{
				caps[networks.indexOf(network)] = network.getCapacity();
				cap += network.getCapacity();
			}

			gasStored.amount = Math.min(cap, gasStored.amount);

			int[] values = ListUtils.calcPercentInt(ListUtils.percent(caps), gasStored.amount);

			for(GasNetwork network : networks)
			{
				int index = networks.indexOf(network);

				if(values[index] > 0)
				{
					network.gasStored = new GasStack(gasStored.getGas(), values[index]);
					network.refGas = gasStored.getGas();
				}
			}
		}

		gasScale = 0;
		refGas = null;
		gasStored = null;
	}

	public synchronized int getGasNeeded()
	{
		return getCapacity()-(gasStored != null ? gasStored.amount : 0);
	}

	public synchronized int tickEmit(GasStack stack)
	{
		List availableAcceptors = Arrays.asList(getAcceptors(stack.getGas()).toArray());

		Collections.shuffle(availableAcceptors);

		int toSend = stack.amount;
		int prevSending = toSend;

		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = toSend % divider;
			int sending = (toSend-remaining)/divider;

			for(Object obj : availableAcceptors)
			{
				if(obj instanceof IGasHandler)
				{
					IGasHandler acceptor = (IGasHandler)obj;

					int currentSending = sending;

					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}

					toSend -= acceptor.receiveGas(acceptorDirections.get(acceptor).getOpposite(), new GasStack(stack.getGas(), currentSending));
				}
			}
		}

		int sent = prevSending-toSend;

		if(sent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			didTransfer = true;
			transferDelay = 2;
		}

		return sent;
	}

	public synchronized int emit(GasStack stack)
	{
		if(gasStored != null && gasStored.getGas() != stack.getGas())
		{
			return 0;
		}

		int toUse = Math.min(getGasNeeded(), stack.amount);

		if(gasStored == null)
		{
			gasStored = stack.copy();
			gasStored.amount = toUse;
		}
		else {
			gasStored.amount += toUse;
		}

		return toUse;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			prevTransferAmount = 0;

			if(transferDelay == 0)
			{
				didTransfer = false;
			}
			else {
				transferDelay--;
			}

			int stored = gasStored != null ? gasStored.amount : 0;

			if(stored != prevStored)
			{
				needsUpdate = true;
			}

			prevStored = stored;

			if(didTransfer != prevTransfer || needsUpdate)
			{
				MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, gasStored, didTransfer));
				needsUpdate = false;
			}

			prevTransfer = didTransfer;

			if(gasStored != null)
			{
				prevTransferAmount = tickEmit(gasStored);
				gasStored.amount -= prevTransferAmount;

				if(gasStored.amount <= 0)
				{
					gasStored = null;
				}
			}
		}
	}

	@Override
	public void clientTick()
	{
		super.clientTick();

		gasScale = Math.max(gasScale, getScale());

		if(didTransfer && gasScale < 1)
		{
			gasScale = Math.max(getScale(), Math.min(1, gasScale+0.02F));
		}
		else if(!didTransfer && gasScale > 0)
		{
			gasScale = Math.max(getScale(), Math.max(0, gasScale-0.02F));

			if(gasScale == 0)
			{
				gasStored = null;
			}
		}
	}

	@Override
	public synchronized Set<IGasHandler> getAcceptors(Object... data)
	{
		Gas type = (Gas)data[0];
		Set<IGasHandler> toReturn = new HashSet<IGasHandler>();

		for(IGasHandler acceptor : possibleAcceptors.values())
		{
			if(acceptorDirections.get(acceptor) == null)
			{
				continue;
			}

			if(acceptor.canReceiveGas(acceptorDirections.get(acceptor).getOpposite(), type))
			{
				toReturn.add(acceptor);
			}
		}

		return toReturn;
	}

	@Override
	public synchronized void refresh()
	{
		Set<IGridTransmitter<GasNetwork>> iterTubes = (Set<IGridTransmitter<GasNetwork>>)transmitters.clone();
		Iterator<IGridTransmitter<GasNetwork>> it = iterTubes.iterator();
		boolean networkChanged = false;

		while(it.hasNext())
		{
			IGridTransmitter<GasNetwork> conductor = (IGridTransmitter<GasNetwork>)it.next();

			if(conductor == null || conductor.getTile().isInvalid())
			{
				it.remove();
				networkChanged = true;
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}

		if(networkChanged) 
		{
			updateCapacity();
		}
	}
	
	@Override
	public synchronized void refresh(IGridTransmitter<GasNetwork> transmitter)
	{
		IGasHandler[] acceptors = GasTransmission.getConnectedAcceptors(transmitter.getTile());
		
		clearAround(transmitter);

		for(IGasHandler acceptor : acceptors)
		{
			ForgeDirection side = ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor));

			if(side != null && acceptor != null && !(acceptor instanceof IGridTransmitter) && transmitter.canConnectToAcceptor(side, true))
			{
				possibleAcceptors.put(Coord4D.get((TileEntity)acceptor), acceptor);
				acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
			}
		}
	}

	public static class GasTransferEvent extends Event
	{
		public final GasNetwork gasNetwork;

		public final GasStack transferType;
		public final boolean didTransfer;

		public GasTransferEvent(GasNetwork network, GasStack type, boolean did)
		{
			gasNetwork = network;
			transferType = type;
			didTransfer = did;
		}
	}

	public float getScale()
	{
		return Math.min(1, (gasStored == null || getCapacity() == 0 ? 0 : (float)gasStored.amount/getCapacity()));
	}

	@Override
	public String toString()
	{
		return "[GasNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	public boolean canMerge(List<ITransmitterNetwork<?, ?>> networks)
	{
		Gas found = null;

		for(ITransmitterNetwork<?, ?> network : networks)
		{
			if(network instanceof GasNetwork)
			{
				GasNetwork net = (GasNetwork)network;

				if(net.gasStored != null)
				{
					if(found != null && found != net.gasStored.getGas())
					{
						return false;
					}

					found = net.gasStored.getGas();
				}
			}
		}

		return true;
	}

	@Override
	protected GasNetwork create(IGridTransmitter<GasNetwork>... varTransmitters)
	{
		GasNetwork network = new GasNetwork(varTransmitters);
		network.refGas = refGas;

		if(gasStored != null)
		{
			if(network.gasStored == null)
			{
				network.gasStored = gasStored;
			}
			else {
				network.gasStored.amount += gasStored.amount;
			}
		}

		network.gasScale = network.getScale();
		gasScale = 0;
		refGas = null;
		gasStored = null;

		return network;
	}

	@Override
	protected GasNetwork create(Collection<IGridTransmitter<GasNetwork>> collection)
	{
		GasNetwork network = new GasNetwork(collection);
		network.refGas = refGas;

		if(gasStored != null)
		{
			if(network.gasStored == null)
			{
				network.gasStored = gasStored;
			}
			else {
				network.gasStored.amount += gasStored.amount;
			}
		}

		network.gasScale = network.getScale();

		return network;
	}

	@Override
	protected GasNetwork create(Set<GasNetwork> networks)
	{
		return new GasNetwork(networks);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public String getNeededInfo()
	{
		return Integer.toString(getGasNeeded());
	}

	@Override
	public String getStoredInfo()
	{
		return gasStored != null ? gasStored.getGas().getLocalizedName() + " (" + gasStored.amount + ")" : "None";
	}

	@Override
	public String getFlowInfo()
	{
		return Integer.toString(prevTransferAmount) + "/t";
	}
}
