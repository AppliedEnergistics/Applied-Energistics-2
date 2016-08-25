package appeng.bootstrap;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.AEFeature;
import appeng.core.features.ItemDefinition;
import appeng.util.Platform;


class ItemDefinitionBuilder implements IItemBuilder
{

	private final FeatureFactory factory;

	private final String registryName;

	private final Supplier<Item> itemSupplier;

	private final EnumSet<AEFeature> features = EnumSet.noneOf( AEFeature.class );

	private final List<Consumer<Item>> preInitCallbacks = new ArrayList<>();

	private final List<Consumer<Item>> initCallbacks = new ArrayList<>();

	private final List<Consumer<Item>> postInitCallbacks = new ArrayList<>();

	@SideOnly( Side.CLIENT )
	private ItemRendering itemRendering;

	private CreativeTabs creativeTab = CreativeTab.instance;

	ItemDefinitionBuilder( FeatureFactory factory, String registryName, Supplier<Item> itemSupplier )
	{
		this.factory = factory;
		this.registryName = registryName;
		this.itemSupplier = itemSupplier;
		if( Platform.isClient() )
		{
			itemRendering = new ItemRendering();
		}
	}

	@Override
	public ItemDefinitionBuilder preInit( Consumer<Item> callback )
	{
		preInitCallbacks.add( callback );
		return this;
	}

	@Override
	public ItemDefinitionBuilder init( Consumer<Item> callback )
	{
		initCallbacks.add( callback );
		return this;
	}

	@Override
	public ItemDefinitionBuilder postInit( Consumer<Item> callback )
	{
		postInitCallbacks.add( callback );
		return this;
	}

	@Override
	public IItemBuilder features( AEFeature... features )
	{
		this.features.clear();
		addFeatures( features );
		return this;
	}

	@Override
	public IItemBuilder addFeatures( AEFeature... features )
	{
		Collections.addAll( this.features, features );
		return this;
	}

	@Override
	public IItemBuilder creativeTab( CreativeTabs tab )
	{
		this.creativeTab = tab;
		return this;
	}

	@Override
	public IItemBuilder rendering( ItemRenderingCustomizer callback )
	{
		if( Platform.isClient() )
		{
			customizeForClient( callback );
		}

		return this;
	}

	@SideOnly( Side.CLIENT )
	private void customizeForClient( ItemRenderingCustomizer callback )
	{
		callback.customize( itemRendering );
	}

	public ItemDefinition build()
	{
		if( !AEConfig.instance.areFeaturesEnabled( features ) )
		{
			return new ItemDefinition( registryName, null );
		}

		Item item = itemSupplier.get();
		item.setRegistryName( AppEng.MOD_ID, registryName );

		ItemDefinition definition = new ItemDefinition( registryName, item );

		item.setUnlocalizedName( "appliedenergistics2." + registryName );
		item.setCreativeTab( creativeTab );

		// Register all extra handlers
		preInitCallbacks.forEach( consumer -> factory.addPreInit( side -> consumer.accept( item ) ) );
		initCallbacks.forEach( consumer -> factory.addInit( side -> consumer.accept( item ) ) );
		postInitCallbacks.forEach( consumer -> factory.addPostInit( side -> consumer.accept( item ) ) );

		factory.addPreInit( side -> GameRegistry.register( item ) );

		if( Platform.isClient() )
		{
			itemRendering.apply( factory, item );
		}

		return definition;
	}
}
