package appeng.util;


import javax.annotation.Nonnegative;


/**
 * Limits a number converter to a char width of at max 4 characters
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IWideReadableNumberConverter
{
	/**
	 * Converts a number into a human readable form. It will not round the number, but down it.
	 * Will try to cut the number down 1 decimal later if width can be below 4.
	 * Can only handle non negative numbers
	 * <p>
	 * Example:
	 * 10000L -> 10K
	 * 9999L -> 9999
	 *
	 * @param number to be converted number
	 * @return String in SI format cut down as far as possible
	 */
	String toWideReadableForm( @Nonnegative long number );
}
