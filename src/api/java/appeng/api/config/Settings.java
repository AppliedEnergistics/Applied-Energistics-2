package appeng.api.config;

import java.util.EnumSet;

public enum Settings
{
	LEVEL_EMITTER_MODE(EnumSet.allOf( LevelEmitterMode.class )),

	REDSTONE_EMITTER(EnumSet.of( RedstoneMode.HIGH_SIGNAL, RedstoneMode.LOW_SIGNAL )), REDSTONE_CONTROLLED(EnumSet.allOf( RedstoneMode.class )),

	CONDENSER_OUTPUT(EnumSet.allOf( CondenserOutput.class )),

	POWER_UNITS(EnumSet.allOf( PowerUnits.class )), ACCESS(EnumSet.of( AccessRestriction.READ_WRITE, AccessRestriction.READ, AccessRestriction.WRITE )),

	SORT_DIRECTION(EnumSet.allOf( SortDir.class )), SORT_BY(EnumSet.allOf( SortOrder.class )),

	SEARCH_TOOLTIPS(EnumSet.of( YesNo.YES, YesNo.NO )), VIEW_MODE(EnumSet.allOf( ViewItems.class )), SEARCH_MODE(EnumSet.allOf( SearchBoxMode.class )),

	ACTIONS(EnumSet.allOf( ActionItems.class )), IO_DIRECTION(EnumSet.of( RelativeDirection.LEFT, RelativeDirection.RIGHT )),

	BLOCK(EnumSet.of( YesNo.YES, YesNo.NO )), OPERATION_MODE(EnumSet.allOf( OperationMode.class )),

	FULLNESS_MODE(EnumSet.allOf( FullnessMode.class )), CRAFT_ONLY(EnumSet.of( YesNo.YES, YesNo.NO )),

	FUZZY_MODE(EnumSet.allOf( FuzzyMode.class )), LEVEL_TYPE(EnumSet.allOf( LevelType.class )),

	TERMINAL_STYLE(EnumSet.of( TerminalStyle.TALL, TerminalStyle.SMALL )), COPY_MODE(EnumSet.allOf( CopyMode.class )),

	INTERFACE_TERMINAL(EnumSet.of( YesNo.YES, YesNo.NO )), CRAFT_VIA_REDSTONE(EnumSet.of( YesNo.YES, YesNo.NO )),

	STORAGE_FILTER(EnumSet.allOf( StorageFilter.class ));

	private EnumSet values;

	public EnumSet getPossibleValues()
	{
		return values;
	}

	private Settings(EnumSet set) {
		if ( set == null || set.isEmpty() )
			throw new RuntimeException( "Invalid configuration." );
		values = set;
	}

}
