package org.jinterpreter;

import org.jinterpreter.ast.Node;
import org.jinterpreter.runtime.Environment;
import org.jinterpreter.runtime.Interpreter;
import org.jinterpreter.runtime.JFunction;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            final String source = new String(System.in.readAllBytes());

            final List<Token> tokens = new Scanner(source).scan();
            final List<Node> program = new Parser(tokens).parse();
            final Environment environment = new Interpreter().run(program);

            environment.getAllVariables().forEach((name, value) -> {
                if (value instanceof JFunction) return;
                if (value == null) return;
                System.out.println(name + ": " + formatValue(value));
            });
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static String formatValue(Object value) {
        if (value instanceof Double number) {
            if (number == Math.floor(number) && !Double.isInfinite(number))
                return String.valueOf((long) number.doubleValue());
            return String.valueOf(number);
        }
        return String.valueOf(value);
    }
}