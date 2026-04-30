package org.jinterpreter.runtime;

import org.jinterpreter.exceptions.InterpreterException;

import java.util.*;

public class Environment {

    private final Map<String, Object> variables = new LinkedHashMap<>();
    private final Environment parent;

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        if (variables.containsKey(name)) return variables.get(name);
        if (parent != null) return parent.getVariable(name);
        throw new InterpreterException("undefined variable '" + name + "'");
    }

    public boolean hasVariable(String name) {
        if (variables.containsKey(name)) return true;
        return parent != null && parent.hasVariable(name);
    }

    public Map<String, Object> getAllVariables() {
        return Collections.unmodifiableMap(variables);
    }
}