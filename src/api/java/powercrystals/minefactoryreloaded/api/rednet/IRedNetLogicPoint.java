package powercrystals.minefactoryreloaded.api.rednet;

/**
 * 
 * @author skyboy
 */
public interface IRedNetLogicPoint
{
	/**
	 * 
	 * @param out
	 * @return
	 */
	public void transformOutput(int[] out);

	/**
	 * 
	 * @param out
	 * @return
	 */
	public void transformOutput(int out);

	/**
	 * 
	 * @param in
	 * @return
	 */
	public int[] transformInput(int[] in);

	/**
	 * 
	 * @param in
	 * @return
	 */
	public int transformInput(int in);
}
