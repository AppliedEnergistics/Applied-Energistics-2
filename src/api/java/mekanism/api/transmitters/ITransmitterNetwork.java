package mekanism.api.transmitters;

import java.util.List;
import java.util.Set;

public interface ITransmitterNetwork<A, N extends DynamicNetwork<A, N>>
{
	public void tick();

	public int getSize();

	public int getAcceptorSize();

	public Set<A> getAcceptors(Object... data);

	public void removeTransmitter(IGridTransmitter<N> transmitter);

	public void refresh();
	
	public void fullRefresh();
	
	public void refresh(IGridTransmitter<N> transmitter);

	public void split(IGridTransmitter<N> splitPoint);

	public void fixMessedUpNetwork(IGridTransmitter<N> transmitter);

	public void register();

	public void deregister();

	public void setFixed(boolean value);

	public TransmissionType getTransmissionType();

	public boolean canMerge(List<ITransmitterNetwork<?, ?>> networks);

	public void onNetworksCreated(List<N> networks);
}
