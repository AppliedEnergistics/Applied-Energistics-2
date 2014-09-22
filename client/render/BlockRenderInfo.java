package appeng.client.render;

import appeng.client.texture.FlippableIcon;
import appeng.client.texture.TmpFlippableIcon;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRenderInfo
{

	public BlockRenderInfo(BaseBlockRender inst) {
		rendererInstance = inst;
	}

	final public BaseBlockRender rendererInstance;

	private boolean useTmp = false;
	private TmpFlippableIcon tmpTopIcon = new TmpFlippableIcon();
	private TmpFlippableIcon tmpBottomIcon = new TmpFlippableIcon();
	private TmpFlippableIcon tmpSouthIcon = new TmpFlippableIcon();
	private TmpFlippableIcon tmpNorthIcon = new TmpFlippableIcon();
	private TmpFlippableIcon tmpEastIcon = new TmpFlippableIcon();
	private TmpFlippableIcon tmpWestIcon = new TmpFlippableIcon();

	private FlippableIcon topIcon = null;
	private FlippableIcon bottomIcon = null;
	private FlippableIcon southIcon = null;
	private FlippableIcon northIcon = null;
	private FlippableIcon eastIcon = null;
	private FlippableIcon westIcon = null;

	public void updateIcons(FlippableIcon Bottom, FlippableIcon Top, FlippableIcon North, FlippableIcon South, FlippableIcon East, FlippableIcon West)
	{
		topIcon = Top;
		bottomIcon = Bottom;
		southIcon = South;
		northIcon = North;
		eastIcon = East;
		westIcon = West;

	}

	public void setTemporaryRenderIcon(IIcon IIcon)
	{
		if ( IIcon == null )
			useTmp = false;
		else
		{
			useTmp = true;
			tmpTopIcon.setOriginal( IIcon );
			tmpBottomIcon.setOriginal( IIcon );
			tmpSouthIcon.setOriginal( IIcon );
			tmpNorthIcon.setOriginal( IIcon );
			tmpEastIcon.setOriginal( IIcon );
			tmpWestIcon.setOriginal( IIcon );
		}
	}

	public void setTemporaryRenderIcons(IIcon nTopIcon, IIcon nBottomIcon, IIcon nSouthIcon, IIcon nNorthIcon, IIcon nEastIcon, IIcon nWestIcon)
	{
		tmpTopIcon.setOriginal( nTopIcon == null ? getTexture( ForgeDirection.UP ) : nTopIcon );
		tmpBottomIcon.setOriginal( nBottomIcon == null ? getTexture( ForgeDirection.DOWN ) : nBottomIcon );
		tmpSouthIcon.setOriginal( nSouthIcon == null ? getTexture( ForgeDirection.SOUTH ) : nSouthIcon );
		tmpNorthIcon.setOriginal( nNorthIcon == null ? getTexture( ForgeDirection.NORTH ) : nNorthIcon );
		tmpEastIcon.setOriginal( nEastIcon == null ? getTexture( ForgeDirection.EAST ) : nEastIcon );
		tmpWestIcon.setOriginal( nWestIcon == null ? getTexture( ForgeDirection.WEST ) : nWestIcon );
		useTmp = true;
	}

	public FlippableIcon getTexture(ForgeDirection dir)
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
		return topIcon != null && bottomIcon != null && southIcon != null && northIcon != null && eastIcon != null && westIcon != null;
	}

}
