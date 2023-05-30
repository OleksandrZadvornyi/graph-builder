package graph.expression;

public class Exponent extends Unary {

    public Exponent(Quantity q) {
        super(q);
    }

    @Override
    public double getValue() {
        double val = realValue(q);
        return Math.exp(val);
    }
}
