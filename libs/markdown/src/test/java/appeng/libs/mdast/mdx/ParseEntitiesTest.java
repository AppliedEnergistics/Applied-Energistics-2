package appeng.libs.mdast.mdx;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseEntitiesTest {

    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            foo &amp; bar | foo & bar | should work on a named reference
            foo &#123; bar | foo { bar | should work on a decimal reference
            foo &#x123; bar | foo ģ bar | should work on a hexadecimal reference
            &amp; bar | & bar | should work when the reference is initial
            foo &amp; | foo & | should work when the reference is final
            foo &amp; | foo & | should work when the reference is final
            &amp;&#123;&#x123; | &{ģ | should work for adjacent entities
            foo &amp bar | foo &amp bar | should ignore unterminated named references
            foo &#123 bar | foo &#123 bar | should fail when numerical and without terminal semicolon
            Foo &\tbar | Foo &\tbar | should work on an ampersand followed by a tab
            Foo &\\nbar | Foo &\\nbar | should work on an ampersand followed by a newline
            Foo &\fbar | Foo &\fbar | should work on an ampersand followed by a form-feed
            Foo & bar | Foo & bar | should work on an ampersand followed by a space
            Foo &<bar | Foo &<bar | should work on an ampersand followed by a `<`
            Foo &&bar | Foo &&bar | should work on an ampersand followed by another ampersand
            Foo & | Foo & | should work on an ampersand followed by EOF
            Foo &" | Foo &" | should work on an ampersand followed by an additional character
            Foo &bar; baz | Foo &bar; baz | should ignore unknown named reference
            Foo &#xD800; baz | Foo \uFFFD baz | should warn when prohibited
            Foo &#x1F44D; baz | Foo 👍 baz | should work when resulting in multiple characters
            &#0; | \uFFFD | should account for special replacements mentioned in HTML
            &; | &; | should ignore empty reference
            """, delimiterString = " | ")
    void testParseEntities(String input, String expected, String message) {
        input = input.replace("\\n", "\n");
        expected = expected.replace("\\n", "\n");
        assertEquals(expected, ParseEntities.parseEntities(input));
    }

}