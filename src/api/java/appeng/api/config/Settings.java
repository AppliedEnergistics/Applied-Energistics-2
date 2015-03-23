/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.config;


import java.util.EnumSet;
import javax.annotation.Nonnull;


public enum Settings
{
	LEVEL_EMITTER_MODE( EnumSet.allOf( LevelEmitterMode.class ) ),

	REDSTONE_EMITTER( EnumSet.of( RedstoneMode.HIGH_SIGNAL, RedstoneMode.LOW_SIGNAL ) ), REDSTONE_CONTROLLED( EnumSet.allOf( RedstoneMode.class ) ),

	CONDENSER_OUTPUT( EnumSet.allOf( CondenserOutput.class ) ),

	POWER_UNITS( EnumSet.allOf( PowerUnits.class ) ), ACCESS( EnumSet.of( AccessRestriction.READ_WRITE, AccessRestriction.READ, AccessRestriction.WRITE ) ),

	SORT_DIRECTION( EnumSet.allOf( SortDir.class ) ), SORT_BY( EnumSet.allOf( SortOrder.class ) ),

	SEARCH_TOOLTIPS( EnumSet.of( YesNo.YES, YesNo.NO ) ), VIEW_MODE( EnumSet.allOf( ViewItems.class ) ), SEARCH_MODE( EnumSet.allOf( SearchBoxMode.class ) ),

	ACTIONS( EnumSet.allOf( ActionItems.class ) ), IO_DIRECTION( EnumSet.of( RelativeDirection.LEFT, RelativeDirection.RIGHT ) ),

	BLOCK( EnumSet.of( YesNo.YES, YesNo.NO ) ), OPERATION_MODE( EnumSet.allOf( OperationMode.class ) ),

	FULLNESS_MODE( EnumSet.allOf( FullnessMode.class ) ), CRAFT_ONLY( EnumSet.of( YesNo.YES, YesNo.NO ) ),

	FUZZY_MODE( EnumSet.allOf( FuzzyMode.class ) ), LEVEL_TYPE( EnumSet.allOf( LevelType.class ) ),

	TERMINAL_STYLE( EnumSet.of( TerminalStyle.TALL, TerminalStyle.SMALL ) ), COPY_MODE( EnumSet.allOf( CopyMode.class ) ),

	INTERFACE_TERMINAL( EnumSet.of( YesNo.YES, YesNo.NO ) ), CRAFT_VIA_REDSTONE( EnumSet.of( YesNo.YES, YesNo.NO ) ),

	STORAGE_FILTER( EnumSet.allOf( StorageFilter.class ) ), PLACE_BLOCK( EnumSet.of( YesNo.YES, YesNo.NO ) );

	private final EnumSet<? extends Enum<?>> values;

	Settings( @Nonnull EnumSet<? extends Enum<?>> possibleOptions )
	{
		if ( possibleOptions.isEmpty() )
		{
			throw new IllegalArgumentException( "Tried to instantiate an empty setting." );
		}

		this.values = possibleOptions;
	}

	public EnumSet<? extends Enum<?>> getPossibleValues()
	{
		return this.values;
	}

}
