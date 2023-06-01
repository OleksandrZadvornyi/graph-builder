package graph.expression;

import graph.parser.ExpressionParser;

public class Function {

    private final String expr;

    public Function(String expr) {
        this.expr = expr;
    }

    public double evaluateAt(double x) throws Exception {
        ExpressionParser parser = new ExpressionParser();
        return parser.parse(expr.replaceAll("x", String.valueOf(x)));
    }
}
