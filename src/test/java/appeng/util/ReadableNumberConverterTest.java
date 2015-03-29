package appeng.util;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Tests for {@link appeng.util.ReadableNumberConverter}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class ReadableNumberConverterTest
{
	private static final long NUMBER_NORMAL = 55L;
	private static final long NUMBER_KILO = 155555L;
	private static final long NUMBER_MEGA = 155555555L;
	private static final long NUMBER_GIGA = 155555555555L;
	private static final long NUMBER_TERA = 155555555555555L;
	private static final long NUMBER_PETA = 155555555555555555L;
	private static final long NUMBER_EXA = 1555555555555555555L;
	private static final long NUMBER_NEGATIVE_GIGA = -155555555555L;

	private static final String RESULT_NORMAL = "55";
	private static final String RESULT_KILO = "155K";
	private static final String RESULT_MEGA = "155M";
	private static final String RESULT_GIGA = "155G";
	private static final String RESULT_TERA = "155T";
	private static final String RESULT_PETA = "155P";
	private static final String RESULT_EXA = "1E";
	private static final String RESULT_NEGATIVE_GIGA = "-155G";

	private static final String RESULT_SHORT_NORMAL = "55";
	private static final String RESULT_SHORT_KILO = ".1M";
	private static final String RESULT_SHORT_MEGA = ".1G";
	private static final String RESULT_SHORT_GIGA = ".1T";
	private static final String RESULT_SHORT_TERA = ".1P";
	private static final String RESULT_SHORT_PETA = ".1E";
	private static final String RESULT_SHORT_EXA = "1E";
	private static final String RESULT_SHORT_NEGATIVE_GIGA = "-.1T";

	private final ReadableNumberConverter converter;

	public ReadableNumberConverterTest()
	{
		this.converter = ReadableNumberConverter.INSTANCE;
	}

	@Test
	public void testConvertNormal()
	{
		assertEquals( RESULT_NORMAL, this.converter.toHumanReadableForm( NUMBER_NORMAL ) );
	}

	@Test
	public void testConvertKilo()
	{
		assertEquals( RESULT_KILO, this.converter.toHumanReadableForm( NUMBER_KILO ) );
	}

	@Test
	public void testConvertMega()
	{
		assertEquals( RESULT_MEGA, this.converter.toHumanReadableForm( NUMBER_MEGA ) );
	}

	@Test
	public void testConvertGiga()
	{
		assertEquals( RESULT_GIGA, this.converter.toHumanReadableForm( NUMBER_GIGA ) );
	}

	@Test
	public void testConvertTera()
	{
		assertEquals( RESULT_TERA, this.converter.toHumanReadableForm( NUMBER_TERA ) );
	}

	@Test
	public void testConvertPeta()
	{
		assertEquals( RESULT_PETA, this.converter.toHumanReadableForm( NUMBER_PETA ) );
	}

	@Test
	public void testConvertExa()
	{
		assertEquals( RESULT_EXA, this.converter.toHumanReadableForm( NUMBER_EXA ) );
	}

	@Test
	public void testConvertNegativeGiga()
	{
		assertEquals( RESULT_NEGATIVE_GIGA, this.converter.toHumanReadableForm( NUMBER_NEGATIVE_GIGA ) );
	}

	@Test
	public void testConvertShortNormal()
	{
		assertEquals( RESULT_SHORT_NORMAL, this.converter.toShortHumanReadableForm( NUMBER_NORMAL ) );
	}

	@Test
	public void testConvertShortKilo()
	{
		assertEquals( RESULT_SHORT_KILO, this.converter.toShortHumanReadableForm( NUMBER_KILO ) );
	}

	@Test
	public void testConvertShortMega()
	{
		assertEquals( RESULT_SHORT_MEGA, this.converter.toShortHumanReadableForm( NUMBER_MEGA ) );
	}

	@Test
	public void testConvertShortGiga()
	{
		assertEquals( RESULT_SHORT_GIGA, this.converter.toShortHumanReadableForm( NUMBER_GIGA ) );
	}

	@Test
	public void testConvertShortTera()
	{
		assertEquals( RESULT_SHORT_TERA, this.converter.toShortHumanReadableForm( NUMBER_TERA ) );
	}

	@Test
	public void testConvertShortPeta()
	{
		assertEquals( RESULT_SHORT_PETA, this.converter.toShortHumanReadableForm( NUMBER_PETA ) );
	}

	@Test
	public void testConvertShortExa()
	{
		assertEquals( RESULT_SHORT_EXA, this.converter.toShortHumanReadableForm( NUMBER_EXA ) );
	}

	@Test
	public void testConvertShortNegativeGiga()
	{
		assertEquals( RESULT_SHORT_NEGATIVE_GIGA, this.converter.toShortHumanReadableForm( NUMBER_NEGATIVE_GIGA ) );
	}
}
