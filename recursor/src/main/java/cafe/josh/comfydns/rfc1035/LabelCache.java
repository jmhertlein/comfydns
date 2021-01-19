package cafe.josh.comfydns.rfc1035;

import java.util.*;

public class LabelCache {
    private final Map<String, Integer> nameToOctetIndex;

    public LabelCache() {
        this.nameToOctetIndex = new HashMap<>();
    }

    public void addSuffixes(String name, int index) {
        List<String> suffixes = genSuffixes(name);
        for (String suffix : suffixes) {
            if(!nameToOctetIndex.containsKey(suffix)) {
                nameToOctetIndex.put(suffix, index + (name.length() - suffix.length()));
            }
        }
    }

    public static List<String> genSuffixes(String name) {
        List<String> labels = Arrays.asList(name.split("\\."));
        Collections.reverse(labels);
        List<String> suffixes = new ArrayList<>();
        for (String label : labels) {
            if(suffixes.isEmpty()) {
                suffixes.add(label);
            } else {
                suffixes.add(label + "." + suffixes.get(suffixes.size()-1));
            }
        }
        Collections.reverse(suffixes);
        return suffixes;
    }

    public Optional<LabelPointer> findBestIndex(String name) {
        List<String> suffixes = genSuffixes(name);

        for(String suffix : suffixes) {
            if(nameToOctetIndex.containsKey(suffix)) {
                return Optional.of(new LabelPointer(suffix, nameToOctetIndex.get(suffix)));
            }
        }

        return Optional.empty();
    }

    public static class LabelPointer {
        public final String name;
        public final int index;

        public LabelPointer(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}
