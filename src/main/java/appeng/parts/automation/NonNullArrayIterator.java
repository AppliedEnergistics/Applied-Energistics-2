package appeng.parts.automation;

import java.util.Iterator;

import scala.NotImplementedError;

public class NonNullArrayIterator<E> implements Iterator<E>
{

	int offset = 0;
	final E[] g;

	public NonNullArrayIterator(E[] o) {
		g = o;
	}

	@Override
	public boolean hasNext()
	{
		while (offset < g.length && g[offset] == null)
			offset++;

		return offset != g.length;
	}

	@Override
	public E next()
	{
		return g[offset++];
	}

	@Override
	public void remove()
	{
		throw new NotImplementedError();
	}

}
