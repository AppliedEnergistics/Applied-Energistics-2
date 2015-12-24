
package appeng.client.render;


import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;


public interface ISimpleBlockRenderingHandler
{

	void renderInventoryBlock(
			Block block,
			int metadata,
			int modelID,
			ModelGenerator renderer );

	boolean renderWorldBlock(
			IBlockAccess world,
			BlockPos pos,
			Block block,
			int modelId,
			ModelGenerator renderer );

}
