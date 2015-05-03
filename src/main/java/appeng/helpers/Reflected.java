package appeng.helpers;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker interface to help identify invocation of reflection
 */
@Retention( RetentionPolicy.SOURCE )
@Target( { ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD } )
public @interface Reflected
{

}
