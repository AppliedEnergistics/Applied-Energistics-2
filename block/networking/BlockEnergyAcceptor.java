package appeng.block.networking;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileEnergyAcceptor;

public class BlockEnergyAcceptor extends AEBaseBlock
{

	public BlockEnergyAcceptor() {
		super( BlockEnergyAcceptor.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setTileEntiy( TileEnergyAcceptor.class );
	}

}
