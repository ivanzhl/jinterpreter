# jinterpreter

A tree-walk interpreter for a small imperative language, written in Java 17.
Reads a source program from standard input, executes it, and prints the values
of all variables to standard output.
 
---

## Building

Requires Java 17 and Maven 3.6+.

```bash
mvn package -q
```

This produces `target/jinterpreter-1.0.jar`.
 
---

## Running

```bash
java -jar target/jinterpreter-1.0.jar < examples/factorial_rec.jint
```

Or inline:

```bash
echo "x = 6
y = x * 7" | java -jar target/jinterpreter-1.0.jar
```

Output:

```
x: 6
y: 42
```
 
---

## Running the tests

```bash
mvn test
```
 
---

## Language

The language is a small imperative language with variables, arithmetic,
conditionals, loops, and first-class functions.

### Statements

Statements are separated by newlines or commas. Commas allow multiple
statements on the same line:

```
x = 1, y = 2
```

### Expressions

Standard arithmetic with correct operator precedence and parentheses:

```
z = (x + 2) * y
```

### Conditionals

```
if x > 10 then y = 100 else y = 0
```

### Loops

```
while x < 3 do x = x + 1
```

### Functions

Functions are defined with `fun` and support recursion and early return:

```
fun factorial(n) {
    if n <= 0 then return 1 else return n * factorial(n - 1)
}
 
result = factorial(5)
```

### Operators

| Operator | Meaning |
|----------|---------|
| `+` `-` `*` `/` | arithmetic |
| `==` `!=` `<` `<=` `>` `>=` | comparison |
| `=` | assignment |
 
---

## Project structure

```
src/
  main/java/org/jinterpreter/
    Main.java               entry point
    Lexer.java              source text -> token list
    Token.java              token data class
    TokenType.java          token type enum
    LexerException.java
    Parser.java             token list -> AST
    ParseException.java
    ast/                    AST node classes
      Node.java
      AssignNode.java
      BinaryOpNode.java
      IfNode.java
      WhileNode.java
      BlockNode.java
      FunNode.java
      CallNode.java
      ReturnNode.java
      NumberNode.java
      IdentNode.java
    runtime/                execution
      Interpreter.java
      Environment.java
      JFunction.java
      ReturnException.java
      RuntimeError.java
  test/java/org/jinterpreter/
    LexerTest.java
    ParserTest.java
    InterpreterTest.java
 
examples/
  arithmetic.jint
  if_else.jint
  while_loop.jint
  function.jint
  factorial_rec.jint
  factorial_iter.jint
```
 
---

## Implementation notes

The interpreter is a classic three-phase pipeline: **Lexer** converts source
text into a flat token list, **Parser** builds an abstract syntax tree via
recursive descent, and **Interpreter** walks the tree and evaluates each node.

Scoping is handled by a linked chain of `Environment` objects — each function
call creates a new scope with a reference to its enclosing scope. The `return`
statement is implemented as a Java exception (`ReturnException`) that unwinds
the call stack back to the nearest function boundary.

No external libraries are used beyond JUnit 5 for testing.
