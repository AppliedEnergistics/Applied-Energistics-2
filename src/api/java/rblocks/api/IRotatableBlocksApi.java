package rblocks.api;

/**
 * obtain via
 * 
 * @Instance("RotateableBlocks")
 * static IRotatableBlocksApi api;
 *
 */
public interface IRotatableBlocksApi
{

	/**
	 * @return render type for rotatable blocks, not this render type only handles in world render, and will not render
	 *         anything as entities, or inventory items.
	 */
	int getRotatableRenderType();

}
