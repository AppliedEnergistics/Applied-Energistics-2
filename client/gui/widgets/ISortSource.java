package appeng.client.gui.widgets;

import appeng.api.config.SortDir;
import appeng.api.config.ViewItems;

public interface ISortSource
{

	/**
	 * @return Sor
	 */
	Enum getSortBy();

	/**
	 * @return {@link SortDir}
	 */
	Enum getSortDir();

	/**
	 * @return {@link ViewItems}
	 */
	Enum getSortDisplay();

}
