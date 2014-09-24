package appeng.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class SortedList<T> implements Iterable<T>, List<T>, Cloneable {

	private boolean sorted = true;
	private final Comparator<T> comp;
	private final LinkedList<T> storage = new LinkedList<T>();

	private void makeSorted() {
		if (!sorted) {
			sorted = true;
			Collections.sort(storage, comp);
		}
	}

	public SortedList(Comparator<T> comp) {
		this.comp = comp;
	}

	@Override
	public boolean add(T input) {
		sorted = false;
		return storage.add(input);
	}

	@Override
	public boolean addAll(Collection<? extends T> input) {
		if (!input.isEmpty())
			sorted = false;
		return storage.addAll(input);
	}

	@Override
	public void clear() {
		sorted = true;
		storage.clear();
	}

	@Override
	public boolean contains(Object input) {
		return storage.contains(input);
	}

	@Override
	public boolean containsAll(Collection<?> input) {
		return storage.containsAll(input);
	}

	@Override
	public boolean isEmpty() {
		return isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		makeSorted();
		return storage.iterator();
	}

	public Iterator<T> reverseIterator() {
		makeSorted();
		final ListIterator<T> listIterator = listIterator(size());

		return new Iterator<T>() {

			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			public T next() {
				return listIterator.previous();
			}

			public void remove() {
				listIterator.remove();
			}

		};
	}

	@Override
	public boolean remove(Object input) {
		return storage.remove(input);
	}

	@Override
	public boolean removeAll(Collection<?> input) {
		return storage.removeAll(input);
	}

	@Override
	public boolean retainAll(Collection<?> input) {
		return storage.retainAll(input);
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public Object[] toArray() {
		return storage.toArray();
	}

	@Override
	public <X> X[] toArray(X[] input) {
		return storage.toArray(input);
	}

	public Comparator<? super T> comparator() {
		return comp;
	}

	public T first() {
		makeSorted();
		return storage.peekFirst();
	}

	public T last() {
		makeSorted();
		return storage.peekLast();
	}

	@Override
	public void add(int index, T element) {
		makeSorted();
		sorted = false;
		add(index, element);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		sorted = false;
		return addAll(index, c);
	}

	@Override
	public T get(int index) {
		makeSorted();
		return get(index);
	}

	@Override
	public int indexOf(Object o) {
		makeSorted();
		return indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		makeSorted();
		return lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		makeSorted();
		return listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		makeSorted();
		return listIterator(index);
	}

	@Override
	public T remove(int index) {
		makeSorted();
		return remove(index);
	}

	@Override
	public T set(int index, T element) {
		makeSorted();
		sorted = false;
		return set(index, element);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		makeSorted();
		return storage.subList(fromIndex, toIndex);
	}

}
