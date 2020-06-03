package appeng.recipes;


import appeng.core.AppEng;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;


public class AE2Tags
{

	public static class Items
	{

		public static final Tag<Item> CRYSTAL_PURE = tag("crystal_pure");
		public static final Tag<Item> CRYSTAL_PURE_CERTUS_QUARTZ = tag("crystal_pure/certus_quartz");
		public static final Tag<Item> CRYSTAL_PURE_NETHER_QUARTZ = tag("crystal_pure/nether_quartz");
		public static final Tag<Item> CRYSTAL_PURE_FLUIX = tag("crystal_pure/fluix");

		public static final Tag<Item> DUSTS_ENDER = forgeTag( "dusts/ender" );
		public static final Tag<Item> DUSTS_ENDER_PEARL = forgeTag( "dusts/ender_pearl" );
		public static final Tag<Item> DUSTS_WHEAT = forgeTag( "dusts/wheat" );
		public static final Tag<Item> DUSTS_GOLD = forgeTag( "dusts/gold" );
		public static final Tag<Item> DUSTS_IRON = forgeTag( "dusts/iron" );
		public static final Tag<Item> DUSTS_FLUIX = forgeTag( "dusts/fluix" );
		public static final Tag<Item> DUSTS_NETHER_QUARTZ = forgeTag( "dusts/nether_quartz" );
		public static final Tag<Item> DUSTS_QUARTZ = forgeTag( "dusts/quartz" );
		public static final Tag<Item> DUSTS_CERTUS_QUARTZ = forgeTag( "dusts/certus_quartz" );

		public static final Tag<Item> GEMS_CERTUS_QUARTZ_CHARGED = forgeTag( "gems/certus_quartz_charged" );
		public static final Tag<Item> GEMS_CERTUS_QUARTZ = forgeTag( "gems/certus_quartz" );
		public static final Tag<Item> GEMS_FLUIX = forgeTag( "gems/fluix" );

		public static final Tag<Item> PEARL_FLUIX = tag("pearl_fluix");

		public static final Tag<Item> GEARS_WOOD = forgeTag("gears/wood");
		public static final Tag<Item> GEARS = forgeTag("gears"); // there's no official forge tag for gears yet?

		public static final Tag<Item> ITEMS_SILICON = forgeTag("items/silicon");

		public static final Tag<Item> STORAGE_BLOCKS_FLUIX = forgeTag( "storage_blocks/fluix" );
		public static final Tag<Item> STORAGE_BLOCKS_CERTUS_QUARTZ = forgeTag( "storage_blocks/certus_quartz" );

		public static final Tag<Item> ORES_CERTUS_QUARTZ = forgeTag( "ores/certus_quartz" );
		public static final Tag<Item> ORES_CERTUS_QUARTZ_CHARGED = forgeTag( "ores/certus_quartz_charged" );

		private static Tag<Item> forgeTag( String path )
		{
			return new ItemTags.Wrapper( new ResourceLocation( "forge", path ) );
		}

		private static Tag<Item> tag( String path )
		{
			return new ItemTags.Wrapper( new ResourceLocation( AppEng.MOD_ID, path ) );
		}

	}

	public static class Blocks
	{

		public static final Tag<Block> STORAGE_BLOCKS_FLUIX = forgeTag( "storage_blocks/fluix" );
		public static final Tag<Block> STORAGE_BLOCKS_CERTUS_QUARTZ = forgeTag( "storage_blocks/certus_quartz" );

		public static final Tag<Block> ORES_CERTUS_QUARTZ = forgeTag( "ores/certus_quartz" );
		public static final Tag<Block> ORES_CERTUS_QUARTZ_CHARGED = forgeTag( "ores/certus_quartz_charged" );

		private static Tag<Block> forgeTag( String path )
		{
			return new BlockTags.Wrapper( new ResourceLocation( "forge", path ) );
		}

	}

}
