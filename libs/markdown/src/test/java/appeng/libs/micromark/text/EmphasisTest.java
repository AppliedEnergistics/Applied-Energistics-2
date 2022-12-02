package appeng.libs.micromark.text;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmphasisTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          // Rule 1.
          "*foo bar*||<p><em>foo bar</em></p>||should support emphasis w/ `*`",
          "a * foo bar*||<p>a * foo bar*</p>||should not support emphasis if the opening is not left flanking (1)",
          "a*\"foo\"*||<p>a*&quot;foo&quot;*</p>||should not support emphasis if the opening is not left flanking (2b)",
          "* a *||<p>* a *</p>||should not support emphasis unicode whitespace either",
          "foo*bar*||<p>foo<em>bar</em></p>||should support intraword emphasis w/ `*` (1)",
          "5*6*78||<p>5<em>6</em>78</p>||should support intraword emphasis w/ `*` (2)",
          // Rule 2.
          "_foo bar_||<p><em>foo bar</em></p>||should support emphasis w/ `_`",
          "_ foo bar_||<p>_ foo bar_</p>||should not support emphasis if the opening is followed by whitespace",
          "a_\"foo\"_||<p>a_&quot;foo&quot;_</p>||should not support emphasis if the opening is preceded by something else and followed by punctuation",
          "foo_bar_||<p>foo_bar_</p>||should not support intraword emphasis (1)",
          "5_6_78||<p>5_6_78</p>||should not support intraword emphasis (2)",
          "пристаням_стремятся_||<p>пристаням_стремятся_</p>||should not support intraword emphasis (3)",
          "aa_\"bb\"_cc||<p>aa_&quot;bb&quot;_cc</p>||should not support emphasis if the opening is right flanking and the closing is left flanking",
          "foo-_(bar)_||<p>foo-<em>(bar)</em></p>||should support emphasis if the opening is both left and right flanking, if it’s preceded by punctuation",
          // Rule 3.
          "_foo*||<p>_foo*</p>||should not support emphasis if opening and closing markers don’t match",
          "*foo bar *||<p>*foo bar *</p>||should not support emphasis w/ `*` if the closing markers are preceded by whitespace",
          "*foo bar^n*||<p>*foo bar^n*</p>||should not support emphasis w/ `*` if the closing markers are preceded by a line break (also whitespace)",
          "*(*foo)||<p>*(*foo)</p>||should not support emphasis w/ `*` if the closing markers are not right flanking",
          "*(*foo*)*||<p><em>(<em>foo</em>)</em></p>||should support nested emphasis",
          // Rule 4.

          "_foo bar _||<p>_foo bar _</p>||should not support emphasis if the closing `_` is preceded by whitespace",
          "_(_foo)||<p>_(_foo)</p>||should not support emphasis w/ `_` if the closing markers are not right flanking",
          "_(_foo_)_||<p><em>(<em>foo</em>)</em></p>||should support nested emphasis w/ `_`",
          "_foo_bar||<p>_foo_bar</p>||should not support intraword emphasis w/ `_` (1)",
          "_пристаням_стремятся||<p>_пристаням_стремятся</p>||should not support intraword emphasis w/ `_` (2)",
          "_foo_bar_baz_||<p><em>foo_bar_baz</em></p>||should not support intraword emphasis w/ `_` (3)",
          "_(bar)_.||<p><em>(bar)</em>.</p>||should support emphasis if the opening is both left and right flanking, if it’s followed by punctuation",
          // Rule 5.
          "**foo bar**||<p><strong>foo bar</strong></p>||should support strong emphasis",
          "** foo bar**||<p>** foo bar**</p>||should not support strong emphasis if the opening is followed by whitespace",
          "a**\"foo\"**||<p>a**&quot;foo&quot;**</p>||should not support strong emphasis if the opening is preceded by something else and followed by punctuation",
          "foo**bar**||<p>foo<strong>bar</strong></p>||should support strong intraword emphasis",
          // Rule 6.
          "__foo bar__||<p><strong>foo bar</strong></p>||should support strong emphasis w/ `_`",
          "__ foo bar__||<p>__ foo bar__</p>||should not support strong emphasis if the opening is followed by whitespace",
          "__^nfoo bar__||<p>__^nfoo bar__</p>||should not support strong emphasis if the opening is followed by a line ending (also whitespace)",
          "a__\"foo\"__||<p>a__&quot;foo&quot;__</p>||should not support strong emphasis if the opening is preceded by something else and followed by punctuation",
          "foo__bar__||<p>foo__bar__</p>||should not support strong intraword emphasis w/ `_` (1)",
          "5__6__78||<p>5__6__78</p>||should not support strong intraword emphasis w/ `_` (2)",
          "пристаням__стремятся__||<p>пристаням__стремятся__</p>||should not support strong intraword emphasis w/ `_` (3)",
          "__foo, __bar__, baz__||<p><strong>foo, <strong>bar</strong>, baz</strong></p>||should support nested strong emphasis",
          "foo-__(bar)__||<p>foo-<strong>(bar)</strong></p>||should support strong emphasis if the opening is both left and right flanking, if it’s preceded by punctuation",
          // Rule 7.
          "**foo bar **||<p>**foo bar **</p>||should not support strong emphasis w/ `*` if the closing is preceded by whitespace",
          "**(**foo)||<p>**(**foo)</p>||should not support strong emphasis w/ `*` if the closing is preceded by punctuation and followed by something else",
          "*(**foo**)*||<p><em>(<strong>foo</strong>)</em></p>||should support strong emphasis in emphasis",
          "**Gomphocarpus (*Gomphocarpus physocarpus*, syn.^n*Asclepias physocarpa*)**||<p><strong>Gomphocarpus (<em>Gomphocarpus physocarpus</em>, syn.^n<em>Asclepias physocarpa</em>)</strong></p>||should support emphasis in strong emphasis (1)",
          "**foo \"*bar*\" foo**||<p><strong>foo &quot;<em>bar</em>&quot; foo</strong></p>||should support emphasis in strong emphasis (2)",
          "**foo**bar||<p><strong>foo</strong>bar</p>||should support strong intraword emphasis",
          // Rule 8.
          "__foo bar __||<p>__foo bar __</p>||should not support strong emphasis w/ `_` if the closing is preceded by whitespace",
          "__(__foo)||<p>__(__foo)</p>||should not support strong emphasis w/ `_` if the closing is preceded by punctuation and followed by something else",
          "_(__foo__)_||<p><em>(<strong>foo</strong>)</em></p>||should support strong emphasis w/ `_` in emphasis",
          "__foo__bar||<p>__foo__bar</p>||should not support strong intraword emphasis w/ `_` (1)",
          "__пристаням__стремятся||<p>__пристаням__стремятся</p>||should not support strong intraword emphasis w/ `_` (2)",
          "__foo__bar__baz__||<p><strong>foo__bar__baz</strong></p>||should not support strong intraword emphasis w/ `_` (3)",
          "__(bar)__.||<p><strong>(bar)</strong>.</p>||should support strong emphasis if the opening is both left and right flanking, if it’s followed by punctuation",
          // Rule 9.
          "*foo [bar](/url)*||<p><em>foo <a href=\"/url\">bar</a></em></p>||should support content in emphasis",
          "*foo^nbar*||<p><em>foo^nbar</em></p>||should support line endings in emphasis",
          "_foo __bar__ baz_||<p><em>foo <strong>bar</strong> baz</em></p>||should support nesting emphasis and strong (1)",
          "_foo _bar_ baz_||<p><em>foo <em>bar</em> baz</em></p>||should support nesting emphasis and strong (2)",
          "__foo_ bar_||<p><em><em>foo</em> bar</em></p>||should support nesting emphasis and strong (3)",
          "*foo *bar**||<p><em>foo <em>bar</em></em></p>||should support nesting emphasis and strong (4)",
          "*foo **bar** baz*||<p><em>foo <strong>bar</strong> baz</em></p>||should support nesting emphasis and strong (5)",
          "*foo**bar**baz*||<p><em>foo<strong>bar</strong>baz</em></p>||should support nesting emphasis and strong (6)",
          "*foo**bar*||<p><em>foo**bar</em></p>||should not support adjacent emphasis in certain cases",
          "***foo** bar*||<p><em><strong>foo</strong> bar</em></p>||complex (1)",
          "*foo **bar***||<p><em>foo <strong>bar</strong></em></p>||complex (2)",
          "*foo**bar***||<p><em>foo<strong>bar</strong></em></p>||complex (3)",
          "foo***bar***baz||<p>foo<em><strong>bar</strong></em>baz</p>||complex (a)",
          "foo******bar*********baz||<p>foo<strong><strong><strong>bar</strong></strong></strong>***baz</p>||complex (b)",
          "*foo **bar *baz* bim** bop*||<p><em>foo <strong>bar <em>baz</em> bim</strong> bop</em></p>||should support indefinite nesting of emphasis (1)",
          "*foo [*bar*](/url)*||<p><em>foo <a href=\"/url\"><em>bar</em></a></em></p>||should support indefinite nesting of emphasis (2)",
          "** is not an empty emphasis||<p>** is not an empty emphasis</p>||should not support empty emphasis",
          "**** is not an empty emphasis||<p>**** is not an empty emphasis</p>||should not support empty strong emphasis",
          // Rule 10.
          "**foo [bar](/url)**||<p><strong>foo <a href=\"/url\">bar</a></strong></p>||should support content in strong emphasis",
          "**foo^nbar**||<p><strong>foo^nbar</strong></p>||should support line endings in emphasis",
          "__foo _bar_ baz__||<p><strong>foo <em>bar</em> baz</strong></p>||should support nesting emphasis and strong (1)",
          "__foo __bar__ baz__||<p><strong>foo <strong>bar</strong> baz</strong></p>||should support nesting emphasis and strong (2)",
          "____foo__ bar__||<p><strong><strong>foo</strong> bar</strong></p>||should support nesting emphasis and strong (3)",
          "**foo **bar****||<p><strong>foo <strong>bar</strong></strong></p>||should support nesting emphasis and strong (4)",
          "**foo *bar* baz**||<p><strong>foo <em>bar</em> baz</strong></p>||should support nesting emphasis and strong (5)",
          "**foo*bar*baz**||<p><strong>foo<em>bar</em>baz</strong></p>||should support nesting emphasis and strong (6)",
          "***foo* bar**||<p><strong><em>foo</em> bar</strong></p>||should support nesting emphasis and strong (7)",
          "**foo *bar***||<p><strong>foo <em>bar</em></strong></p>||should support nesting emphasis and strong (8)",
          "**foo *bar **baz**^nbim* bop**||<p><strong>foo <em>bar <strong>baz</strong>^nbim</em> bop</strong></p>||should support indefinite nesting of emphasis (1)",
          "**foo [*bar*](/url)**||<p><strong>foo <a href=\"/url\"><em>bar</em></a></strong></p>||should support indefinite nesting of emphasis (2)",
          "__ is not an empty emphasis||<p>__ is not an empty emphasis</p>||should not support empty emphasis",
          "____ is not an empty emphasis||<p>____ is not an empty emphasis</p>||should not support empty strong emphasis",
          // Rule 11.
          "foo ***||<p>foo ***</p>||should not support emphasis around the same marker",
          "foo *\\**||<p>foo <em>*</em></p>||should support emphasis around an escaped marker",
          "foo *_*||<p>foo <em>_</em></p>||should support emphasis around the other marker",
          "foo *****||<p>foo *****</p>||should not support strong emphasis around the same marker",
          "foo **\\***||<p>foo <strong>*</strong></p>||should support strong emphasis around an escaped marker",
          "foo **_**||<p>foo <strong>_</strong></p>||should support strong emphasis around the other marker",
          "**foo*||<p>*<em>foo</em></p>||should support a superfluous marker at the start of emphasis",
          "*foo**||<p><em>foo</em>*</p>||should support a superfluous marker at the end of emphasis",
          "***foo**||<p>*<strong>foo</strong></p>||should support a superfluous marker at the start of strong",
          "****foo*||<p>***<em>foo</em></p>||should support multiple superfluous markers at the start of strong",
          "**foo***||<p><strong>foo</strong>*</p>||should support a superfluous marker at the end of strong",
          "*foo****||<p><em>foo</em>***</p>||should support multiple superfluous markers at the end of strong",
          // Rule 12.
          "foo ___||<p>foo ___</p>||should not support emphasis around the same marker",
          "foo _\\__||<p>foo <em>_</em></p>||should support emphasis around an escaped marker",
          "foo _X_||<p>foo <em>X</em></p>||should support emphasis around the other marker",
          "foo _____||<p>foo _____</p>||should not support strong emphasis around the same marker",
          "foo __\\___||<p>foo <strong>_</strong></p>||should support strong emphasis around an escaped marker",
          "foo __X__||<p>foo <strong>X</strong></p>||should support strong emphasis around the other marker",
          "__foo_||<p>_<em>foo</em></p>||should support a superfluous marker at the start of emphasis",
          "_foo__||<p><em>foo</em>_</p>||should support a superfluous marker at the end of emphasis",
          "___foo__||<p>_<strong>foo</strong></p>||should support a superfluous marker at the start of strong",
          "____foo_||<p>___<em>foo</em></p>||should support multiple superfluous markers at the start of strong",
          "__foo___||<p><strong>foo</strong>_</p>||should support a superfluous marker at the end of strong",
          "_foo____||<p><em>foo</em>___</p>||should support multiple superfluous markers at the end of strong",
          // Rule 13.
          "**foo**||<p><strong>foo</strong></p>||should support strong w/ `*`",
          "*_foo_*||<p><em><em>foo</em></em></p>||should support emphasis directly in emphasis w/ `_` in `*`",
          "__foo__||<p><strong>foo</strong></p>||should support strong w/ `_`",
          "_*foo*_||<p><em><em>foo</em></em></p>||should support emphasis directly in emphasis w/ `*` in `_`",
          "****foo****||<p><strong><strong>foo</strong></strong></p>||should support strong emphasis directly in strong emphasis w/ `*`",
          "____foo____||<p><strong><strong>foo</strong></strong></p>||should support strong emphasis directly in strong emphasis w/ `_`",
          "******foo******||<p><strong><strong><strong>foo</strong></strong></strong></p>||should support indefinite strong emphasis",
          // Rule 14.
          "***foo***||<p><em><strong>foo</strong></em></p>||should support strong directly in emphasis w/ `*`",
          "___foo___||<p><em><strong>foo</strong></em></p>||should support strong directly in emphasis w/ `_`",
          // Rule 15.
          "*foo _bar* baz_||<p><em>foo _bar</em> baz_</p>||should not support mismatched emphasis",
          "*foo __bar *baz bim__ bam*||<p><em>foo <strong>bar *baz bim</strong> bam</em></p>||should not support mismatched strong emphasis",
          // Rule 16.
          "**foo **bar baz**||<p>**foo <strong>bar baz</strong></p>||should not shortest strong possible",
          "*foo *bar baz*||<p>*foo <em>bar baz</em></p>||should not shortest emphasis possible",
          // Rule 17.
          "*[bar*](/url)||<p>*<a href=\"/url\">bar*</a></p>||should not mismatch inside links (1)",
          "_[bar_](/url)||<p>_<a href=\"/url\">bar_</a></p>||should not mismatch inside links (1)",
          "*a `*`*||<p><em>a <code>*</code></em></p>||should not end emphasis inside code (1)",
          "_a `_`_||<p><em>a <code>_</code></em></p>||should not end emphasis inside code (2)",
          "**a<http://foo.bar/?q=**>||<p>**a<a href=\"http://foo.bar/?q=**\">http://foo.bar/?q=**</a></p>||should not end strong emphasis inside autolinks (1)",
          "__a<http://foo.bar/?q=__>||<p>__a<a href=\"http://foo.bar/?q=__\">http://foo.bar/?q=__</a></p>||should not end strong emphasis inside autolinks (2)",
  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }
    @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
    "*<img src=\"foo\" title=\"*\"/>||<p>*<img src=\"foo\" title=\"*\"/></p>||should not end inside HTML",
    "*<img src=\"foo\" title=\"*\"/>||<p>*<img src=\"foo\" title=\"*\"/></p>||should not end emphasis inside HTML",
    "**<a href=\"**\">||<p>**<a href=\"**\"></p>||should not end strong inside HTML (1)",
    "__<a href=\"__\">||<p>__<a href=\"__\"></p>||should not end strong inside HTML (2)",
  })
  public void testGeneratedHtmlUnsafe(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
  }

  @Test
  public void testDisabled() {
    TestUtil.assertGeneratedHtmlWithDisabled("*a*", "<p>*a*</p>", "should support turning off attention", "attention");
  }

  }
