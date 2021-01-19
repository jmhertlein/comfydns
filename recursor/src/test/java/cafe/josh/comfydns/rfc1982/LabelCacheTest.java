package cafe.josh.comfydns.rfc1982;

import cafe.josh.comfydns.rfc1035.LabelCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LabelCacheTest {
    @Test
    public void testSuffixes() {
        List<String> suffixes = LabelCache.genSuffixes("maps.google.com");
        List<String> expected = new ArrayList<>();
        expected.add("maps.google.com");
        expected.add("google.com");
        expected.add("com");
        Assertions.assertEquals(expected, suffixes);
    }

    @Test
    public void testCache() {
        // 4maps6google3com
        LabelCache c = new LabelCache();
        Assertions.assertTrue(c.findBestIndex("maps.google.com").isEmpty());
        c.addSuffixes("maps.google.com", 0);

        Optional<LabelCache.LabelPointer> calIndex = c.findBestIndex("calendar.google.com");
        Assertions.assertTrue(calIndex.isPresent());
        Assertions.assertEquals(5, calIndex.get().index);
        c.addSuffixes("calendar.google.com", 50);

        Optional<LabelCache.LabelPointer> cal2Index = c.findBestIndex("srv2.smth.calendar.google.com");
        Assertions.assertTrue(cal2Index.isPresent());
        Assertions.assertEquals(50, cal2Index.get().index);
        c.addSuffixes("srv2.smth.calendar.google.com", 100);

        Optional<LabelCache.LabelPointer> smthIndex = c.findBestIndex("smth.calendar.google.com");
        Assertions.assertTrue(smthIndex.isPresent());
        Assertions.assertEquals(105, smthIndex.get().index);
        c.addSuffixes("smth.calendar.google.com", 200);
    }
}
