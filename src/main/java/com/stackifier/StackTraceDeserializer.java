package com.stackifier;

import com.stackifier.interfaces.Deserializer;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Talal Ahmed on 08/02/2018
 * <p>
 * Taken from {@link <a href="https://github.com/abertschi/stacktrace-unserialize/blob/master/src/main/java/ch/abertschi/unserialize/StackTraceUnserialize.java"/>}
 */
public class StackTraceDeserializer implements Deserializer {

    private static final String VALID_CLASSNAME = "[^\\(\\)\\\\\\[\\] ]*";

    private static final Pattern PATTERN_EXCEPTION_AND_MESSAGE = Pattern.compile("(?=[^(Caused by:)])([^: ]*:)(.*)");

    private static final Pattern PATTERN_STACKTRACE = Pattern.compile("at ([^(: \\t\\r]*)?\\((.*?)\\)(\\[(.*?)\\])?");

    public Throwable deserialize(final String stacktrace) {
        Map<Integer, StackTraceElement> elements = parseStackTraceElements(stacktrace);

        TreeMap<Integer, CauseElement> causes = new TreeMap<>(Collections.reverseOrder());
        causes.putAll(parseExceptionAndMessage(stacktrace));

        if (causes.isEmpty() && elements.isEmpty()) {
            String msg = String.format("No valid stacktrace given. No exception or stacktrace element found. "
                    + "Input: %s", stacktrace == null ? "null" : stacktrace.isEmpty() ? "<empty>" : stacktrace);

            throw new IllegalArgumentException(msg);
        } else {
            if (causes.isEmpty()) {
                causes.put(0, new CauseElement(RuntimeException.class.getCanonicalName(), ""));
            }
            return buildThrowable(causes, elements);
        }
    }

    protected static Throwable buildThrowable(Map<Integer, CauseElement> causes, Map<Integer, StackTraceElement> elements) {
        Throwable rootThrowable = null;
        Integer lastStackTraceIndex = Integer.MAX_VALUE;
        for (Map.Entry<Integer, CauseElement> cause : causes.entrySet()) {
            Throwable throwable = lookupThrowable(cause.getValue().getType(), cause.getValue().getMessage());

            TreeMap<Integer, StackTraceElement> stacktrace = new TreeMap<>();
            for (Map.Entry<Integer, StackTraceElement> subElement : elements.entrySet()) {
                if (subElement.getKey() > cause.getKey() && subElement.getKey() < lastStackTraceIndex) {
                    stacktrace.put(subElement.getKey(), subElement.getValue());
                }
            }
            if (rootThrowable != null) {
                throwable.initCause(rootThrowable);
            }
            throwable.setStackTrace(stacktrace.values().toArray(new StackTraceElement[0]));

            lastStackTraceIndex = cause.getKey();
            rootThrowable = throwable;
        }
        return rootThrowable;
    }

    protected static Map<Integer, CauseElement> parseExceptionAndMessage(final String trace) {
        Map<Integer, CauseElement> returns = new TreeMap<>();
        Matcher matcher = PATTERN_EXCEPTION_AND_MESSAGE.matcher(trace);
        while (matcher.find()) {
            String type = matcher.group(1);
            String message = matcher.group(2);

            if (!type.isEmpty()) {
                type = type.substring(0, type.length() - 1); // remove ":" as in exception: message
                type = type.trim();
                message = message.trim();

                if (type.matches(VALID_CLASSNAME)
                        && isInClassPath(type)) {
                    returns.put(matcher.start(), new CauseElement(type, message));
                }
            }
        }
        return returns;
    }

    protected static Map<Integer, StackTraceElement> parseStackTraceElements(final String trace) {
        Map<Integer, StackTraceElement> stack = new TreeMap<>();
        Matcher matcher = PATTERN_STACKTRACE.matcher(trace);
        while (matcher.find()) {
            String className = matcher.group(1);
            String methodName = "";
            String[] classnameSplit = className.split("\\.");
            if (classnameSplit.length > 1) {
                className = className.substring(0, className.lastIndexOf("."));
                methodName = classnameSplit[classnameSplit.length - 1];
            }

            String fileName = matcher.group(2);
            int lineNumber = -2;
            if (fileName.contains(":")) {
                String line = fileName.substring(fileName.indexOf(":") + 1);
                fileName = fileName.substring(0, fileName.indexOf(":"));
                try {
                    lineNumber = Integer.valueOf(line);
                } catch (Exception e) { // No valid linenumber available
                }
            }
            StackTraceElement element = new StackTraceElement(className, methodName, fileName, lineNumber);
            stack.put(matcher.start(), element);
        }
        return stack;
    }

    protected static boolean isInClassPath(String type) {
        try {
            Class.forName(type);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected static Throwable lookupThrowable(String type, String msg) {
        Object instance;
        try {
            Class<?> clazz = Class.forName(type);
            Constructor<?> constructor = clazz.getConstructor(String.class);
            constructor.setAccessible(true);
            instance = constructor.newInstance(msg);
        } catch (Throwable e) {
            try {
                Class<?> clazz = Class.forName(type);
                Constructor<?> constructor = clazz.getConstructor();
                constructor.setAccessible(true);
                instance = constructor.newInstance();
            } catch (Throwable e1) {
                instance = new RuntimeException(msg);
            }
        }

        return (Throwable) instance;
    }

    protected static class CauseElement {
        private String type;
        private String message;

        public CauseElement() {
        }

        public CauseElement(String type, String msg) {
            this.type = type;
            this.message = msg;
        }

        public String getMessage() {
            return message;
        }

        public CauseElement setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getType() {
            return type;
        }

        public CauseElement setType(String type) {
            this.type = type;
            return this;
        }
    }
}
