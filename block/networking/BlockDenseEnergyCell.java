package appeng.block.networking;

import java.util.EnumSet;

import net.minecraft.util.IIcon;
import appeng.client.texture.ExtraBlockTextures;
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
		setTileEntity( TileDenseEnergyCell.class );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		switch (metadata)
		{
		case 0:
			return ExtraBlockTextures.MEDenseEnergyCell0.getIcon();
		case 1:
			return ExtraBlockTextures.MEDenseEnergyCell1.getIcon();
		case 2:
			return ExtraBlockTextures.MEDenseEnergyCell2.getIcon();
		case 3:
			return ExtraBlockTextures.MEDenseEnergyCell3.getIcon();
		case 4:
			return ExtraBlockTextures.MEDenseEnergyCell4.getIcon();
		case 5:
			return ExtraBlockTextures.MEDenseEnergyCell5.getIcon();
		case 6:
			return ExtraBlockTextures.MEDenseEnergyCell6.getIcon();
		case 7:
			return ExtraBlockTextures.MEDenseEnergyCell7.getIcon();

		}
		return super.getIcon( direction, metadata );
	}

}
