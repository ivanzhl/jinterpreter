package org.jinterpreter.ast;

import java.util.List;

public sealed interface Node permits Node.Number, Node.Identifier,
        Node.Assign, Node.BinaryOperation, Node.If, Node.While,
        Node.Block, Node.Fun, Node.Call, Node.Return {

    record Number(double value) implements Node {
    }

    record Identifier(String name) implements Node {
    }

    record Assign(String name, Node value) implements Node {
    }

    record BinaryOperation(String operator, Node left, Node right) implements Node {
    }

    record If(Node condition, Node thenBranch, Node elseBranch) implements Node {
    }


    record While(Node condition, Node body) implements Node {
    }

    record Block(List<Node> statements) implements Node {
    }

    record Fun(String name, List<String> parameters, Node body) implements Node {
    }

    record Call(String name, List<Node> arguments) implements Node {
    }

    record Return(Node value) implements Node {
    }
}