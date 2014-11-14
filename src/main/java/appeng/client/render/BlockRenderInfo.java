/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

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
	private final TmpFlippableIcon tmpTopIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpBottomIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpSouthIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpNorthIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpEastIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpWestIcon = new TmpFlippableIcon();

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
