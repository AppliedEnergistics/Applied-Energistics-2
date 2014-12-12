package appeng.api.util;

public interface IReadOnlyCollection<T> extends Iterable<T>
{

	/**
	 * @return the objects in in the set.
	 */
	int size();

	/**
	 * @return true if there are objects in the set
	 */
	boolean isEmpty();

	/**
	 * @return return true if the object is part of the set.
	 */
	boolean contains(Object node);

}
