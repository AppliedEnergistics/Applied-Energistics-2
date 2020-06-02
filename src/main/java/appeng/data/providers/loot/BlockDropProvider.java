package appeng.data.providers.loot;


import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;


public class BlockDropProvider extends AE2LootProviderBase
{
	public static final IBlocks BLOCKS = Api.INSTANCE.definitions().blocks();
	public static final IMaterials MATERIALS = Api.INSTANCE.definitions().materials();

	private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
			.put( BLOCKS.quartzOre().block(), silkTouchOreBuilder( Items.QUARTZ ) ) // FIXME replace with the material reference
			.put( BLOCKS.quartzOreCharged().block(), silkTouchOreBuilder( Items.LAPIS_LAZULI ) ) // FIXME replace with the material reference
			.build();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path outputFolder;

	public BlockDropProvider( GatherDataEvent dataEvent )
	{
		outputFolder = dataEvent.getGenerator().getOutputFolder();
	}

	@Override
	public void act( @Nonnull DirectoryCache cache ) throws IOException
	{
		for( Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries() )
		{
			LootTable.Builder builder;
			if( entry.getKey().getNamespace().equals( AppEng.MOD_ID ) )
			{
				builder = overrides.getOrDefault( entry.getValue(), this::defaultBuilder )
						.apply( entry.getValue() );

				IDataProvider.save( GSON, cache, toJson( builder ), getPath( outputFolder, entry.getKey() ) );
			}
		}
	}

	private LootTable.Builder defaultBuilder( Block block )
	{
		LootEntry.Builder<?> entry = ItemLootEntry.builder( block );
		LootPool.Builder pool = LootPool.builder()
				.name( "main" )
				.rolls( ConstantRange.of( 1 ) )
				.addEntry( entry )
				.acceptCondition( SurvivesExplosion.builder() );

		return LootTable.builder()
				.addLootPool( pool );
	}

	private Function<Block, LootTable.Builder> silkTouchOreBuilder( Item drop )
	{
		return block -> {
			LootEntry.Builder<?> dropEntry = ItemLootEntry.builder( drop );
			LootPool.Builder pool = LootPool.builder()
					.name( "main" )
					.acceptFunction( () -> ( stack, lootContext ) -> {
						stack = stack.copy();
						int fortune = lootContext.getLootingModifier();
						Random rand = lootContext.getRandom();
						if( fortune > 0 )
						{
							int j = rand.nextInt( fortune + 2 ) - 1;
							if( j < 0 )
							{
								j = 0;
							}
							stack.setCount( 1 + rand.nextInt( 2 ) * ( j + 1 ) );
						}
						else
						{
							stack.setCount( 1 + rand.nextInt( 2 ) );
						}
						return stack;
					} )
					.rolls( ConstantRange.of( 1 ) )
					.addEntry( dropEntry );

			return LootTable.builder()
					.addLootPool( pool );
		};
	}

	@Nonnull
	@Override
	protected LootParameterSet getParameterSetTarget()
	{
		return LootParameterSets.BLOCK;
	}

	@Nonnull
	@Override
	protected String getDataPath()
	{
		return "loot_tables/blocks";
	}

}
