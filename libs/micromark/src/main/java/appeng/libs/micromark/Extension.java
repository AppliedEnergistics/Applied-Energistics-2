package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Extension {
    public Map<Integer, List<Construct>> document = new HashMap<>();
    public Map<Integer, List<Construct>> contentInitial = new HashMap<>();
    public Map<Integer, List<Construct>> flowInitial = new HashMap<>();
    public Map<Integer, List<Construct>> flow = new HashMap<>();
    public Map<Integer, List<Construct>> string = new HashMap<>();
    public Map<Integer, List<Construct>> text = new HashMap<>();
    public List<String> nullDisable = new ArrayList<>();
    public List<Construct.Resolver> nullInsideSpan = new ArrayList<>();
    public List<Integer> nullAttentionMarkers = new ArrayList<>();
    public boolean _hiddenFootnoteSupport;
}
