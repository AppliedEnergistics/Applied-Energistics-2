
package appeng.client;


import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseBlock;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.MissingIcon;


// net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
public class SmartModel implements IBakedModel, ISmartBlockModel, ISmartItemModel
{

	private final BlockRenderInfo aeRenderer;

	public SmartModel( final BlockRenderInfo rendererInstance )
	{
		this.aeRenderer = rendererInstance;
	}

	@Override
	public List getFaceQuads( final EnumFacing p_177551_1_ )
	{
		return Collections.emptyList();
	}

	@Override
	public List getGeneralQuads()
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.aeRenderer != null ? this.aeRenderer.getTexture( AEPartLocation.UP ).getAtlas() : MissingIcon.getMissing();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public IBakedModel handleItemState( final ItemStack stack )
	{
		final ModelGenerator helper = new ModelGenerator();
		final Block blk = Block.getBlockFromItem( stack.getItem() );
		helper.setRenderBoundsFromBlock( blk );
		this.aeRenderer.getRendererInstance().renderInventory( blk instanceof AEBaseBlock ? (AEBaseBlock) blk : null, stack, helper, ItemRenderType.INVENTORY, null );
		helper.finalizeModel( true );
		return helper.getOutput();
	}

	@Override
	public IBakedModel handleBlockState( final IBlockState state )
	{
		final ModelGenerator helper = new ModelGenerator();
		final Block blk = state.getBlock();
		final BlockPos pos = ( (IExtendedBlockState) state ).getValue( AEBaseBlock.AE_BLOCK_POS );
		final IBlockAccess world = ( (IExtendedBlockState) state ).getValue( AEBaseBlock.AE_BLOCK_ACCESS );
		helper.setTranslation( -pos.getX(), -pos.getY(), -pos.getZ() );
		helper.setRenderBoundsFromBlock( blk );
		helper.setBlockAccess( world );
		this.aeRenderer.getRendererInstance().renderInWorld( blk instanceof AEBaseBlock ? (AEBaseBlock) blk : null, world, pos, helper );
		helper.finalizeModel( false );
		return helper.getOutput();
	}

}
