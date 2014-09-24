package appeng.me;

import java.util.HashSet;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

public class MachineSet extends HashSet<IGridNode> implements IMachineSet
{

	private static final long serialVersionUID = 3224660708327386933L;

	private final Class<? extends IGridHost> machine;

	MachineSet(Class<? extends IGridHost> m) {
		machine = m;
	}

	@Override
	public Class<? extends IGridHost> getMachineClass()
	{
		return machine;
	}

}
