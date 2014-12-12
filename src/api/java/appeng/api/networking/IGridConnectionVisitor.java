package appeng.api.networking;

public interface IGridConnectionVisitor extends IGridVisitor
{

	/**
	 * Called for each connection on the network.
	 * 
	 * @param n
	 *            the connection.
	 */
	public void visitConnection(IGridConnection n);

}
