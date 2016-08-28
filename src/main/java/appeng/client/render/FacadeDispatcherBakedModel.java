package appeng.client.render;


import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

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
import net.minecraft.world.World;

import appeng.items.parts.ItemFacade;


/**
 * This baked model class is used as a dispatcher to redirect the renderer to the *real* model that should be used based on the item stack.
 * A custom Item Override List is used to accomplish this.
 */
public class FacadeDispatcherBakedModel implements IBakedModel
{

	private final IBakedModel baseModel;

	public FacadeDispatcherBakedModel( IBakedModel baseModel )
	{
		this.baseModel = baseModel;
	}

	// This is never used. See the item override list below.
	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return new ItemOverrideList( Collections.emptyList() )
		{
			@Override
			public IBakedModel handleItemState( IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity )
			{
				if( !( stack.getItem() instanceof ItemFacade ) )
				{
					return originalModel;
				}

				ItemFacade itemFacade = (ItemFacade) stack.getItem();

				Block block = itemFacade.getBlock( stack );
				int meta = itemFacade.getMeta( stack );

				// This is kinda fascinating, how do we get the meta from the itemblock
				IBlockState state = block.getStateFromMeta( meta );

				return new FacadeWithBlockBakedModel( baseModel, state );
			}
		};
	}
}
