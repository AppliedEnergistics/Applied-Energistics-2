package appeng.client.render;

import appeng.client.texture.FlipableIcon;
import appeng.client.texture.TmpFlipableIcon;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

public class BlockRenderInfo
{

	public BlockRenderInfo(BaseBlockRender inst) {
		rendererInstance = inst;
	}

	final public BaseBlockRender rendererInstance;

	private boolean useTmp = false;
	private TmpFlipableIcon tmpTopIcon = new TmpFlipableIcon();
	private TmpFlipableIcon tmpBottomIcon = new TmpFlipableIcon();
	private TmpFlipableIcon tmpSouthIcon = new TmpFlipableIcon();
	private TmpFlipableIcon tmpNorthIcon = new TmpFlipableIcon();
	private TmpFlipableIcon tmpEastIcon = new TmpFlipableIcon();
	private TmpFlipableIcon tmpWestIcon = new TmpFlipableIcon();

	private FlipableIcon topIcon = null;
	private FlipableIcon bottomIcon = null;
	private FlipableIcon southIcon = null;
	private FlipableIcon northIcon = null;
	private FlipableIcon eastIcon = null;
	private FlipableIcon westIcon = null;

	public void updateIcons(FlipableIcon Bottom, FlipableIcon Top, FlipableIcon North, FlipableIcon South, FlipableIcon East,
			FlipableIcon West)
	{
		topIcon = Top;
		bottomIcon = Bottom;
		southIcon = South;
		northIcon = North;
		eastIcon = East;
		westIcon = West;

	}

	public void setTemporaryRenderIcon(Icon icon)
	{
		if ( icon == null )
			useTmp = false;
		else
		{
			useTmp = true;
			tmpTopIcon.setOriginal( icon );
			tmpBottomIcon.setOriginal( icon );
			tmpSouthIcon.setOriginal( icon );
			tmpNorthIcon.setOriginal( icon );
			tmpEastIcon.setOriginal( icon );
			tmpWestIcon.setOriginal( icon );
		}
	}

	public void setTemporaryRenderIcons(Icon nTopIcon, Icon nBottomIcon, Icon nSouthIcon, Icon nNorthIcon, Icon nEastIcon,
			Icon nWestIcon)
	{
		tmpTopIcon.setOriginal( nTopIcon == null ? getTexture( ForgeDirection.UP ) : nTopIcon );
		tmpBottomIcon.setOriginal( nBottomIcon == null ? getTexture( ForgeDirection.DOWN ) : nBottomIcon );
		tmpSouthIcon.setOriginal( nSouthIcon == null ? getTexture( ForgeDirection.SOUTH ) : nSouthIcon );
		tmpNorthIcon.setOriginal( nNorthIcon == null ? getTexture( ForgeDirection.NORTH ) : nNorthIcon );
		tmpEastIcon.setOriginal( nEastIcon == null ? getTexture( ForgeDirection.EAST ) : nEastIcon );
		tmpWestIcon.setOriginal( nWestIcon == null ? getTexture( ForgeDirection.WEST ) : nWestIcon );
		useTmp = true;
	}

	public FlipableIcon getTexture(ForgeDirection dir)
	{
		if ( useTmp )
		{
			switch (dir)
			{
			case DOWN:
				return tmpBottomIcon;
			case UP:
				return tmpTopIcon;
			case NORTH:
				return tmpNorthIcon;
			case SOUTH:
				return tmpSouthIcon;
			case EAST:
				return tmpEastIcon;
			case WEST:
				return tmpWestIcon;
			default:
				break;
			}
		}

		switch (dir)
		{
		case DOWN:
			return bottomIcon;
		case UP:
			return topIcon;
		case NORTH:
			return northIcon;
		case SOUTH:
			return southIcon;
		case EAST:
			return eastIcon;
		case WEST:
			return westIcon;
		default:
			break;
		}

		return topIcon;
	}

	public boolean isValid()
	{
		return topIcon != null && bottomIcon != null && southIcon != null && northIcon != null && eastIcon != null
				&& westIcon != null;
	}

}
