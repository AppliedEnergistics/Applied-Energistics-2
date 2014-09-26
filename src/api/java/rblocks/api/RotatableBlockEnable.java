package rblocks.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * if this annotation is found on a block, Rotatable Blocks will consider it supported, even if it normally would not
 * be, this dosn't magically make it work, but it will allow you to possibly render the rotation or work around the
 * Limitations.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RotatableBlockEnable {

}
