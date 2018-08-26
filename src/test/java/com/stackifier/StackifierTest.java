package com.stackifier;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by Talal Ahmed on 08/02/2018
 */
public class StackifierTest {

    private static final String SAMPLE_STACKTRACE_FILE = "stacktrace_sample";

    private String stacktrace;

    @Before
    public void setup() throws IOException {
        String path = getClass()
                .getClassLoader()
                .getResource(SAMPLE_STACKTRACE_FILE)
                .getPath();

        this.stacktrace = read(path);
    }

    @Test
    public void testBuilder() {
        Stackifier stackifier = new Stackifier.Builder()
                .add("org.apache.commons")
                .add("java", "sun", "junit")
                .use(new StackTraceDeserializer())
                .get();

        assertNotNull(stackifier.getDeserializer());

        List<String> libraries = stackifier.getLibraries();
        assertTrue(libraries.contains("org.apache.commons"));
        assertTrue(libraries.contains("java"));
        assertTrue(libraries.contains("sun"));
        assertTrue(libraries.contains("junit"));
    }

    @Test
    public void testGetGroups() {
        Stackifier stackifier = new Stackifier.Builder()
                .add("java", "sun", "junit", "org.apache.commons", "org.pitest")
                .get();

        Stackified stackified = stackifier.stackify(stacktrace);
        List<Group> groups = stackified.getGroups();

        assertEquals(groups.size(), 8);
    }

    @Test
    public void testGetGroupsWithName() {
        Stackifier stackifier = new Stackifier.Builder()
                .add("java")
                .get();

        Stackified stackified = stackifier.stackify(stacktrace);
        List<Group> javaGroups = stackified.getGroups("java");

        assertEquals(javaGroups.size(), 3);
    }

    @Test
    public void testGetGroupWithGroupId() {
        Stackifier stackifier = new Stackifier.Builder()
                .add("org.apache.commons")
                .get();

        Stackified stackified = stackifier.stackify(stacktrace);
        Optional<Group> javaGroups = stackified.getGroup(3);

        assertNotNull(javaGroups.get());
        assertEquals(javaGroups.get().getName(), "org.apache.commons");
    }

    @Test
    public void testGetElementsWithGroupId() {
        Stackifier stackifier = new Stackifier.Builder()
                .add("junit")
                .get();

        Stackified stackified = stackifier.stackify(stacktrace);
        List<StackTraceElement> javaGroupElements = stackified.getElements(10);

        assertNotNull(javaGroupElements);
        assertEquals(javaGroupElements.size(), 8);
    }

    @Test
    public void testPrettyPrint() {
        Stackifier stackifier = new Stackifier.Builder()
                .add("java", "sun", "junit", "org.apache.commons", "org.pitest")
                .get();

        Stackified stackified = stackifier.stackify(stacktrace);
        stackified.prettyPrint();
    }

    private static String read(String path) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        }

        return sb.toString();
    }
}
