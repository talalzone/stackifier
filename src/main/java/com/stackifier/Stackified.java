package com.stackifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Talal Ahmed on 09/02/2018
 */
public class Stackified {

    private final String stacktrace;
    private final String description;
    private final Throwable throwable;
    private final List<Group> groups = new ArrayList<>();

    public Stackified(String stacktrace, String description, Throwable throwable) {
        this.stacktrace = stacktrace;
        this.description = description;
        this.throwable = throwable;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public String getDescription() {
        return description;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Group> getGroups(String groupName) {
        return groups.stream()
                .filter(x -> x.getName().equals(groupName))
                .collect(Collectors.toList());
    }

    public Optional<Group> getGroup(int groupId) {
        return groups.stream()
                .filter(x -> x.getId() == groupId)
                .findFirst();
    }

    public List<StackTraceElement> getElements(int groupId) {
        return groups.stream()
                .filter(x -> x.getId() == groupId)
                .map(Group::getElements)
                .findFirst()
                .get();
    }

    protected void addGroup(Group group) {
        groups.add(group);
    }

    public void prettyPrint() {
        StringBuilder sb = new StringBuilder();

        sb.append(description);
        sb.append("\n");
        groups.forEach(x -> sb.append(x.prettyString()));

        System.out.println(sb.toString());
    }

    @Override
    public String toString() {
        return "Stackified{" +
                "stacktrace='" + stacktrace + '\'' +
                ", description='" + description + '\'' +
                ", throwable=" + throwable +
                ", groups=" + groups +
                '}';
    }
}
