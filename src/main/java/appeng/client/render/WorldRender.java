package appeng.client.render;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.block.AEBaseBlock;
import appeng.core.AELog;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldRender implements ISimpleBlockRenderingHandler
{

	private final RenderBlocks renderer = new RenderBlocks();
	final int renderID = RenderingRegistry.getNextAvailableRenderId();
	public static final WorldRender instance = new WorldRender();
	boolean hasError = false;

	public final HashMap<AEBaseBlock, BaseBlockRender> blockRenders = new HashMap<AEBaseBlock, BaseBlockRender>();

	void setRender(AEBaseBlock in, BaseBlockRender r)
	{
		blockRenders.put( in, r );
	}

	private WorldRender() {
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		// wtf is this for?
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		AEBaseBlock blk = (AEBaseBlock) block;
		renderer.setRenderBoundsFromBlock( block );
		return getRender( blk ).renderInWorld( blk, world, x, y, z, renderer );
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return renderID;
	}

	public void renderItemBlock(ItemStack item, ItemRenderType type, Object[] data)
	{
		Block blk = Block.getBlockFromItem( item.getItem() );
		if ( blk instanceof AEBaseBlock )
		{
			AEBaseBlock block = (AEBaseBlock) blk;
			renderer.setRenderBoundsFromBlock( block );

			renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
			getRender( block ).renderInventory( block, item, renderer, type, data );
			renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		}
		else
		{
			if ( !hasError )
			{
				hasError = true;
				AELog.severe( "Invalid render - item/block mismatch" );
				AELog.severe( "		item: " + item.getUnlocalizedName() );
				AELog.severe( "		block: " + blk.getUnlocalizedName() );
			}
		}
	}

	private BaseBlockRender getRender(AEBaseBlock block)
	{
		return block.getRendererInstance().rendererInstance;
	}
}
