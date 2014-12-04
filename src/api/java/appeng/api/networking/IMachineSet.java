package appeng.api.networking;

import appeng.api.util.IReadOnlyCollection;

public interface IMachineSet extends IReadOnlyCollection<IGridNode>
{

	/**
	 * @return the machine class for this set.
	 */
	Class<? extends IGridHost> getMachineClass();

}
