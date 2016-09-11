package appeng.client;


import net.minecraftforge.common.property.IUnlistedProperty;


/**
 * A generic implementation for {@link IUnlistedProperty}.
 * @param <T>
 */
public class UnlistedProperty<T> implements IUnlistedProperty<T>
{

	private final String name;

	private final Class<T> clazz;

	public UnlistedProperty( String name, Class<T> clazz )
	{
		this.name = name;
		this.clazz = clazz;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isValid( T value )
	{
		return value != null;
	}

	@Override
	public Class<T> getType()
	{
		return clazz;
	}

	@Override
	public String valueToString( T value )
	{
		return value.toString();
	}

}
