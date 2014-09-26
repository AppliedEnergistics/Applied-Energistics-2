package powercrystals.minefactoryreloaded.api.rednet;

/**
 * Defines a Block that can connect to RedNet cables. This must be implemented on your Block class.
 * <p>
 * Note that when you implement this, the RedNet network makes several assumptions about your code -
 * It will not clamp values to 0 <= x <= 15. This means you must be able to accept any possible integer
 * without crashing, even negatives. It will also assume that calling the onInput(s)Changed() methods
 * are sufficient, and will not issue block updates. It will never call the vanilla redstone output
 * methods, and will only query the methods contained in this interface.
 * <p>
 * RedNet cables have their subnets indicated to the user by colored bands on the cable.
 * The color of a given subnet is the same as the wool with metadata equal to the subnet number.
 * <p>
 * For reference:<br>
 * 0:White, 1:Orange, 2:Magenta, 3:LightBlue, 4:Yellow, 5:Lime, 6:Pink, 7:Gray,
 * 8:LightGray, 9:Cyan, 10:Purple, 11:Blue, 12:Brown, 13:Green, 14:Red, 15:Black
 */
public interface IRedNetOmniNode extends IRedNetInputNode, IRedNetOutputNode
{
	// this is merely provided for convenience
}
