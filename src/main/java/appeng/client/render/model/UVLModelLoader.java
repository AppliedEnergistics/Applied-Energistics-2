
package appeng.client.render.model;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;


public enum UVLModelLoader implements ICustomModelLoader
{
	INSTANCE;

	private static final Gson gson = new Gson();

	private static final Constructor<? extends IModel> vanillaModelWrapper;
	private static final Field faceBakery;

	static
	{
		try
		{
			Field modifiers = Field.class.getDeclaredField( "modifiers" );
			modifiers.setAccessible( true );

			faceBakery = ReflectionHelper.findField( ModelBakery.class, "faceBakery", "field_177607_l" );
			modifiers.set( faceBakery, faceBakery.getModifiers() & ( ~Modifier.FINAL ) );

			Class clas = Class.forName( ModelLoader.class.getName() + "$VanillaModelWrapper" );
			vanillaModelWrapper = clas.getDeclaredConstructor( ModelLoader.class, ResourceLocation.class, ModelBlock.class, boolean.class, ModelBlockAnimation.class );
			vanillaModelWrapper.setAccessible( true );
		}
		catch( Exception e )
		{
			throw Throwables.propagate( e );
		}
	}

	private static Object deserializer( Class clas )
	{
		try
		{
			clas = Class.forName( clas.getName() + "$Deserializer" );
			Constructor constr = clas.getDeclaredConstructor();
			constr.setAccessible( true );
			return constr.newInstance();
		}
		catch( Exception e )
		{
			throw Throwables.propagate( e );
		}
	}

	private static <M extends IModel> M vanillaModelWrapper( ModelLoader loader, ResourceLocation location, ModelBlock model, boolean uvlock, ModelBlockAnimation animation )
	{
		try
		{
			return (M) vanillaModelWrapper.newInstance( loader, location, model, uvlock, animation );
		}
		catch( Exception e )
		{
			throw Throwables.propagate( e );
		}
	}

	private static void setFaceBakery( ModelBakery modelBakery, FaceBakery faceBakery )
	{
		try
		{
			UVLModelLoader.faceBakery.set( modelBakery, faceBakery );
		}
		catch( Exception e )
		{
			throw Throwables.propagate( e );
		}
	}

	private IResourceManager resourceManager;
	private ModelLoader loader;

	public ModelLoader getLoader()
	{
		return loader;
	}

	public void setLoader( ModelLoader loader )
	{
		this.loader = loader;
	}

	@Override
	public void onResourceManagerReload( IResourceManager resourceManager )
	{
		this.resourceManager = resourceManager;
	}

	@Override
	public boolean accepts( ResourceLocation modelLocation )
	{
		String modelPath = modelLocation.getResourcePath();
		if( modelLocation.getResourcePath().startsWith( "models/" ) )
		{
			modelPath = modelPath.substring( "models/".length() );
		}
		try( InputStreamReader io = new InputStreamReader( Minecraft.getMinecraft().getResourceManager().getResource( new ResourceLocation( modelLocation.getResourceDomain(), "models/" + modelPath + ".json" ) ).getInputStream() ) )
		{
			return gson.fromJson( io, UVLMarker.class ).uvlMarker;
		}
		catch( IOException e )
		{

		}
		return false;
	}

	@Override
	public IModel loadModel( ResourceLocation modelLocation ) throws Exception
	{
		return new UVLModelWrapper( modelLocation );
	}

	public class UVLModelWrapper implements IModel
	{
		final Gson UVLSERIALIZER = ( new GsonBuilder() ).registerTypeAdapter( ModelBlock.class, deserializer( ModelBlock.class ) ).registerTypeAdapter( BlockPart.class, deserializer( BlockPart.class ) ).registerTypeAdapter( BlockPartFace.class, new BlockPartFaceOverrideSerializer() ).registerTypeAdapter( BlockFaceUV.class, deserializer( BlockFaceUV.class ) ).registerTypeAdapter( ItemTransformVec3f.class, deserializer( ItemTransformVec3f.class ) ).registerTypeAdapter( ItemCameraTransforms.class, deserializer( ItemCameraTransforms.class ) ).registerTypeAdapter( ItemOverride.class, deserializer( ItemOverride.class ) ).create();

		private Map<BlockPartFace, Pair<Float, Float>> uvlightmap = new HashMap<>();

		private final IModel parent;

