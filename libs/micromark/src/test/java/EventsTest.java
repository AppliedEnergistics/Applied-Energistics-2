import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.commonmark.Subtokenize;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EventsTest {

    @Test
    public void test() {
        var markdown = "- foo\n- bar\n+ baz\n";

        var events = Micromark.parse(markdown);
        printEvents(events);

        while (!Subtokenize.subtokenize(events)) {
            printEvents(events);
        }

        System.out.println("HTML: " + new HtmlCompiler(new CompileOptions()).compile(events));
    }

    private void printEvents(List<Tokenizer.Event> events) {
        System.out.println("EVENTS:");
        for (var event : events) {
            var type = event.type();
            var token = event.token();
            System.out.println(
                    "  " + type + " " + token.type + ",start=" + token.start.line() + ":" + token.start.column() + ",end=" + token.end.line() + ":" + token.end.column()
            );
        }
    }


}
