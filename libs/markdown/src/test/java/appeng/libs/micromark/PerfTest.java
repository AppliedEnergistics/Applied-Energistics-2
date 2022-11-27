package appeng.libs.micromark;

import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.html.HtmlCompiler;

public class PerfTest {
    public static void main(String[] args) {

        System.out.println("base");
        var then = System.currentTimeMillis();
        micromark("xxxx".repeat((int) 1e4));
        System.out.println((System.currentTimeMillis() - then));

        System.out.println("strong");
        then = System.currentTimeMillis();
        micromark("a**b".repeat((int) 1e4));
        System.out.println((System.currentTimeMillis() - then));

        System.out.println("strong/emphasis?");
        then = System.currentTimeMillis();
        micromark("a**b" + "c*".repeat((int) 1e4));
        System.out.println((System.currentTimeMillis() - then));

        System.out.println("unclosed links");
        then = System.currentTimeMillis();
        micromark("[a](b".repeat((int) 1e4));
        System.out.println((System.currentTimeMillis() - then));

        System.out.println("unclosed links (2)");
        then = System.currentTimeMillis();
        micromark("[a](<b".repeat((int) 1e4));
        System.out.println((System.currentTimeMillis() - then));

        System.out.println("tons of definitions");
        then = System.currentTimeMillis();
        micromark("[a]: u\n".repeat((int) 1e4));
        System.out.println((System.currentTimeMillis() - then));

    }

    private static String micromark(String text) {
        return new HtmlCompiler().compile(Micromark.parseAndPostprocess(text));
    }
}
