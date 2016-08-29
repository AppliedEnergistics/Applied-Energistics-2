package appeng.bootstrap;


import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.BlockColorComponent;
import appeng.bootstrap.components.StateMapperComponent;
import appeng.bootstrap.components.TesrComponent;
import appeng.client.render.model.AutoRotatingModel;


class BlockRendering implements IBlockRendering
{

	@SideOnly( Side.CLIENT )
	private BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> modelCustomizer;

	@SideOnly( Side.CLIENT )
	private IBlockColor blockColor;

	@SideOnly( Side.CLIENT )
	private TileEntitySpecialRenderer<?> tesr;

	@SideOnly( Side.CLIENT )
	private IStateMapper stateMapper;

	@SideOnly( Side.CLIENT )
	private Map<String, IModel> builtInModels = new HashMap<>();

	@SideOnly( Side.CLIENT )
	public IBlockRendering modelCustomizer( BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer )
	{
		modelCustomizer = customizer;
		return this;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public IBlockRendering blockColor( IBlockColor blockColor )
	{
		this.blockColor = blockColor;
		return this;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public IBlockRendering tesr( TileEntitySpecialRenderer<?> tesr )
	{
		this.tesr = tesr;
		return this;
	}

	@Override
	public IBlockRendering builtInModel( String name, IModel model )
	{
		this.builtInModels.put( name, model );
		return this;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public IBlockRendering stateMapper( IStateMapper mapper )
	{
		this.stateMapper = mapper;
		return this;
	}

	void apply( FeatureFactory factory, Block block, Class<?> tileEntityClass )
	{
		if( tesr != null )
		{
			if( tileEntityClass == null )
			{
				throw new IllegalStateException( "Tried to register a TESR for " + block + " even though no tile entity has been specified." );
			}
			factory.addBootstrapComponent( new TesrComponent( tileEntityClass, tesr ) );
		}

		if( modelCustomizer != null )
		{
			factory.modelOverrideComponent.addOverride( block.getRegistryName().getResourcePath(), modelCustomizer );
		}
		else if ( block instanceof AEBaseTileBlock )
		{
			// This is a default rotating model if the base-block uses an AE tile entity which exposes UP/FRONT as extended props
			factory.modelOverrideComponent.addOverride( block.getRegistryName().getResourcePath(), ( l, m ) -> new AutoRotatingModel( m ) );
		}

		builtInModels.forEach( factory::addBuiltInModel );

		if( blockColor != null )
		{
			factory.addBootstrapComponent( new BlockColorComponent( block, blockColor ) );
		}

		if( stateMapper != null )
		{
			factory.addBootstrapComponent( new StateMapperComponent( block, stateMapper ) );
		}
	}
}
