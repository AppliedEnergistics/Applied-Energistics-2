package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Extension {
    Map<Integer, List<Construct>> document = Map.of();
    Map<Integer, List<Construct>> contentInitial = Map.of();
    Map<Integer, List<Construct>> flowInitial = Map.of();
    Map<Integer, List<Construct>> flow = Map.of();
    Map<Integer, List<Construct>> string = Map.of();
    Map<Integer, List<Construct>> text = Map.of();
    List<String> nullDisable = new ArrayList<>();
    List<Construct.Resolver> nullInsideSpan = new ArrayList<>();
    List<Integer> nullAttentionMarkers = new ArrayList<>();
}
