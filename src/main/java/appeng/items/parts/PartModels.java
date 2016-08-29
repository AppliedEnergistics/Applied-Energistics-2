package appeng.items.parts;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation is used to mark static fields or static methods that return/contain models used
 * for a part. They are automatically registered as part of the part item registration.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( {
		ElementType.FIELD,
		ElementType.METHOD
} )
public @interface PartModels
{
}
