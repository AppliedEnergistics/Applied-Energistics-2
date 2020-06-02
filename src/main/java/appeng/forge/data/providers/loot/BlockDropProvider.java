package appeng.forge.data.providers.loot;


import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.loot.BlockLootTables;
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
import java.util.function.Function;


public class BlockDropProvider extends BlockLootTables implements IAE2DataProvider
{
	private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
			.put( BLOCKS.matrixFrame().block(), $ -> LootTable.builder() )
			.put( BLOCKS.quartzOre().block(), b -> droppingItemWithFortune( b, Items.QUARTZ ) ) // FIXME replace with the material reference
			.put( BLOCKS.quartzOreCharged().block(), b -> droppingItemWithFortune( b, Items.LAPIS_LAZULI ) ) // FIXME replace with the material reference
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

	private Path getPath( Path root, ResourceLocation id )
	{
		return root.resolve( "data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json" );
	}

	public JsonElement toJson( LootTable.Builder builder )
	{
		return LootTableManager.toJson( finishBuilding( builder ) );
	}

	@Nonnull
	public LootTable finishBuilding( LootTable.Builder builder )
	{
		return builder.setParameterSet( LootParameterSets.BLOCK ).build();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + " Block Drops";
	}

}
