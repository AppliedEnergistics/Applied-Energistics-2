/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 * Copyright (c) 2014 Kane York
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

package appeng.helpers;

import appeng.core.localization.GuiText;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Carries a message representing the diagnostic for an invalid pattern.
 * (The message is intended for human consumption.)
 */
public class InvalidPatternException extends Exception
{
	public static final int REASON_EMPTY = 1;
	public static final int REASON_UNCRAFTABLE = 2;
	public static final int REASON_NO_INPUT = 3;
	public static final int REASON_NO_OUTPUT = 4;
	public static final int REASON_BAD_ITEM = 5;

	private final int reason;
	private String nbtString;

	public InvalidPatternException(int reason)
	{
		super( localization( reason ).getLocal() );
		this.reason = reason;
		this.nbtString = null;
	}

	public InvalidPatternException(NBTTagCompound tag)
	{
		this( REASON_BAD_ITEM );
		this.nbtString = tag.toString();
	}

	private static GuiText localization(int reason)
	{
		switch ( reason )
		{
			case REASON_EMPTY:
				return GuiText.InvalidPatternEmpty;
			case REASON_UNCRAFTABLE:
				return GuiText.InvalidPatternUncraftable;
			case REASON_NO_INPUT:
				return GuiText.InvalidPatternNoInput;
			case REASON_NO_OUTPUT:
				return GuiText.InvalidPatternNoOutput;
			case REASON_BAD_ITEM:
				return GuiText.InvalidPatternBadItem;
		}
		return GuiText.InvalidPattern; // reasonable fallback, but it'll look ugly
	}

	@Override
	public String getMessage()
	{
		return getLocalizedMessage();
	}

	@Override
	public String getLocalizedMessage()
	{
		String tmp = localization( reason ).getLocal();
		if ( reason == REASON_BAD_ITEM )
		{
			tmp = tmp + ": " + nbtString;
		}
		return tmp;
	}

	// just in case some other code calling us needs it
	@SuppressWarnings("unused")
	public int getReasonNumber()
	{
		return reason;
	}
}
