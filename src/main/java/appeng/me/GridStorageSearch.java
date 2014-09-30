package appeng.me;

import java.lang.ref.WeakReference;

public class GridStorageSearch
{

	final long id;
	public WeakReference<GridStorage> gridStorage;

	/**
	 * for use with the world settings
	 * 
	 * @param id ID of grid storage search
	 */
	public GridStorageSearch(long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;

		GridStorageSearch other = (GridStorageSearch) obj;
		if ( id == other.id )
			return true;

		return false;
	}

	@Override
	public int hashCode()
	{
		return ((Long) id).hashCode();
	}

}
