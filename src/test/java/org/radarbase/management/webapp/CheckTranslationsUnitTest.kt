package org.radarbase.management.webapp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by dverbeec on 14/12/2017.
 */
/**
 * Test class for checking that the i18n JSON files in all languages have the same fields. This
 * test takes one language as the 'ground truth' to check against. That language is currently
 * configured to be English, but can be changed by changing the `BASE_LANG` field.
 */
//TODO reimplement in proper kotlin
internal class CheckTranslationsUnitTest {
//    @Test
//    fun testLanguages() {
//        val basePath = File(PATH)
//        val differencesFound: MutableMap<String, List<String>> = HashMap()
//        Arrays.stream<File>(basePath.listFiles())
//            .filter { f: File -> BASE_LANG != f.getName() }
//            .forEach { path: File ->
//                val keys = loadJsonKeysFromDirectory(path)
//                // find differences, prepend all keys with the name of the current language
//                differencesFound.putAll(
//                    findDifferences(baseDictionary, keys).entries
//                        .stream()
//                        .collect(
//                            Collectors.toMap<Entry<String, List<String>>, String, List<String>>(
//                                Function<Entry<String, List<String>>, String> { e: Entry<String, List<String>> ->
//                                    java.lang.String.join(
//                                        "/",
//                                        path.getName(),
//                                        e.key
//                                    )
//                                },
//                                Function<Entry<String, List<String>>, List<String>> { (key, value) -> java.util.Map.Entry.value })
//                        )
//                )
//            }
//        Assertions.assertEquals(
//            java.util.Map.of<Any, Any>(), differencesFound,
//            "There were missing keys in some of the translations."
//        )
//    }

    /**
     * Compare the lists of corresponding keys in `base` and `toCheck`. Any elements
     * that appear in `base` but not in `toCheck` to will be added to a list in the
     * output map, with that same key.
     *
     * @param base The base map to check against
     * @param toCheck If there are missing elements in the values of corresponding keys, they
     * will be added to the output map
     * @return The elements that are missing from `toCheck` in values of corresponding
     * keys.
     */
    private fun findDifferences(
        base: Map<String, List<String>>?,
        toCheck: Map<String, List<String>>
    ): Map<String, List<String>> {
        val result: MutableMap<String, List<String>> = HashMap()
        for (baseKey in base!!.keys) {
            if (!toCheck.containsKey(baseKey)) {
                // the toCheck does not even have the key, so by definition it misses all the values
                result[baseKey] = base[baseKey]!!
            } else {
                // copy the base list
                val missing: MutableList<String> = LinkedList(base[baseKey])
                // remove elements appearing in toCheck list
                missing.removeAll(toCheck[baseKey]!!)
                if (!missing.isEmpty()) {
                    result[baseKey] = missing
                }
            }
        }
        return result
    }

    companion object {
        private const val PATH = "src/main/webapp/i18n"
        private const val BASE_LANG = "en"
        private var baseDictionary: Map<String, List<String>>? = null
        @BeforeAll
        fun loadBaseDictionary() {
            val baseLangPath = File(PATH, BASE_LANG)
            baseDictionary = loadJsonKeysFromDirectory(baseLangPath)
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
         * lists of JSON paths.
         */
        private fun loadJsonKeysFromDirectory(path: File): Map<String, List<String>> {
            Assertions.assertTrue(path.isDirectory())
            val map = HashMap<String, List<String>>()
            Arrays.stream(path.listFiles())
                .filter { p: File -> p.getName().endsWith(".json") }
                .forEach { p: File -> map[p.getName()] = loadJsonKeysFromFile(p) }
            return map
        }

        /**
         * Read a JSON file and return all the field names. The different levels are seperated by a
         * period.
         *
         * @param file the JSON file to scan
         * @return The list of all the fields in the file
         */
        private fun loadJsonKeysFromFile(file: File): List<String> {
            Assertions.assertTrue(file.isFile())
            Assertions.assertTrue(file.getName().endsWith(".json"))
            val mapper = ObjectMapper()
            // Adding to a LinkedList is always O(1) (never a resize necessary as in ArrayList)
            val result: MutableList<String> = LinkedList()
            try {
                val node = mapper.readTree(file)
                addKeysToList(node, "", result)
            } catch (ex: IOException) {
                Assertions.fail<Any>(ex.message)
            }
            return result
        }

        /**
         * Recursive method for scanning a JsonNode structure and building a list of the fields
         * contained within it.
         *
         * @param currentNode The current node in the JSON structure
         * @param currentPath Path already traversed to get to `currentNode`
         * @param keyList Accumulator argument where all fields will be added to. This prevents us
         * from having to instantiate a new list at every recursion.
         */
        private fun addKeysToList(
            currentNode: JsonNode, currentPath: String,
            keyList: MutableList<String>
        ) {
            val iterator = currentNode.fieldNames()
            while (iterator.hasNext()) {
                val field = iterator.next()
                val fieldValue = currentNode[field]
                val path = java.lang.String.join(".", currentPath, field)
                if (fieldValue.isObject) {
                    addKeysToList(fieldValue, path, keyList)
                } else if (fieldValue.isTextual) {
                    keyList.add(path)
                } else {
                    Assertions.fail<Any>(
                        "Encountered field that is not an object and not a string at "
                                + path
                    )
                }
            }
        }
    }
}
