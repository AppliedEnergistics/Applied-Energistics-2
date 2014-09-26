package uristqwerty.gui_craftguide.editor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GuiElementMeta
{
	public String name();

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface GuiElementProperty
	{
		/**
		 * The name of this property, as seen in the in-game editor
		 * and as used when a template is being read from a file or
		 * written to a file.
		 */
		public String name();
		
		public boolean required() default false;
	}
	
	public @interface EnumMapProperty
	{
		public Class<? extends Enum> keyType();
		public Class<?> valueType();
	}
}
