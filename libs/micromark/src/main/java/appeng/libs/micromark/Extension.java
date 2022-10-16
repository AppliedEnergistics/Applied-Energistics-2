package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Extension {
    public Map<Integer, List<Construct>> document = Map.of();
    public Map<Integer, List<Construct>> contentInitial = Map.of();
    public Map<Integer, List<Construct>> flowInitial = Map.of();
    public Map<Integer, List<Construct>> flow = Map.of();
    public Map<Integer, List<Construct>> string = Map.of();
    public Map<Integer, List<Construct>> text = Map.of();
    public List<String> nullDisable = new ArrayList<>();
    public List<Construct.Resolver> nullInsideSpan = new ArrayList<>();
    public List<Integer> nullAttentionMarkers = new ArrayList<>();
    public boolean _hiddenFootnoteSupport;
}
