package appeng.client.render;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appeng.client.render.model.UVLModelLoader;

@Mod(modid = "UVLightmapJsonTest", name = "UVLightmapJsonTest", version ="0.0.0")
public class UVLightmapJsonTest
{

	private static final ModelResourceLocation uvlblockModel = new ModelResourceLocation( new ResourceLocation( "UVLightmapJsonTest", "uvlblock" ), "normal" );

	public static Block uvlblock;
	public static Item uvlblockItem;

	@EventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
		GameRegistry.register( uvlblock = new Block( Material.IRON ){

			final AxisAlignedBB box = new AxisAlignedBB( 0.25, 0, 7 / 16d, 0.75, 1, 9 / 16d );

			public boolean isFullBlock(IBlockState state)
			{
				return false;
			}

			public boolean isOpaqueCube(IBlockState state)
			{
				return false;
			}

			public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) 
			{
				return box;
			}

			public BlockRenderLayer getBlockLayer()
			{
				return BlockRenderLayer.CUTOUT;
			}

		}.setLightLevel( 0.2f ).setCreativeTab( CreativeTabs.DECORATIONS ).setRegistryName( new ResourceLocation( "UVLightmapJsonTest", "uvlblock" ) ) );
		GameRegistry.register( uvlblockItem = new ItemBlock( uvlblock ).setRegistryName( new ResourceLocation( "UVLightmapJsonTest", "uvlblock" ) ) );

		ModelBakery.registerItemVariants( uvlblockItem, uvlblockModel );

	}

	@EventHandler
	public void init( FMLInitializationEvent event )
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(uvlblockItem, 0, uvlblockModel);
	}

}
