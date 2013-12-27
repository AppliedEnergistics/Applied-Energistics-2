package appeng.block.networking;

import java.util.EnumSet;

import net.minecraft.util.Icon;
import appeng.client.texture.ExtraTextures;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileDenseEnergyCell;

public class BlockDenseEnergyCell extends BlockEnergyCell
{

	@Override
	public double getMaxPower()
	{
		return 200000.0 * 8.0;
	}

	public BlockDenseEnergyCell() {
		super( BlockDenseEnergyCell.class );
		setfeature( EnumSet.of( AEFeature.DenseEnergyCells ) );
		setTileEntiy( TileDenseEnergyCell.class );
	}

	@Override
	public Icon getIcon(int direction, int metadata)
	{
		switch (metadata)
		{
		case 0:
			return ExtraTextures.MEDenseEnergyCell0.getIcon();
		case 1:
			return ExtraTextures.MEDenseEnergyCell1.getIcon();
		case 2:
			return ExtraTextures.MEDenseEnergyCell2.getIcon();
		case 3:
			return ExtraTextures.MEDenseEnergyCell3.getIcon();
		case 4:
			return ExtraTextures.MEDenseEnergyCell4.getIcon();
		case 5:
			return ExtraTextures.MEDenseEnergyCell5.getIcon();
		case 6:
			return ExtraTextures.MEDenseEnergyCell6.getIcon();
		case 7:
			return ExtraTextures.MEDenseEnergyCell7.getIcon();

		}
		return super.getIcon( direction, metadata );
	}

}
