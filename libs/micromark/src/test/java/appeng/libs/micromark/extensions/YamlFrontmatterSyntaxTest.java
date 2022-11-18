package appeng.libs.micromark.extensions;

import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class YamlFrontmatterSyntaxTest {

    @Test
    public void shouldNotSupportASingleYamlFenceThematicBreak() {
        shouldNotParseAsFrontmatter("---");
    }

    @Test
    public void shouldSupportEmptyYaml() {
        shouldParseAsFrontmatter("---\n---");
    }

    @Test
    public void shouldSupportYamlWithContent() {
        shouldParseAsFrontmatter("---\na\n\nb\n---");
    }

    @Test
    public void shouldSupportContentAfterYaml() {
        shouldParseAsFrontmatter(
                "---\na\n\nb\n---\n", "# Heading\n***\n    code"
        );
    }

    @Test
    public void shouldNotSupportAPrefixIndentBeforeAYamlOpeningFence() {
        shouldNotParseAsFrontmatter(" ---\n---");
    }

    @Test
    public void shouldNotSupportAPrefixIndentBeforeAYamlClosingFence() {
        shouldNotParseAsFrontmatter("---\n ---");
    }

    @Test
    public void shouldParseAnArbitrarySuffixAfterTheOpeningAndClosingFenceOfYaml() {
        shouldParseAsFrontmatter("---  \n---\t ", "");
    }

    @Test
    public void shouldNotSupportOtherCharactersAfterTheSuffixOnTheOpeningFenceOfYaml() {
        shouldNotParseAsFrontmatter("--- --\n---");
    }

    @Test
    public void shouldNotSupportOtherCharactersAfterTheSuffixOnTheClosingFenceOfYaml() {
        shouldNotParseAsFrontmatter("---\n--- x");
    }

    @Test
    public void shouldNotSupportAnOpeningYamlFenceOfMoreThan3Characters() {
        shouldNotParseAsFrontmatter("----\n---");
    }


    @Test
    public void shouldNotSupportAClosingYamlFenceOfMoreThan3Characters() {
        shouldNotParseAsFrontmatter("---\n----");
    }

    @Test
    public void shouldNotSupportAnOpeningYamlFenceOfLessThan3Characters() {
        shouldNotParseAsFrontmatter("--\n---");
    }

    @Test
    public void shouldNotSupportAClosingYamlFenceOfLessThan3Characters() {
        shouldNotParseAsFrontmatter("---\n--");
    }

    @Test
    public void shouldSupportContentInYaml() {
        shouldParseAsFrontmatter("---\na\nb\n---");
    }

    @Test
    public void shouldSupportBlankLinesInYaml() {
        shouldParseAsFrontmatter("---\na\n\nb\n---");
    }

    @Test
    public void shouldNotSupportYamlFrontmatterInTheMiddle() {
        shouldNotParseAsFrontmatter("# Hello\n---\na\n\nb\n---\n+++");
    }

    @Test
    public void shouldNotSupportFrontmatterWithoutClosing() {
        shouldNotParseAsFrontmatter("---\nasd");
    }

    @Test
    public void shouldNotSupportFrontmatterInAContainerList() {
        shouldNotParseAsFrontmatter("* ---\n  asd\n  ---");
    }

    @Test
    public void shouldNotSupportFrontmatterInAContainerBlockQuote() {
        shouldNotParseAsFrontmatter("> ---\n  asd\n  ---");
    }

    private static List<Tokenizer.Event> parse(String markdown) {
        var options = new ParseOptions().withExtension(YamlFrontmatterSyntax.INSTANCE);
        return Micromark.parseAndPostprocess(markdown, options);
    }

    private void shouldNotParseAsFrontmatter(String markdown) {
        var events = parse(markdown);
        var eventsNoExtension = Micromark.parseAndPostprocess(markdown, new ParseOptions());
        assertEquals(serializeEvents(eventsNoExtension), serializeEvents(events));
    }

    private void shouldParseAsFrontmatter(String frontmatter) {
        shouldParseAsFrontmatter(frontmatter, "");
    }

    private void shouldParseAsFrontmatter(String frontmatter, String additionalContent) {
        var events = parse(frontmatter + additionalContent);
        var additionalContentEvents = Micromark.parseAndPostprocess(additionalContent, new ParseOptions());

        int startOfExtraContent = -1;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).token().start.offset() == frontmatter.length()) {
                startOfExtraContent = i;
                break;
            }
        }

        List<String> frontmatterEvents;
        if (additionalContentEvents.isEmpty()) {
            assertEquals(-1, startOfExtraContent);
            frontmatterEvents = serializeEvents(events);
        } else {
            assertEquals(
                    serializeEvents(events.subList(startOfExtraContent, events.size())),
                    serializeEvents(additionalContentEvents)
            );
            frontmatterEvents = serializeEvents(events.subList(0, startOfExtraContent));
        }
        assertNotEquals(0, frontmatterEvents.size());
    }

    private List<String> serializeEvents(List<Tokenizer.Event> events) {
        return events.stream()
                .map(e -> e.type() + ":" + e.token().type + " (" + e.context().sliceSerialize(e.token()) + ")")
                .toList();
    }

}
