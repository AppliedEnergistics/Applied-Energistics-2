package appeng.util;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Test for {@link ISlimReadableNumberConverter}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class SlimReadableNumberConverterTest
{
	private static final long NUMBER_NEG_999999 = -999999L;
	private static final String RESULT_NEG_999999 = "-0M";

	private static final long NUMBER_NEG_9999 = -9999L;
	private static final String RESULT_NEG_9999 = "-9K";

	private static final long NUMBER_NEG_999 = -999L;
	private static final String RESULT_NEG_999 = "-0K";

	private static final long NUMBER_0 = 0L;
	private static final String RESULT_0 = "0";

	private static final long NUMBER_999 = 999L;
	private static final String RESULT_999 = "999";

	private static final long NUMBER_9999 = 9999L;
	private static final String RESULT_9999 = "9K";

	private static final long NUMBER_10000 = 10000L;
	private static final String RESULT_10000 = "10K";

	private static final long NUMBER_10500 = 10500L;
	private static final String RESULT_10500 = "10K";

	private static final long NUMBER_155555 = 155555L;
	private static final String RESULT_155555 = ".1M";

	private static final long NUMBER_9999999 = 9999999L;
	private static final String RESULT_9999999 = "9M";

	private static final long NUMBER_10000000 = 10000000L;
	private static final String RESULT_10000000 = "10M";

	private static final long NUMBER_155555555 = 155555555L;
	private static final String RESULT_155555555 = ".1G";

	private final ISlimReadableNumberConverter converter = ReadableNumberConverter.INSTANCE;

	@Test( expected = AssertionError.class )
	public void testConvertNeg999999()
	{
		assertEquals( RESULT_NEG_999999, this.converter.toSlimReadableForm( NUMBER_NEG_999999 ) );
	}

	@Test( expected = AssertionError.class )
	public void testConvertNeg9999()
	{
		assertEquals( RESULT_NEG_9999, this.converter.toSlimReadableForm( NUMBER_NEG_9999 ) );
	}

	@Test( expected = AssertionError.class )
	public void testConvertNeg999()
	{
		assertEquals( RESULT_NEG_999, this.converter.toSlimReadableForm( NUMBER_NEG_999 ) );
	}

	@Test
	public void testConvert0()
	{
		assertEquals( RESULT_0, this.converter.toSlimReadableForm( NUMBER_0 ) );
	}

	@Test
	public void testConvert999()
	{
		assertEquals( RESULT_999, this.converter.toSlimReadableForm( NUMBER_999 ) );
	}

	@Test
	public void testConvert9999()
	{
		assertEquals( RESULT_9999, this.converter.toSlimReadableForm( NUMBER_9999 ) );
	}

	@Test
	public void testConvert10000()
	{
		assertEquals( RESULT_10000, this.converter.toSlimReadableForm( NUMBER_10000 ) );
	}

	@Test
	public void testConvert10500()
	{
		assertEquals( RESULT_10500, this.converter.toSlimReadableForm( NUMBER_10500 ) );
	}

	@Test
	public void testConvert155555()
	{
		assertEquals( RESULT_155555, this.converter.toSlimReadableForm( NUMBER_155555 ) );
	}

	@Test
	public void testConvert9999999()
	{
		assertEquals( RESULT_9999999, this.converter.toSlimReadableForm( NUMBER_9999999 ) );
	}

	@Test
	public void testConvert10000000()
	{
		assertEquals( RESULT_10000000, this.converter.toSlimReadableForm( NUMBER_10000000 ) );
	}

	@Test
	public void testConvert155555555()
	{
		assertEquals( RESULT_155555555, this.converter.toSlimReadableForm( NUMBER_155555555 ) );
	}
}
