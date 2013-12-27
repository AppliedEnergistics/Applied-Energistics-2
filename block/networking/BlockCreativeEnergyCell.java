package appeng.block.networking;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileCreativeEnergyCell;

public class BlockCreativeEnergyCell extends AEBaseBlock
{

	public BlockCreativeEnergyCell() {
		super( BlockCreativeEnergyCell.class, Material.glass );
		setfeature( EnumSet.of( AEFeature.Creative ) );
		setTileEntiy( TileCreativeEnergyCell.class );
	}

}
