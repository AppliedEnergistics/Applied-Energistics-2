package appeng.api.networking.security;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;

public interface IActionHost extends IGridHost
{

	/**
	 * Used to for calculating security rules, you must supply a node from your
	 * IGridHost for the security test, this should be the primary node for the
	 * machine, unless the action is preformed by a non primary node.
	 * 
	 * @return the the gridnode that actions from this IGridHost are preformed
	 *         by.
	 */
	IGridNode getActionableNode();

}