		public UVLModelWrapper( ResourceLocation modelLocation )
		{
			String modelPath = modelLocation.getResourcePath();
			if( modelLocation.getResourcePath().startsWith( "models/" ) )
			{
				modelPath = modelPath.substring( "models/".length() );
			}
			ResourceLocation armatureLocation = new ResourceLocation( modelLocation.getResourceDomain(), "armatures/" + modelPath + ".json" );
			ModelBlockAnimation animation = ModelBlockAnimation.loadVanillaAnimation( resourceManager, armatureLocation );
			ModelBlock model;
			{
				Reader reader = null;
				IResource iresource = null;
				ModelBlock lvt_5_1_ = null;

				try
				{
					String s = modelLocation.getResourcePath();

					iresource = Minecraft.getMinecraft().getResourceManager().getResource( new ResourceLocation( modelLocation.getResourceDomain(), "models/" + modelPath + ".json" ) );
					reader = new InputStreamReader( iresource.getInputStream(), Charsets.UTF_8 );

					lvt_5_1_ = JsonUtils.gsonDeserialize( UVLSERIALIZER, reader, ModelBlock.class, false );
					lvt_5_1_.name = modelLocation.toString();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				finally
				{
					IOUtils.closeQuietly( reader );
					IOUtils.closeQuietly( (Closeable) iresource );
				}

				model = lvt_5_1_;
			}

			this.parent = vanillaModelWrapper( getLoader(), modelLocation, model, false, animation );
		}

		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return parent.getDependencies();
		}

		@Override
		public Collection<ResourceLocation> getTextures()
		{
			return parent.getTextures();
		}

		@Override
		public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
		{
			setFaceBakery( getLoader(), new FaceBakeryOverride() );
			IBakedModel model = parent.bake( state, format, bakedTextureGetter );
			setFaceBakery( getLoader(), new FaceBakery() );
			return model;
		}

		@Override
		public IModelState getDefaultState()
		{
			return parent.getDefaultState();
		}

		public class BlockPartFaceOverrideSerializer implements JsonDeserializer<BlockPartFace>
		{
			public BlockPartFace deserialize( JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_ ) throws JsonParseException
			{
				JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
				EnumFacing enumfacing = this.parseCullFace( jsonobject );
				int i = this.parseTintIndex( jsonobject );
				String s = this.parseTexture( jsonobject );
				BlockFaceUV blockfaceuv = (BlockFaceUV) p_deserialize_3_.deserialize( jsonobject, BlockFaceUV.class );
				BlockPartFace blockFace = new BlockPartFace( enumfacing, i, s, blockfaceuv );
				uvlightmap.put( blockFace, parseUVL( jsonobject ) );
				return blockFace;
			}

			protected int parseTintIndex( JsonObject object )
			{
				return JsonUtils.getInt( object, "tintindex", -1 );
			}

			private String parseTexture( JsonObject object )
			{
				return JsonUtils.getString( object, "texture" );
			}

			@Nullable
			private EnumFacing parseCullFace( JsonObject object )
			{
				String s = JsonUtils.getString( object, "cullface", "" );
				return EnumFacing.byName( s );
			}

			protected Pair<Float, Float> parseUVL( JsonObject object )
			{
				if( !object.has( "uvlightmap" ) )
				{
					return null;
				}
				object = object.get( "uvlightmap" ).getAsJsonObject();
				return new ImmutablePair<Float, Float>( JsonUtils.getFloat( object, "sky", 0 ), JsonUtils.getFloat( object, "block", 0 ) );
			}
		}

		public class FaceBakeryOverride extends FaceBakery
		{

			@Override
			public BakedQuad makeBakedQuad( Vector3f posFrom, Vector3f posTo, BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, ITransformation modelRotationIn, BlockPartRotation partRotation, boolean uvLocked, boolean shade )
			{
				BakedQuad quad = super.makeBakedQuad( posFrom, posTo, face, sprite, facing, modelRotationIn, partRotation, uvLocked, shade );

				Pair<Float, Float> brightness = uvlightmap.get( face );
				if( brightness != null )
				{
					VertexFormat format = new VertexFormat( quad.getFormat() );
					if( !format.getElements().contains( DefaultVertexFormats.TEX_2S ) )
					{
						format.addElement( DefaultVertexFormats.TEX_2S );
					}
					UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder( format );
					VertexLighterFlat trans = new VertexLighterFlat( Minecraft.getMinecraft().getBlockColors() ){

						@Override
						protected void updateLightmap( float[] normal, float[] lightmap, float x, float y, float z )
						{
							lightmap[0] = brightness.getRight();
							lightmap[1] = brightness.getLeft();
						}

						@Override
						public void setQuadTint( int tint )
						{
							// Tint requires a block state which we don't have at this point
						}
					};
					trans.setParent( builder );
					quad.pipe( trans );
					builder.setQuadTint( quad.getTintIndex() );
					builder.setQuadOrientation( quad.getFace() );
					return builder.build();
				}
				else
				{
					return quad;
				}
			}

		}

	}

	class UVLMarker
	{
		boolean uvlMarker = false;
	}

}