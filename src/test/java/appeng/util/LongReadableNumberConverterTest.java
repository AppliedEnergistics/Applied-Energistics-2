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
public class LongReadableNumberConverterTest
{
	private static final long NUMBER_LOW = 999L;
	private static final long NUMBER_NORMAL = 9999L;
	private static final long NUMBER_BORDER = 10000L;
	private static final long NUMBER_KILO = 155555L;
	private static final long NUMBER_KILO_BORDER = 9999999L;
	private static final long NUMBER_MEGA_STEP = 10000000;
	private static final long NUMBER_MEGA = 155555555L;
	private static final long NUMBER_GIGA = 155555555555L;
	private static final long NUMBER_TERA = 155555555555555L;
	private static final long NUMBER_PETA = 155555555555555555L;
	private static final long NUMBER_EXA = 1555555555555555555L;
	private static final long NUMBER_NEGATIVE_LOW = -999L;
	private static final long NUMBER_NEGATIVE_NORMAL = -9999L;
	private static final long NUMBER_NEGATIVE_GIGA = -155555555555L;

	private static final String RESULT_LONG_LOW = "999";
	private static final String RESULT_LONG_NORMAL = "9999";
	private static final String RESULT_LONG_BORDER = "10K";
	private static final String RESULT_LONG_KILO = "155K";
	private static final String RESULT_LONG_KILO_BORDER = "9999K";
	private static final String RESULT_LONG_MEGA_STEP = "10M";
	private static final String RESULT_LONG_MEGA = "155M";
	private static final String RESULT_LONG_GIGA = "155G";
	private static final String RESULT_LONG_TERA = "155T";
	private static final String RESULT_LONG_PETA = "155P";
	private static final String RESULT_LONG_EXA = "1555P";
	private static final String RESULT_LONG_NEGATIVE_LOW = "-999";
	private static final String RESULT_LONG_NEGATIVE_NORMAL = "-9999";
	private static final String RESULT_LONG_NEGATIVE_GIGA = "-155G";

	private final ReadableNumberConverter converter;

	public LongReadableNumberConverterTest()
	{
		this.converter = ReadableNumberConverter.INSTANCE;
	}

	@Test
	public void testConvertLongNormal()
	{
		assertEquals( RESULT_LONG_NORMAL, this.converter.toLongHumanReadableForm( NUMBER_NORMAL ) );
	}

	@Test
	public void testConvertLongKiloBorder()
	{
		assertEquals( RESULT_LONG_KILO_BORDER, this.converter.toLongHumanReadableForm( NUMBER_KILO_BORDER ) );
	}

	@Test
	public void testConvertLongMegaStep()
	{
		assertEquals( RESULT_LONG_MEGA_STEP, this.converter.toLongHumanReadableForm( NUMBER_MEGA_STEP ) );
	}

	@Test
	public void testConvertLongLow()
	{
		assertEquals( RESULT_LONG_LOW, this.converter.toLongHumanReadableForm( NUMBER_LOW ) );
	}

	@Test
	public void testConvertLongBorder()
	{
		assertEquals( RESULT_LONG_BORDER, this.converter.toLongHumanReadableForm( NUMBER_BORDER ) );
	}

	@Test
	public void testConvertLongKilo()
	{
		assertEquals( RESULT_LONG_KILO, this.converter.toLongHumanReadableForm( NUMBER_KILO ) );
	}

	@Test
	public void testConvertLongMega()
	{
		assertEquals( RESULT_LONG_MEGA, this.converter.toLongHumanReadableForm( NUMBER_MEGA ) );
	}

	@Test
	public void testConvertLongGiga()
	{
		assertEquals( RESULT_LONG_GIGA, this.converter.toLongHumanReadableForm( NUMBER_GIGA ) );
	}

	@Test
	public void testConvertLongTera()
	{
		assertEquals( RESULT_LONG_TERA, this.converter.toLongHumanReadableForm( NUMBER_TERA ) );
	}

	@Test
	public void testConvertLongPeta()
	{
		assertEquals( RESULT_LONG_PETA, this.converter.toLongHumanReadableForm( NUMBER_PETA ) );
	}

	@Test
	public void testConvertLongExa()
	{
		assertEquals( RESULT_LONG_EXA, this.converter.toLongHumanReadableForm( NUMBER_EXA ) );
	}

	@Test
	public void testConvertLongNegativeLow()
	{
		assertEquals( RESULT_LONG_NEGATIVE_LOW, this.converter.toLongHumanReadableForm( NUMBER_NEGATIVE_LOW ) );
	}

	@Test
	public void testConvertLongNegativeNormal()
	{
		assertEquals( RESULT_LONG_NEGATIVE_NORMAL, this.converter.toLongHumanReadableForm( NUMBER_NEGATIVE_NORMAL ) );
	}

	@Test
	public void testConvertLongNegativeGiga()
	{
		assertEquals( RESULT_LONG_NEGATIVE_GIGA, this.converter.toLongHumanReadableForm( NUMBER_NEGATIVE_GIGA ) );
	}
}
