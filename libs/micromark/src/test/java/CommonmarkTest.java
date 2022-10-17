import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Uses the Commonmark Testcases from https://spec.commonmark.org/0.30/spec.json.
 */
public class CommonmarkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonmarkTest.class);

    @TestFactory
    public List<DynamicContainer> generateTests() throws Exception {

        var mapper = JsonMapper.builder().build();
        var testTree = mapper.readTree(getClass().getResource("/commonmark.json"));

        var bySection = new HashMap<String, List<JsonNode>>();

        for (var testNode : testTree) {
            var sectionName = testNode.path("section").asText();
            bySection.computeIfAbsent(sectionName, ignored -> new ArrayList<>())
                    .add(testNode);
        }

        return bySection.entrySet()
                .stream()
                .map(entry -> DynamicContainer.dynamicContainer(entry.getKey(), entry.getValue().stream()
                        .map(testNode -> {
                            var name = "Example " + testNode.path("example").intValue();
                            var markdown = testNode.path("markdown").asText();
                            var html = testNode.path("html").asText();

                            return DynamicTest.dynamicTest(name, () -> {
                                LOGGER.info("Markdown: {}", markdown);
                                var events = Micromark.parse(markdown);
                                System.out.println(events);

                                var options = new CompileOptions();
                                options.setAllowDangerousHtml(true);
                                options.setAllowDangerousProtocol(true);
                                var compiler = new HtmlCompiler(options);
                                assertEquals(html, compiler.compile(events));
                            });
                        }))
                )
                .toList();
    }

}
