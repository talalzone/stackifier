package com.stackifier;

import com.stackifier.exceptions.NoStacktraceException;
import com.stackifier.interfaces.Deserializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Talal Ahmed on 08/02/2018
 */
public class Stackifier {

    private static final String DEFAULT_GROUP_NAME = "other";

    private final List<String> libraries;
    private final Deserializer deserializer;

    public Stackifier(List<String> libraries) {
        this(libraries, new StackTraceDeserializer());
    }

    public Stackifier(List<String> libraries, Deserializer deserializer) {
        this.libraries = libraries;
        this.deserializer = deserializer;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public Deserializer getDeserializer() {
        return deserializer;
    }

    public Stackified stackify(String stacktrace) {
        if (stacktrace == null || stacktrace.isEmpty()) {
            throw new NoStacktraceException();
        }

        Throwable throwable = this.deserializer.deserialize(stacktrace);
        Stackified stackified = new Stackified(stacktrace, throwable.toString(), throwable);

        int groupId = 0;
        Group group = new Group(groupId, DEFAULT_GROUP_NAME);

        StackTraceElement[] elements = throwable.getStackTrace();
        for (StackTraceElement element : elements) {

            if (!element.getClassName().startsWith(group.getName())) {
                String newGroup = findGroup(element.getClassName());
                group = new Group(++groupId, newGroup);
                stackified.addGroup(group);
            }

            group.addElement(element);
        }

        return stackified;
    }

    private String findGroup(String className) {
        Optional<String> lib = this.libraries
                .stream()
                .filter(className::startsWith)
                .findFirst();

        return lib.orElse(DEFAULT_GROUP_NAME);
    }

    public static class Builder {
        private Deserializer deserializer;
        private List<String> libraries = new ArrayList<>();
        private boolean libraryDetection;

        public Builder use(Deserializer deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        public Builder libraryDetection(boolean auto) {
            this.libraryDetection = auto;
            return this;
        }

        public Builder add(String library) {
            this.libraries.add(library);
            return this;
        }

        public Builder add(String... libraries) {
            this.libraries.addAll(Arrays.asList(libraries));
            return this;
        }

        public Stackifier get() {
            if (this.deserializer == null) {
                this.deserializer = new StackTraceDeserializer();
            }
            return new Stackifier(libraries, deserializer);
        }
    }
}
