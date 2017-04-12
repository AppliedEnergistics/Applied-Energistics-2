package appeng.util;


import javax.annotation.Nonnegative;


/**
 * Limits a number converter to a char width of at max 3 characters.
 * This is generally used for players, who activated the large font extension.
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface ISlimReadableNumberConverter
{
	/**
	 * Converts a number into a human readable form. It will not round the number, but down it.
	 * Will try to cut the number down 1 decimal later, but rarely because of the 3 width limitation.
	 * Can only handle non negative numbers
	 * <p>
	 * Example:
	 * 10000L -> 10K
	 * 9999L -> 9K, not 9.9K cause 4 width
	 *
	 * @param number to be converted number
	 * @return String in SI format cut down as far as possible
	 */
	String toSlimReadableForm( @Nonnegative long number );
}
