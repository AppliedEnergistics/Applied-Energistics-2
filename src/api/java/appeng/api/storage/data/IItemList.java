package appeng.api.storage.data;

import java.util.Iterator;

/**
 * Represents a list of items in AE.
 * 
 * Don't Implement.
 * 
 * Construct with Util.createItemList()
 */
public interface IItemList<StackType extends IAEStack> extends IItemContainer<StackType>, Iterable<StackType>
{

	/**
	 * add a stack to the list stackSize is used to add to stackSize, this will merge the stack with an item already in
	 * the list if found.
	 * 
	 * @param option stacktype option
	 */
	public void addStorage(StackType option); // adds a stack as stored

	/**
	 * add a stack to the list as craftable, this will merge the stack with an item already in the list if found.
	 * 
	 * @param option stacktype option
	 */
	public void addCrafting(StackType option);

	/**
	 * add a stack to the list, stack size is used to add to requestable, this will merge the stack with an item already
	 * in the list if found.
	 * 
	 * @param option stacktype option
	 */
	public void addRequestable(StackType option); // adds a stack as requestable

	/**
	 * @return the first item in the list
	 */
	StackType getFirstItem();

	/**
	 * @return the number of items in the list
	 */
	int size();

	/**
	 * allows you to iterate the list.
	 */
	@Override
	public Iterator<StackType> iterator();

	/**
	 * resets stack sizes to 0.
	 */
	void resetStatus();

}