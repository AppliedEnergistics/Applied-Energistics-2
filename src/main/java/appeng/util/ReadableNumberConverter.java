package appeng.util;


/**
 * Converter class to convert a large number into a SI system.
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public enum ReadableNumberConverter
{
	INSTANCE;

	/**
	 * Defines the base for a division, non-si standard could be 1024 for kilobytes
	 */
	private static final int DIVISION_BASE = 1000;

	/**
	 * for lg(1000) = 3, just saves some calculation
	 */
	private static final double LOG_DIVISION_BASE = Math.log( DIVISION_BASE );

	/**
	 * String representation of the sorted postfixes
	 */
	private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();

	/**
	 * if a result would be higher than this threshold,
	 * it is pushed into the next bigger group,
	 * so the display string is shorter
	 */
	private static final int SHORT_THRESHOLD = 100;

	/**
	 * Converts a number into a human readable form. It will not round the number, but floor it.
	 *
	 * Example: 15555L -> 15.5K
	 *
	 * @param number to be converted number
	 *
	 * @return String in SI format cut down as far as possible
	 */
	public String toHumanReadableForm( long number )
	{
		final String sign = this.getSign( number );
		final long absNumber = Math.abs( number );

		if( absNumber < DIVISION_BASE )
			return Long.toString( absNumber );

		final int exp = (int) ( Math.log( absNumber ) / LOG_DIVISION_BASE );
		final char postFix = ENCODED_POSTFIXES[exp - 1];
		final int result = (int) ( absNumber / Math.pow( DIVISION_BASE, exp ) );

		return String.format( "%s%d%s", sign, result, postFix );
	}

	/**
	 * Converts a number into a human readable form. It will not round the number, but floor it.
	 * Will try to cut the number down 1 decimal earlier. This will limit the String size to 3 chars.
	 *
	 * Example: 900L -> 0.9K
	 *
	 * @param number to be converted number
	 *
	 * @return String in SI format cut down as far as possible
	 */
	public String toShortHumanReadableForm( long number )
	{
		final String sign = this.getSign( number );
		final long absNumber = Math.abs( number );

		if( absNumber < DIVISION_BASE )
			return Long.toString( absNumber );

		final int exp = (int) ( Math.log( absNumber ) / LOG_DIVISION_BASE );
		final int result = (int) ( absNumber / Math.pow( DIVISION_BASE, exp ) );
		if( result >= SHORT_THRESHOLD )
		{
			final int shortResult = result / SHORT_THRESHOLD;
			final char postFix = ENCODED_POSTFIXES[exp];

			return String.format( "%s.%d%s", sign, shortResult, postFix );
		}
		else
		{
			final char postFix = ENCODED_POSTFIXES[exp - 1];

			return String.format( "%s%d%s", sign, result, postFix );
		}
	}

	/**
	 * Gets character representation of the sign of a number
	 *
	 * @param number maybe signed number
	 *
	 * @return '-' if the number is signed, else an empty character
	 */
	private String getSign( long number )
	{
		if ( number < 0 )
		{
			return "-";
		}
		else
		{
			return "";
		}
	}
}
