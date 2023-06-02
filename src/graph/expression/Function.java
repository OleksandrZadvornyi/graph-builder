package graph.expression;

import graph.parser.ExpressionParser;

/**
 * Клас функції, який зберігає текстове представлення математичної функції.
 * Містить метод "evaluateAt" для обчислення значення функції в заданій точці.
 *
 * @author Oleksandr
 */
public class Function {

    // текстове представлення функції
    private final String expr;

    /**
     * Конструктор для ініціалізації функції.
     *
     * @param expr - текстове представлення функції
     */
    public Function(String expr) {
        this.expr = expr;
    }

    /**
     * Обчислює значення функції в заданій точці за допомогою
     * рекурсивно-низхідного алгоритму розбору математичних виразів.
     *
     * @param x - координата 'х', в якій потрібно обчислити значення функції
     * @return - результат обчислення (значення координати 'у')
     * @throws Exception - помилка аналізу виразу
     */
    public double evaluateAt(double x) throws Exception {
        ExpressionParser parser = new ExpressionParser();
        return parser.parse(expr.replaceAll("x", "(" + String.valueOf(x) + ")"));
    }
}
