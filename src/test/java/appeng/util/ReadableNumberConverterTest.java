package appeng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ReadableNumberConverterTest {
    @ParameterizedTest
    @ValueSource(longs = { -999999L, -9999L, -1L })
    void testThrowsOnNegativeLong(long number) {
        assertThrows(IllegalArgumentException.class, () -> ReadableNumberConverter.format(number, 3));
    }

    @BeforeAll
    public static void setLocale() {
        Locale.setDefault(Locale.ROOT);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "3|0|0",
            "3|999|999",
            "3|1000|1K",
            "3|9999|9K",
            "3|10000|10K",
            "3|10500|10K",
            "3|155555|.1M",
            "3|9999999|9M",
            "3|10000000|10M",
            "3|155555555|.1G",
            "4|0|0",
            "4|999|999",
            "4|1000|1000",
            "4|9999|9999",
            "4|10000|10K",
            "4|10500|10K",
            "4|155555|155K",
            "4|9999999|9.9M",
            "4|10000000|10M",
            "4|155555555|155M",
    }, delimiter = '|')
    public void testFormatLong(int width, long number, String expected) {
        assertEquals(expected, ReadableNumberConverter.format(number, width));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "3|0.0001|~0",
            "3|0.001|~0",
            "3|0.01|.01",
            "3|0.1|.1",
            "3|0.12|.12",
            "3|0.123|.12",
            "3|0.1234|.12",
            "3|0.4567|.45",
            "3|0.789799|.78",
            "3|1.01|~1",
            "3|9.12345|9.1",
            "3|10.1|~10",
            "3|99.12345|~99",
            "3|999.12345|999",
            "3|1000.12345|1K",
            "3|9999.12345|9K",
            "3|10000.12345|10K",
            "3|10500.12345|10K",
            "3|155555.12345|.1M",
            "3|9999999.12345|9M",
            "3|10000000.12345|10M",
            "3|155555555.12345|.1G",
            "4|0.1|.1",
            "4|0.12|.12",
            "4|0.123|.123",
            "4|0.1234|.123",
            "4|0.4567|.456",
            "4|0.789799|.789",
            "4|1.001|~1",
            "4|1.01|1.01",
            "4|9.12345|9.12",
            "4|10.01|~10",
            "4|10.1|10.1",
            "4|99.12345|99.1",
            "4|999.12345|~999",
            "4|1000.12345|1000",
            "4|1100.12345|1100",
            "4|9999.12345|9999",
            "4|10000.12345|10K",
            "4|10500.12345|10K",
            "4|155555.12345|155K",
            "4|9999999.12345|9.9M",
            "4|10000000.12345|10M",
            "4|155555555.12345|155M",
    }, delimiter = '|')
    public void testFormatDouble(int width, double number, String expected) {
        assertEquals(expected, ReadableNumberConverter.format(number, width));
    }
}
