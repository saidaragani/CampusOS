package com.campusos.api_gateway.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Request wrapper that lets the gateway add or hide headers (case-insensitively)
 * before forwarding downstream. Used to inject trusted identity headers and strip
 * any client-supplied copies of them.
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> overridden = new LinkedHashMap<>();
    private final Set<String> removed = new LinkedHashSet<>();

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public void setHeader(String name, String value) {
        overridden.put(name, value);
    }

    public void removeHeader(String name) {
        removed.add(name);
    }

    private String overrideKey(String name) {
        for (String k : overridden.keySet()) {
            if (k.equalsIgnoreCase(name)) {
                return k;
            }
        }
        return null;
    }

    private boolean isRemoved(String name) {
        for (String k : removed) {
            if (k.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getHeader(String name) {
        String key = overrideKey(name);
        if (key != null) {
            return overridden.get(key);
        }
        if (isRemoved(name)) {
            return null;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String key = overrideKey(name);
        if (key != null) {
            return Collections.enumeration(List.of(overridden.get(key)));
        }
        if (isRemoved(name)) {
            return Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            String n = original.nextElement();
            if (overrideKey(n) == null && !isRemoved(n)) {
                names.add(n);
            }
        }
        names.addAll(overridden.keySet());
        return Collections.enumeration(names);
    }
}
