package mekanism.api.transmitters;

import mekanism.api.Coord4D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IGridTransmitter<N extends DynamicNetwork<?, N>> extends ITransmitter
{
	/**
	 * Gets the network currently in use by this transmitter segment.
	 * @return network this transmitter is using
	 */
	public N getTransmitterNetwork();

	/**
	 * Gets the network currently in use by this transmitter segment.
	 * @param createIfNull - If true, the transmitter will try and connect to an
	 * adjacent network, merging several if necessary, or creating a new one
	 * if none is available
	 * @return network this transmitter is using
	 */
	public N getTransmitterNetwork(boolean createIfNull);

	/**
	 * Sets this transmitter segment's network to a new value.
	 * @param network - network to set to
	 */
	public void setTransmitterNetwork(N network);

	/**
	 * Refreshes the transmitter's network.
	 */
	public void refreshTransmitterNetwork();

	/**
	 * Called when the chunk this transmitter is in is loaded.
	 */
	public void chunkLoad();

	/**
	 * Remove this transmitter from its network.
	 */
	public void removeFromTransmitterNetwork();

	public boolean canConnectToAcceptor(ForgeDirection side, boolean ignoreActive);

	/**
	 * Call this if you're worried a transmitter's network is messed up and you want
	 * it to try and fix itself.
	 */
	public void fixTransmitterNetwork();

	public boolean areTransmitterNetworksEqual(TileEntity tileEntity);

	public int getTransmitterNetworkSize();

	public int getTransmitterNetworkAcceptorSize();

	public String getTransmitterNetworkNeeded();

	public String getTransmitterNetworkFlow();

	public int getCapacity();
	
	public TileEntity getTile();
}
