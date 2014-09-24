package appeng.util.prioitylist;

import appeng.api.storage.data.IAEStack;

public interface IPartitionList<T extends IAEStack<T>>
{

	boolean isListed(T input);

	boolean isEmpty();

	Iterable<T> getItems();

}
