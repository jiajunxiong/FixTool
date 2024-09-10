import java.util.Map;
import org.FixTool.FixMessageParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FixMessageParserTest {

    @Test
    public void testParseValidFixMessage() {
        FixMessageParser parser = new FixMessageParser();
        byte[] fixMessage = "8=FIX.4.2^9=112^35=D^49=CLIENT12^56=BROKER12^34=215^52=20230905-09:30:00^11=123456^55=IBM^54=1^59=0^60=20230905-09:30:00^10=003^".getBytes();

        Map<String, String> parsedMessage = parser.parse(fixMessage);

        assertEquals("FIX.4.2", parsedMessage.get("BeginString"));
        assertEquals("112", parsedMessage.get("BodyLength"));
        assertEquals("D", parsedMessage.get("MsgType"));
        assertEquals("CLIENT12", parsedMessage.get("SenderCompID"));
        assertEquals("BROKER12", parsedMessage.get("TargetCompID"));
        assertEquals("215", parsedMessage.get("MsgSeqNum"));
        assertEquals("20230905-09:30:00", parsedMessage.get("SendingTime"));
        assertEquals("123456", parsedMessage.get("ClOrdID"));
        assertEquals("IBM", parsedMessage.get("Symbol"));
        assertEquals("1", parsedMessage.get("Side"));
        assertEquals("0", parsedMessage.get("TimeInForce"));
        assertEquals("20230905-09:30:00", parsedMessage.get("TransactTime"));
        assertEquals("003", parsedMessage.get("CheckSum"));
    }

    @Test
    public void testParseUnknownTag() {
        FixMessageParser parser = new FixMessageParser();
        byte[] fixMessage = "9999=UNKNOWN^".getBytes();

        Map<String, String> parsedMessage = parser.parse(fixMessage);

        assertEquals("UNKNOWN", parsedMessage.get("UnknownTag(9999)"));
    }

    @Test
    public void testParseEmptyMessage() {
        FixMessageParser parser = new FixMessageParser();
        byte[] fixMessage = "".getBytes();

        Map<String, String> parsedMessage = parser.parse(fixMessage);

        assertTrue(parsedMessage.isEmpty());
    }

    @Test
    public void testParseMessageWithNoDelimiter() {
        FixMessageParser parser = new FixMessageParser();
        byte[] fixMessage = "8=FIX.4.2".getBytes(); // No delimiter at the end

        Map<String, String> parsedMessage = parser.parse(fixMessage);

        assertNotEquals("FIX.4.2", parsedMessage.get("BeginString"));
    }

    @Test
    public void testParseMessageWithMultipleUnknownTags() {
        FixMessageParser parser = new FixMessageParser();
        byte[] fixMessage = "9999=UNKNOWN^9888=TEST^".getBytes();

        Map<String, String> parsedMessage = parser.parse(fixMessage);

        assertEquals("UNKNOWN", parsedMessage.get("UnknownTag(9999)"));
        assertEquals("TEST", parsedMessage.get("UnknownTag(9888)"));
    }

    @Test
    public void testSingleThreadedParsingPerformance() {
        double averageTime = FixMessageParser.benchmarkSingleThreadedParsing(1000000);

        // Ensure the performance is within reasonable bounds (adjust this based on your system)
        assertTrue(averageTime < 0.01); // below 10ms averge parsing time 
    }
}