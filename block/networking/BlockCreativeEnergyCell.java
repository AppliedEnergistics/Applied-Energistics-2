package appeng.block.networking;

import java.util.EnumSet;

import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.networking.TileCreativeEnergyCell;

public class BlockCreativeEnergyCell extends AEBaseBlock
{

	public BlockCreativeEnergyCell() {
		super( BlockCreativeEnergyCell.class, AEGlassMaterial.instance );
		setfeature( EnumSet.of( AEFeature.Creative ) );
		setTileEntity( TileCreativeEnergyCell.class );
	}

}
