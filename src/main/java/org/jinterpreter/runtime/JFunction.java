package org.jinterpreter.runtime;

import org.jinterpreter.ast.Node;

import java.util.List;

public record JFunction(String name, List<String> parameters, Node body) {}