package appeng.bootstrap;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.components.ItemColorComponent;
import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemModelComponent;
import appeng.bootstrap.components.ItemVariantsComponent;


class ItemRendering implements IItemRendering
{

	@SideOnly( Side.CLIENT )
	private IItemColor itemColor;

	@SideOnly( Side.CLIENT )
	private ItemMeshDefinition itemMeshDefinition;

	@SideOnly( Side.CLIENT )
	private Map<Integer, ModelResourceLocation> itemModels = new HashMap<>();

	@SideOnly( Side.CLIENT )
	private Set<ResourceLocation> variants = new HashSet<>();

	@Override
	@SideOnly( Side.CLIENT )
	public IItemRendering meshDefinition( ItemMeshDefinition meshDefinition )
	{
		this.itemMeshDefinition = meshDefinition;
		return this;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IItemRendering model( int meta, ModelResourceLocation model )
	{
		this.itemModels.put( meta, model );
		return this;
	}

	@Override
	public IItemRendering variants( Collection<ResourceLocation> resources )
	{
		this.variants.addAll( resources );
		return this;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IItemRendering color( IItemColor itemColor )
	{
		this.itemColor = itemColor;
		return this;
	}

	void apply( FeatureFactory factory, Item item )
	{
		if( this.itemMeshDefinition != null )
		{
			factory.addBootstrapComponent( new ItemMeshDefinitionComponent( item, itemMeshDefinition ) );
		}

		if( !this.itemModels.isEmpty() )
		{
			factory.addBootstrapComponent( new ItemModelComponent( item, this.itemModels ) );
		}

		Set<ResourceLocation> resources = new HashSet<>( variants );

		// Register a default item model if neither items by meta nor an item mesh definition exist
		if( this.itemMeshDefinition == null && this.itemModels.isEmpty() )
		{
			ModelResourceLocation model;

			// For block items, the default will try to use the default state of the associated block
			if( item instanceof ItemBlock )
			{
				Block block = ( (ItemBlock) item ).getBlock();

				// We can only do this once the blocks are actually registered...
				StateMapperHelper helper = new StateMapperHelper( block.getRegistryName() );
				model = helper.getModelResourceLocation( block.getDefaultState() );
			}
			else
			{
				model = new ModelResourceLocation( item.getRegistryName(), "inventory" );
			}

			factory.addBootstrapComponent( new ItemModelComponent( item, ImmutableMap.of( 0, model ) ) );
			resources.add( model );
		}

		if( !resources.isEmpty() )
		{
			factory.addBootstrapComponent( new ItemVariantsComponent( item, resources ) );
		}

		if( itemColor != null )
		{
			factory.addBootstrapComponent( new ItemColorComponent( item, itemColor ) );
		}
	}

	private static class StateMapperHelper extends StateMapperBase
	{

		private final ResourceLocation registryName;

		public StateMapperHelper( ResourceLocation registryName )
		{
			this.registryName = registryName;
		}

		@Override
		protected ModelResourceLocation getModelResourceLocation( IBlockState state )
		{
			return new ModelResourceLocation( registryName, getPropertyString( state.getProperties() ) );
		}
	}
}
