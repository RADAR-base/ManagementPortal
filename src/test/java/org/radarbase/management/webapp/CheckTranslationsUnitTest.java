package org.radarbase.management.webapp;

/**
 * Created by dverbeec on 14/12/2017.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for checking that the i18n JSON files in all languages have the same fields. This
 * test takes one language as the 'ground truth' to check against. That language is currently
 * configured to be English, but can be changed by changing the <code>BASE_LANG</code> field.
 */
class CheckTranslationsUnitTest {

    private static final String PATH = "src/main/webapp/i18n";
    private static final String BASE_LANG = "en";
    private static File baseLangPath;
    private static Map<String, List<String>> baseDictionary;

    private static final Logger log = LoggerFactory.getLogger(CheckTranslationsUnitTest.class);

    @BeforeAll
    public static void loadBaseDictionary() {
        baseLangPath = new File(PATH, BASE_LANG);
        baseDictionary = loadJsonKeysFromDirectory(baseLangPath);
    }

    @Test
    void testLanguages() {
        File basePath = new File(PATH);
        Map<String, List<String>> differencesFound = new HashMap<>();
        Arrays.stream(basePath.listFiles())
                .filter(f -> !BASE_LANG.equals(f.getName()))
                .forEach(path -> {
                    Map<String, List<String>> keys = loadJsonKeysFromDirectory(path);
                    // find differences, prepend all keys with the name of the current language
                    differencesFound.putAll(findDifferences(baseDictionary, keys).entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    e -> String.join("/", path.getName(), e.getKey()),
                                    e -> e.getValue())));
                });
        // If there were missing elements, first print out all of them and only then fail the
        // test, so we can fix all translations at once.
        if (!differencesFound.isEmpty()) {
            for (String key : differencesFound.keySet()) {
                log.error("Missing translations in {}: {}", key,
                        String.join(", ", differencesFound.get(key)));
            }
            Assertions.fail("There were missing keys in some of the translations.");
        }
    }

    /**
     * Compare the lists of corresponding keys in {@code base} and {@code toCheck}. Any elements
     * that appear in {@code base} but not in {@code toCheck} to will be added to a list in the
     * output map, with that same key.
     *
     * @param base The base map to check against
     * @param toCheck If there are missing elements in the values of corresponding keys, they
     *                will be added to the output map
     * @return The elements that are missing from <code>toCheck</code> in values of corresponding
     *     keys.
     */
    private Map<String, List<String>> findDifferences(Map<String, List<String>> base,
            Map<String, List<String>> toCheck) {
        Map<String, List<String>> result = new HashMap<>();
        for (String baseKey : base.keySet()) {
            if (!toCheck.containsKey(baseKey)) {
                // the toCheck does not even have the key, so by definition it misses all the values
                result.put(baseKey, base.get(baseKey));
            } else {
                // copy the base list
                List<String> missing = new LinkedList<>(base.get(baseKey));
                // remove elements appearing in toCheck list
                missing.removeAll(toCheck.get(baseKey));
                if (!missing.isEmpty()) {
                    result.put(baseKey, missing);
                }
            }
        }
        return result;
    }

    /**
     * Finds all JSON files in a directory and reads their field names. The different levels in the
     * JSON path are seperated by a period. The output is a map where the keys are the names of
     * the JSON files, and the values are lists of strings, where the elements are all the
     * keys in that JSON file. The given directory is NOT traversed recursively. Only files with
     * the extension '.json' are read.
     *
     * @param path The path to scan in for JSON files
     * @return a Map whose keys are absolute paths to the JSON files, and whose elements are
     *     lists of JSON paths.
     */
    private static Map<String, List<String>> loadJsonKeysFromDirectory(File path) {
        Assertions.assertTrue(path.isDirectory());
        HashMap<String, List<String>> map = new HashMap<>();
        Arrays.stream(path.listFiles())
                .filter(p -> p.getName().endsWith(".json"))
                .forEach(p -> map.put(p.getName(), loadJsonKeysFromFile(p)));
        return map;
    }

    /**
     * Read a JSON file and return all the field names. The different levels are seperated by a
     * period.
     *
     * @param file the JSON file to scan
     * @return The list of all the fields in the file
     */
    private static List<String> loadJsonKeysFromFile(File file) {
        Assertions.assertTrue(file.isFile());
        Assertions.assertTrue(file.getName().endsWith(".json"));
        ObjectMapper mapper = new ObjectMapper();
        // Adding to a LinkedList is always O(1) (never a resize necessary as in ArrayList)
        List<String> result = new LinkedList<>();
        try {
            JsonNode node = mapper.readTree(file);
            addKeysToList(node, "", result);
        } catch (IOException ex) {
            Assertions.fail(ex.getMessage());
        }
        return result;
    }

    /**
     * Recursive method for scanning a JsonNode structure and building a list of the fields
     * contained within it.
     *
     * @param currentNode The current node in the JSON structure
     * @param currentPath Path already traversed to get to <code>currentNode</code>
     * @param keyList Accumulator argument where all fields will be added to. This prevents us
     *                from having to instantiate a new list at every recursion.
     */
    private static void addKeysToList(JsonNode currentNode, String currentPath,
            List<String> keyList) {
        for (Iterator<String> iterator = currentNode.fieldNames(); iterator.hasNext();) {
            String field = iterator.next();
            JsonNode fieldValue = currentNode.get(field);
            String path = String.join(".", currentPath, field);
            if (fieldValue.isObject()) {
                addKeysToList(fieldValue, path, keyList);
            } else if (fieldValue.isTextual()) {
                keyList.add(path);
            } else {
                Assertions.fail("Encountered field that is not an object and not a string at "
                        + path);
            }
        }
    }
}
