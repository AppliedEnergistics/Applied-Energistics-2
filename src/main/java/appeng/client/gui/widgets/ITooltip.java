package appeng.client.gui.widgets;

/**
 * AEBaseGui controlled Tooltip Interface.
 * 
 */
public interface ITooltip
{

	/**
	 * returns the tooltip message.
	 * 
	 * @return tooltip message
	 */
	String getMsg();

	/**
	 * x Location for the object that triggers the tooltip.
	 * 
	 * @return xPosition
	 */
	int xPos();

	/**
	 * y Location for the object that triggers the tooltip.
	 * 
	 * @return yPosition
	 */
	int yPos();

	/**
	 * Width of the object that triggers the tooltip.
	 * 
	 * @return width
	 */
	int getWidth();

	/**
	 * Height for the object that triggers the tooltip.
	 * 
	 * @return height
	 */
	int getHeight();

	/**
	 * @return true if button being drawn
	 */
	boolean isVisible();

}
