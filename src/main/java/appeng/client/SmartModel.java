
package appeng.client;


import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.api.util.AEPartLocation;
import appeng.api.util.ModelGenerator;
import appeng.block.AEBaseBlock;
import appeng.client.render.BakingModelGenerator;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.MissingIcon;


// net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
public class SmartModel implements IBakedModel
{

	private final BlockRenderInfo aeRenderer;

	public SmartModel( final BlockRenderInfo rendererInstance )
	{
		this.aeRenderer = rendererInstance;
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
	public List<BakedQuad> getQuads( IBlockState state, EnumFacing side, long rand )
	{
		final ModelGenerator helper = new BakingModelGenerator();
		final Block blk = state.getBlock();
		final BlockPos pos = ( (IExtendedBlockState) state ).getValue( AEBaseBlock.AE_BLOCK_POS );
		final IBlockAccess world = ( (IExtendedBlockState) state ).getValue( AEBaseBlock.AE_BLOCK_ACCESS );
		helper.setTranslation( -pos.getX(), -pos.getY(), -pos.getZ() );
		helper.setRenderBoundsFromBlock( state, pos );
		helper.setBlockAccess( world );
		this.aeRenderer.getRendererInstance().renderInWorld( blk instanceof AEBaseBlock ? (AEBaseBlock) blk : null, world, pos, helper );
		helper.finalizeModel( false );
		return helper.getOutput().getQuads( state, side, rand );
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return new ItemOverrideList( Lists.newArrayList() ){

			@Override
			public IBakedModel handleItemState( IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity )
			{
				final ModelGenerator helper = new BakingModelGenerator();
				final Block blk = Block.getBlockFromItem( stack.getItem() );
				helper.setRenderBoundsFromBlock( blk.getDefaultState(), null );
				aeRenderer.getRendererInstance().renderInventory( blk instanceof AEBaseBlock ? (AEBaseBlock) blk : null, stack, helper, ItemRenderType.INVENTORY, null );
				helper.finalizeModel( true );
				return helper.getOutput();
			}

		};
	}

}
