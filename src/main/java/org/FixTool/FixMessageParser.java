package org.FixTool;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;

public class FixMessageParser {

    private static final byte EQUALS_SIGN = (byte) '=';
    private static final byte DELIMITER = (byte) '^';
    // Constants for the FIX message format with delimeter '^'. For example: 8=FIX.4.2^35=D^...^10=003^
    private static final byte[] FIX_MESSAGE = "8=FIX.4.2^9=112^35=D^49=CLIENT12^56=BROKER12^34=215^52=20230905-09:30:00^11=123456^55=IBM^54=1^59=0^60=20230905-09:30:00^957=2^958=ExecutionStyle^959=String^960=Aggressive^958=ExecutionStyle^959=String^960=Passive^10=003^".getBytes(); 
    private static final int THREAD_POOL_SIZE = 10;

    // Mapping of FIX tags to their field names
    static FixTagLoader fixTag = new FixTagLoader();
    private static final Map<Integer, String> fixTags = fixTag.loadFixTag("src/main/resources/fix.xml");
    private static final Map<Integer, List<Integer>> repeatingGroup = fixTag.loadRepeatingGroup("src/main/resources/fix.xml");
    
    /**
     * Parses a FIX message from a byte array.
     *
     * @param msg the byte array containing the FIX message
     * @return a Map where the key is the FIX tag field name and the value is the corresponding value
     */
    public Map<String, String> parse(byte[] msg) {
        Map<String, String> fixMessage = new HashMap<>();
        int start = 0;
        int length = msg.length;

        while (start < length) {
            // Find the position of the equals sign
            int equalsPos = findByte(msg, start, length, EQUALS_SIGN);
            if (equalsPos == -1) break;

            // Extract the tag
            int tag = parseInt(msg, start, equalsPos);

            // Get the field name from the tag
            String fieldName = fixTags.getOrDefault(tag, "UnknownTag(" + tag + ")");

            // Find the position of the delimiter
            int delimiterPos = findByte(msg, equalsPos + 1, length, DELIMITER);
            if (delimiterPos == -1) break;

            // Extract the value
            String value = new String(msg, equalsPos + 1, delimiterPos - equalsPos - 1);

            // Check if this tag indicates a repeating group
            if(repeatingGroup.containsKey(tag)) {
                int groupCount = Integer.parseInt(value);
                List<Map<String, String>> groupList = parseRepeatingGroup(msg, delimiterPos+1, groupCount, repeatingGroup.get(tag));
                fixMessage.put(fixTags.get(tag), groupList.toString());
                start = delimiterPos + 1;
                continue;
            }

            // Store the field name-value pair in the map
            if (!isTagInRepeatingGroup(tag)) {
                fixMessage.put(fieldName, value);
            }

            // Move to the next tag-value pair
            start = delimiterPos + 1;
        }

        return fixMessage;
    }

     /**
     * Parses a repeating group from the byte array.
     *
     * @param msg the byte array containing the FIX message
     * @param start the start position of the repeating group
     * @param groupCount the number of groups to parse
     * @param groupTags the list of tags that belong to the repeating group
     * @return a List of Maps, where each Map represents one instance of the repeating group
     */
    private List<Map<String, String>> parseRepeatingGroup(byte[] msg, int start, int groupCount, List<Integer> groupTags) {
        List<Map<String, String>> groupList = new ArrayList<>();
        int length = msg.length;
        int groupLength = groupTags.size();
        int currentGroup = 0;

        while (currentGroup < groupCount && start < length) {
            Map<String, String> groupEntry = new HashMap<>();
            int tagsParsed = 0; 
            while (tagsParsed < groupLength && start < length) {
                // Find the position of the equals sign
                int equalsPos = findByte(msg, start, length, EQUALS_SIGN);
                if (equalsPos == -1) break;

                // Extract the tag
                int tag = parseInt(msg, start, equalsPos);
                // Check if the tag belongs to the repeating group
                if(!groupTags.contains(tag)) break;

                // Find the position of the delimiter
                int delimiterPos = findByte(msg, equalsPos + 1, length, DELIMITER);
                if (delimiterPos == -1) break;

                // Extract the value
                String value = new String(msg, equalsPos + 1, delimiterPos - equalsPos - 1);

                // Get the field name from the tag
                String fieldName = fixTags.getOrDefault(tag, "UnknownTag(" + tag + ")");
                groupEntry.put(fieldName, value);
                tagsParsed++;

                // Move to the next tag-value pair
                start = delimiterPos + 1;
            }
            groupList.add(groupEntry);
            currentGroup++;
        }
        return groupList;
    }

