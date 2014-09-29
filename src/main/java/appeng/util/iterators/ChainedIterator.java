package appeng.util.iterators;

import java.util.Iterator;

public class ChainedIterator<T> implements Iterator<T>
{

	int offset = 0;
	final T[] list;

	public ChainedIterator(T... list) {
		this.list = list;
	}

	@Override
	public boolean hasNext()
	{
		return offset < list.length;
	}

	@Override
	public T next()
	{
		return list[offset++];
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "Not implemented." );
	}

}
