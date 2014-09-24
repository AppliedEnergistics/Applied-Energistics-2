package appeng.me;

import java.lang.ref.WeakReference;

public class GridStorageSearch
{

	final long id;
	public WeakReference<GridStorage> gridStorage;

	/**
	 * for use with the world settings
	 * 
	 * @param id
	 */
	public GridStorageSearch(long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj == null )
			return false;

		GridStorageSearch b = (GridStorageSearch) obj;

		if ( id == b.id )
			return true;

		return false;
	}

	@Override
	public int hashCode()
	{
		return ((Long) id).hashCode();
	}

}
