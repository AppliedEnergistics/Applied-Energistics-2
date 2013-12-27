package appeng.block.storage;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.storage.TileIOPort;

public class BlockIOPort extends AEBaseBlock
{

	public BlockIOPort() {
		super( BlockIOPort.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.StorageCells, AEFeature.IOPort ) );
		setTileEntiy( TileIOPort.class );
	}

}
