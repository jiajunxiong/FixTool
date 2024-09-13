import org.FixTool.FixTagLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class FixTagLoaderTest {

    @Test
    public void testLoadFixTag() {
        FixTagLoader loader = new FixTagLoader();
        Map<Integer, String> fixTags = loader.loadFixTag("src/test/resources/fix.xml");

        assertEquals("BeginString", fixTags.get(8));
        assertEquals("BodyLength", fixTags.get(9));
        assertEquals("MsgType", fixTags.get(35));
        assertEquals("CheckSum", fixTags.get(10));
    }

    @Test
    public void testLoadRepeatingGroup() {
        FixTagLoader loader = new FixTagLoader();
        Map<Integer, List<Integer>> repeatingGroups = loader.loadRepeatingGroup("src/test/resources/fix.xml");

        assertTrue(repeatingGroups.containsKey(957));
        List<Integer> groupFields = repeatingGroups.get(957);
        assertNotNull(groupFields);
        assertTrue(groupFields.contains(958));
        assertTrue(groupFields.contains(959)); 
        assertTrue(groupFields.contains(960)); 
    }

    @Test
    public void testLoadFixTagWithInvalidFile() {
        FixTagLoader loader = new FixTagLoader();
        Map<Integer, String> fixTags = loader.loadFixTag("invalid/path/to/fix.xml");

        // Expecting an empty map due to invalid file path
        assertTrue(fixTags.isEmpty());
    }

    @Test
    public void testLoadRepeatingGroupWithInvalidFile() {
        FixTagLoader loader = new FixTagLoader();
        Map<Integer, List<Integer>> repeatingGroups = loader.loadRepeatingGroup("invalid/path/to/fix.xml");

        // Expecting an empty map due to invalid file path
        assertTrue(repeatingGroups.isEmpty());
    }
}