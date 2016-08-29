package appeng.api;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Use this annotation on a class in your Mod to have it instantiated during the initialization phase of Applied Energistics.
 * AE expects your class to have a single constructor and can supply certain arguments to your constructor using dependency injection.
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface AEPlugin
{
}
