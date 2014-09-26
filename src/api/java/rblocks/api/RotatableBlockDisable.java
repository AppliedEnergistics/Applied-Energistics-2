package rblocks.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * If found on a Block, Rotatable Blocks will ignore the block, even if it would have normally supported it.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RotatableBlockDisable {

}
