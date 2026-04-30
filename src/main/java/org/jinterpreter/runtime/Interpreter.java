package org.jinterpreter.runtime;

import org.jinterpreter.ast.Node;
import org.jinterpreter.exceptions.InterpreterException;
import org.jinterpreter.exceptions.ReturnException;

import java.util.List;

public class Interpreter {

    private final Environment globalEnvironment = new Environment();
    private int callDepth = 0;
    private static final int MAX_CALL_DEPTH = 500;

    public Environment run(List<Node> program) {
        for (Node statement : program) {
            evaluate(statement, globalEnvironment);
        }
        return globalEnvironment;
    }

    private Object evaluate(Node node, Environment environment) {
        return switch (node) {
            case Node.Number n -> evaluateNumber(n);
            case Node.Identifier i -> evaluateIdentifier(i, environment);
            case Node.Assign a -> evaluateAssignment(a, environment);
            case Node.BinaryOperation b -> evaluateBinaryOperation(b, environment);
            case Node.If i -> evaluateIf(i, environment);
            case Node.While w -> evaluateWhile(w, environment);
            case Node.Block b -> evaluateBlock(b, environment);
            case Node.Fun f -> evaluateFunctionDefinition(f, environment);
            case Node.Call c -> evaluateFunctionCall(c, environment);
            case Node.Return r -> evaluateReturn(r, environment);
        };
    }

    private Object evaluateNumber(Node.Number node) {
        return node.value();
    }

    private Object evaluateIdentifier(Node.Identifier node, Environment environment) {
        return environment.getVariable(node.name());
    }

    private Object evaluateAssignment(Node.Assign node, Environment environment) {
        Object value = evaluate(node.value(), environment);
        environment.setVariable(node.name(), value);
        return value;
    }

    private Object evaluateBinaryOperation(Node.BinaryOperation node, Environment environment) {
        double left = toNumber(evaluate(node.left(), environment));
        double right = toNumber(evaluate(node.right(), environment));
        return switch (node.operator()) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0) throw new InterpreterException("division by zero");
                yield left / right;
            }
            case "==" -> booleanToNumber(left == right);
            case "!=" -> booleanToNumber(left != right);
            case "<" -> booleanToNumber(left < right);
            case "<=" -> booleanToNumber(left <= right);
            case ">" -> booleanToNumber(left > right);
            case ">=" -> booleanToNumber(left >= right);
            default -> throw new InterpreterException("unknown operator '" + node.operator() + "'");
        };
    }

    private Object evaluateIf(Node.If node, Environment environment) {
        Object condition = evaluate(node.condition(), environment);
        if (isTruthy(condition)) return evaluate(node.thenBranch(), environment);
        else return evaluate(node.elseBranch(), environment);
    }

    private Object evaluateWhile(Node.While node, Environment environment) {
        Object result = null;
        while (isTruthy(evaluate(node.condition(), environment))) {
            result = evaluate(node.body(), environment);
        }
        return result;
    }

    private Object evaluateBlock(Node.Block node, Environment environment) {
        Object result = null;
        for (Node statement : node.statements()) {
            result = evaluate(statement, environment);
        }
        return result;
    }

    private Object evaluateFunctionDefinition(Node.Fun node, Environment environment) {
        JFunction function = new JFunction(node.name(), node.parameters(), node.body());
        environment.setVariable(node.name(), function);
        return function;
    }

    private Object evaluateFunctionCall(Node.Call node, Environment environment) {
        Object found = environment.getVariable(node.name());
        if (!(found instanceof JFunction(String name, List<String> parameters, Node body)))
            throw new InterpreterException("'" + node.name() + "' is not a function");

        List<Node> arguments = node.arguments();
        if (arguments.size() != parameters.size())
            throw new InterpreterException("function '" + name + "' expects " + parameters.size() + " arguments but got " + arguments.size());

        if (++callDepth > MAX_CALL_DEPTH)
            throw new InterpreterException("maximum call depth exceeded");

        Environment functionEnvironment = new Environment(environment);
        for (int i = 0; i < parameters.size(); i++) {
            Object value = evaluate(arguments.get(i), environment);
            functionEnvironment.setVariable(parameters.get(i), value);
        }

        try {
            evaluate(body, functionEnvironment);
        } catch (ReturnException returnValue) {
            return returnValue.getValue();
        }
        finally {
            callDepth--;
        }

        return null;
    }

    private Object evaluateReturn(Node.Return node, Environment environment) {
        throw new ReturnException(evaluate(node.value(), environment));
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Double number) return number != 0;
        return true;
    }

    private double toNumber(Object value) {
        if (value instanceof Double number) return number;
        throw new InterpreterException("expected a number but got '" + value + "'");
    }

    private double booleanToNumber(boolean value) {
        return value ? 1.0 : 0.0;
    }
}