    /**
     * Checks if a given tag belongs to a repeating group.
     *
     * @param groupCountTag The tag that indicates the start of the repeating group (e.g., NoPartyIDs, NoOrders).
     * @param tag The tag to check.
     * @return true if the tag is part of the repeating group, false otherwise.
     */
    public boolean isTagInRepeatingGroup(int tag) {
        for (Map.Entry<Integer, List<Integer>> entry : repeatingGroup.entrySet()) {
            if (entry.getValue().contains(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the position of a byte in the array.
     *
     * @param array the byte array
     * @param start the start position to search from
     * @param length the length of the array
     * @param target the byte to find
     * @return the position of the byte, or -1 if not found
     */
    private int findByte(byte[] array, int start, int length, byte target) {
        for (int i = start; i < length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Parses an integer from a byte array.
     *
     * @param array the byte array
     * @param start the start position of the integer
     * @param end the end position of the integer (exclusive)
     * @return the parsed integer
     */
    private int parseInt(byte[] array, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            result = result * 10 + (array[i] - '0');
        }
        return result;
    }

    /**
     * @param iterations how many fix messages to parse
     * @return the parse duration in milliseconds
     */
    public static double benchmarkSingleThreadedParsing(int iterations) {
        FixMessageParser parser = new FixMessageParser();

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            parser.parse(FIX_MESSAGE);
        }
        long endTime = System.nanoTime();
        
        // Calculate elapsed time in milliseconds
        long elapsedTime = endTime - startTime;
        double averageTime = (double) elapsedTime / iterations / 1_000_000; // Convert to milliseconds

        System.out.printf("SingleThreaded average parsing time: %.6f ms/op%n", averageTime);
        return averageTime;
    }

    /**
     * @param iterations how many fix messages to parse
     * @return the parse duration in milliseconds
     */
    public static double benchmarkMultiThreadedParsing(int iterations) {
        List<byte[]> fixMessages = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            fixMessages.add(FIX_MESSAGE);
        }

        FixMessageParser parser = new FixMessageParser();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Create a thread pool

        List<Future<Map<String, String>>> futures = new ArrayList<>();

        // Submit parsing tasks
        long startTime = System.nanoTime();
        for (byte[] message : fixMessages) {
            Future<Map<String, String>> future = executorService.submit(new Callable<Map<String, String>>() {
                @Override
                public Map<String, String> call() {
                    return parser.parse(message); // Parse the message
                }
            });
            futures.add(future);
        }

        // Process the results
        for (Future<Map<String, String>> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions
            }
        }
        long endTime = System.nanoTime();

        // Calculate elapsed time in milliseconds
        long elapsedTime = endTime - startTime;
        executorService.shutdown(); // Shutdown the executor service
        double averageTime = (double) elapsedTime / iterations / 1_000_000; // Convert to milliseconds

        System.out.printf("MultiThreaded average parsing time: %.6f ms/op%n", averageTime);
        return averageTime;
    }

    public static void main(String[] args) {
        benchmarkSingleThreadedParsing(1000000);
        benchmarkMultiThreadedParsing(1000000);

        
        FixMessageParser parser = new FixMessageParser();        
        Map<String, String> parsedMessage = parser.parse(FIX_MESSAGE);
        System.out.println("Parsed Message: " + parsedMessage);
    }

}