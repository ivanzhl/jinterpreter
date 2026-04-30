# jinterpreter

A tree-walking interpreter for a small imperative language, written in Java. The interpreter reads a source program from standard input, executes it, and prints the final values of all variables to standard output.

## Requirements

- Java 21
- Maven 3.6+

## Building

```
mvn package
```

This produces `target/jinterpreter-1.0.jar`.

## Running

```
java -jar target/jinterpreter-1.0.jar < program.txt
```

Or inline:

```
echo "x = 6 * 7" | java -jar target/jinterpreter-1.0.jar
```

Output:

```
x: 42
```

## Language reference

### Values

The only data type is a double-precision floating-point number. Integer values are printed without a decimal point (`42`, not `42.0`).

### Literals

| Form   | Description     |
|--------|-----------------|
| `42`   | Integer literal |
| `3.14` | Decimal literal |
| `true` | Numeric 1       |
| `false`| Numeric 0       |

### Operators

| Operator            | Description |
|---------------------|-------------|
| `+` `-` `*` `/`    | Arithmetic  |
| `==` `!=`          | Equality    |
| `<` `<=` `>` `>=` | Comparison  |

Comparison operators return `1` (true) or `0` (false). Standard arithmetic precedence applies; parentheses can override it.

### Statements

**Assignment**

```
x = expression
```

**Conditional**

```
if condition then statement
if condition then statement else statement
```

**While loop**

```
while condition do statement
while condition do statement, statement, statement
```

**Function definition**

```
fun name(param1, param2) {
    statement
    statement
}
```

**Return**

```
return expression
```

### Statement separators

At the top level, statements are separated by newlines or commas. Inside a `while` body, commas separate the statements that execute each iteration. Inside a function body, both newlines and commas work.

### Scope

Each function call creates a new scope that inherits from the enclosing scope, so functions can read outer variables. Variables assigned inside a function are local to that function and are not visible to the caller after it returns.

### Output

After execution, every variable in the global scope is printed in the order it was first assigned, one per line:

```
name: value
```

Function names are not included in the output.

## Examples

**Arithmetic**

```
x = 2
y = (x + 2) * 2
```

```
x: 2
y: 8
```

**Conditional**

```
x = 20
if x > 10 then y = 100 else y = 0
```

```
x: 20
y: 100
```

**Loop**

```
x = 0
y = 0
while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
```

```
x: 3
y: 11
```

**Functions**

```
fun add(a, b) { return a + b }
four = add(2, 2)
```

```
four: 4
```

**Recursive factorial**

```
fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
a = fact_rec(5)
```

```
a: 120
```

**Iterative factorial**

```
fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
b = fact_iter(5)
```

```
b: 120
```

## Project structure

```
src/
  main/java/org/jinterpreter/
    Main.java                   Entry point
    Scanner.java                Source text -> token list
    Parser.java                 Token list -> AST
    Token.java
    TokenType.java
    ast/
      Node.java                 Sealed AST node hierarchy
    runtime/
      Interpreter.java          Tree-walking evaluator
      Environment.java          Variable scope chain
      JFunction.java            Runtime function representation
    exceptions/
      ScannerException.java
      ParseException.java
      InterpreterException.java
      ReturnException.java
  test/java/org/jinterpreter/
    ScannerTest.java
    ParserTest.java
    InterpreterTest.java
```
