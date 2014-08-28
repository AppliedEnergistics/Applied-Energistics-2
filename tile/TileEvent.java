package appeng.tile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import appeng.tile.events.TileEventType;

@Retention(RetentionPolicy.RUNTIME)
public @interface TileEvent {

	TileEventType value();

}
