package com.ontimize.gui;

import java.util.*;

public class MultipleValue implements java.io.Serializable {

    private final Map data = new Hashmap();

    public MultipleValue(Map data) {
        if (data != null) {
            this.data.putAll(data);
        }
    }

    public Object get(Object attribute) {
        return this.data.get(attribute);
    }

    public void put(Object attribute, Object value) {
        this.data.put(attribute, value);
    }

    public void clear() {
        this.data.clear();
    }

    public Enumeration keys() {
        return Collections.enumeration(this.data.keySet());
    }

}
