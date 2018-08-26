package com.stackifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Talal Ahmed on 26/08/2018
 */
public class Group {

    private final Integer id;
    private final String name;
    private final List<StackTraceElement> elements;

    public Group(Integer id, String name) {
        this(id, name, new ArrayList<>());
    }

    public Group(Integer id, String name, List<StackTraceElement> elements) {
        this.id = id;
        this.name = name;
        this.elements = elements;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<StackTraceElement> getElements() {
        return elements;
    }

    protected void addElement(StackTraceElement element) {
        this.elements.add(element);
    }

    public String prettyString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("(").append(id).append(")").append(" ").append(name);
        sb.append("\n");

        elements.forEach(x -> {
            sb.append("\t\t");
            sb.append(x.toString());
            sb.append("\n");
        });

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Group)) return false;

        Group other = (Group) o;
        return other.id.equals(this.id) &&
                other.name.equals(this.name);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", elements=" + elements +
                '}';
    }
}