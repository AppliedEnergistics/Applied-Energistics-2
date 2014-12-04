package appeng.api.movable;

/**
 * You can implement this, or use the IMovableRegistry to white list your tile,
 * please see the registry for more information.
 */
public interface IMovableTile
{

	/**
	 * notification that your block will be moved, called instead of invalidate,
	 * return false to prevent movement.
	 * 
	 * @return false to prevent movement
	 */
	boolean prepareToMove();

	/**
	 * notification that your block was moved, called after validate.
	 */
	void doneMoving();

}
