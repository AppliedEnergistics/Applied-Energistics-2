package appeng.container.interfaces;

import appeng.client.gui.widgets.GuiProgressBar;

/**
 * This interface provides the data for anything simulating a progress.
 * 
 * Its main use is in combination with the {@link GuiProgressBar}, which ensures to scale it to a percentage of 0 to
 * 100.
 *
 */
public interface IProgressProvider
{

	/**
	 * The current value of the progress. It should cover a range from 0 to the max progress
	 * 
	 * @return An int representing the current progress
	 */
	int getCurrentProgress();

	/**
	 * The max value the progress.
	 * 
	 * It is not limited to a value of 100 and can be scaled to fit the current needs. For example scaled down to
	 * decrease or scaled up to increase the precision.
	 * 
	 * @return An int representing the max progress
	 */
	int getMaxProgress();

}
