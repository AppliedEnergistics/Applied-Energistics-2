package appeng.debug;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import appeng.block.AEBaseBlock;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;

public class BlockChunkloader extends AEBaseBlock implements LoadingCallback
{

	public BlockChunkloader() {
		super( BlockChunkloader.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
		setTileEntiy( TileChunkLoader.class );
		ForgeChunkManager.setForcedChunkLoadingCallback( AppEng.instance, this );
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{

	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		registerNoIcons();
	}

}
