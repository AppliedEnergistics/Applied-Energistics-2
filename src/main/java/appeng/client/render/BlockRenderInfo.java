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


import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.client.texture.FlippableIcon;
import appeng.client.texture.TmpFlippableIcon;


public class BlockRenderInfo
{

	final public BaseBlockRender rendererInstance;
	private final TmpFlippableIcon tmpTopIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpBottomIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpSouthIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpNorthIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpEastIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpWestIcon = new TmpFlippableIcon();
	private boolean useTmp = false;
	private FlippableIcon topIcon = null;
	private FlippableIcon bottomIcon = null;
	private FlippableIcon southIcon = null;
	private FlippableIcon northIcon = null;
	private FlippableIcon eastIcon = null;
	private FlippableIcon westIcon = null;

	public BlockRenderInfo( BaseBlockRender inst )
	{
		this.rendererInstance = inst;
	}

	public void updateIcons( FlippableIcon Bottom, FlippableIcon Top, FlippableIcon North, FlippableIcon South, FlippableIcon East, FlippableIcon West )
	{
		this.topIcon = Top;
		this.bottomIcon = Bottom;
		this.southIcon = South;
		this.northIcon = North;
		this.eastIcon = East;
		this.westIcon = West;
	}

	public void setTemporaryRenderIcon( IIcon IIcon )
	{
		if( IIcon == null )
			this.useTmp = false;
		else
		{
			this.useTmp = true;
			this.tmpTopIcon.setOriginal( IIcon );
			this.tmpBottomIcon.setOriginal( IIcon );
			this.tmpSouthIcon.setOriginal( IIcon );
			this.tmpNorthIcon.setOriginal( IIcon );
			this.tmpEastIcon.setOriginal( IIcon );
			this.tmpWestIcon.setOriginal( IIcon );
		}
	}

	public void setTemporaryRenderIcons( IIcon nTopIcon, IIcon nBottomIcon, IIcon nSouthIcon, IIcon nNorthIcon, IIcon nEastIcon, IIcon nWestIcon )
	{
		this.tmpTopIcon.setOriginal( nTopIcon == null ? this.getTexture( ForgeDirection.UP ) : nTopIcon );
		this.tmpBottomIcon.setOriginal( nBottomIcon == null ? this.getTexture( ForgeDirection.DOWN ) : nBottomIcon );
		this.tmpSouthIcon.setOriginal( nSouthIcon == null ? this.getTexture( ForgeDirection.SOUTH ) : nSouthIcon );
		this.tmpNorthIcon.setOriginal( nNorthIcon == null ? this.getTexture( ForgeDirection.NORTH ) : nNorthIcon );
		this.tmpEastIcon.setOriginal( nEastIcon == null ? this.getTexture( ForgeDirection.EAST ) : nEastIcon );
		this.tmpWestIcon.setOriginal( nWestIcon == null ? this.getTexture( ForgeDirection.WEST ) : nWestIcon );
		this.useTmp = true;
	}

	public FlippableIcon getTexture( ForgeDirection dir )
	{
		if( this.useTmp )
		{
			switch( dir )
			{
				case DOWN:
					return this.tmpBottomIcon;
				case UP:
					return this.tmpTopIcon;
				case NORTH:
					return this.tmpNorthIcon;
				case SOUTH:
					return this.tmpSouthIcon;
				case EAST:
					return this.tmpEastIcon;
				case WEST:
					return this.tmpWestIcon;
				default:
					break;
			}
		}

		switch( dir )
		{
			case DOWN:
				return this.bottomIcon;
			case UP:
				return this.topIcon;
			case NORTH:
				return this.northIcon;
			case SOUTH:
				return this.southIcon;
			case EAST:
				return this.eastIcon;
			case WEST:
				return this.westIcon;
			default:
				break;
		}

		return this.topIcon;
	}

	public boolean isValid()
	{
		return this.topIcon != null && this.bottomIcon != null && this.southIcon != null && this.northIcon != null && this.eastIcon != null && this.westIcon != null;
	}
}
