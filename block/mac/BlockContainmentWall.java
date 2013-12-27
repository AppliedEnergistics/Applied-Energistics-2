package appeng.block.mac;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.mac.TileMolecularAssembler;

public class BlockContainmentWall extends AEBaseBlock
{

	public BlockContainmentWall() {
		super( BlockContainmentWall.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.MolecularAssembler ) );
		setTileEntiy( TileMolecularAssembler.class );
	}

}
