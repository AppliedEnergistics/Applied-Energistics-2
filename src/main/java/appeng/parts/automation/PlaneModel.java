package appeng.parts.automation;


import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;


/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements IModel
{

	private final ResourceLocation frontTexture;
	private final ResourceLocation sidesTexture;
	private final ResourceLocation backTexture;
	private final PlaneConnections connections;

	public PlaneModel( ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture, PlaneConnections connections )
	{
		this.frontTexture = frontTexture;
		this.sidesTexture = sidesTexture;
		this.backTexture = backTexture;
		this.connections = connections;
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Lists.newArrayList( frontTexture, sidesTexture, backTexture );
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		TextureAtlasSprite frontSprite = bakedTextureGetter.apply( frontTexture );
		TextureAtlasSprite sidesSprite = bakedTextureGetter.apply( sidesTexture );
		TextureAtlasSprite backSprite = bakedTextureGetter.apply( backTexture );

		return new PlaneBakedModel( format, frontSprite, sidesSprite, backSprite, connections );
	}

	@Override
	public IModelState getDefaultState()
	{
		return TRSRTransformation.identity();
	}

}
