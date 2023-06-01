package graph.parser;

public class ExpressionParser {

    private char[] expr;    // масив з виразом
    private int exprIdx;    // поточний індекс в виразі
    private String exprStr;
    private String token;   // містить поточку лексему
    private int tokType;    // містить тип поточної лексеми
    private int kwToken;

    /**
     * В цьому класі звязуються ключові слова із їх лексемами
     */
    class Keyword {

        String keyword; // стрічка
        int keywordTok; // внутрішнє представлення

        Keyword(String str, int t) {
            keyword = str;
            keywordTok = t;
        }
    }
    Keyword kwTable[];

    // внутрішнє представлення ключових слів
    final int UNKNCOM = 0;
    final int SIN = 1;
    final int COS = 2;
    final int TAN = 3;
    final int COT = 4;
    final int ASIN = 5;
    final int ACOS = 6;
    final int ATAN = 7;
    final int ACOT = 8;
    final int EXP = 9;
    final int LOG10 = 10;
    final int LOG1P = 11;
    final int SQRT = 12;
    final int ABS = 13;

    // типи лексем
    final int NONE = 0;
    final int DELIMITER = 1;
    final int NUMBER = 3;
    final int COMMAND = 4;

    // лексема кінця виразу
    final String EOP = "\0";

    public ExpressionParser() {
        this.kwTable = new Keyword[]{
            new Keyword("sin", SIN),
            new Keyword("cos", COS),
            new Keyword("tan", TAN),
            new Keyword("cot", COT),
            new Keyword("asin", ASIN),
            new Keyword("acos", ACOS),
            new Keyword("atan", ATAN),
            new Keyword("acot", ACOT),
            new Keyword("e", EXP),
            new Keyword("log10", LOG10),
            new Keyword("log1p", LOG1P),
            new Keyword("sqrt", SQRT),
            new Keyword("abs", ABS),};
    }

    public double parse(String expr) throws Exception {
        if (expr.equals("")) {
            return 0.0;
        }
        if (expr.charAt(0) == '-') {
            exprStr = expr.replaceFirst("-", "(0-1)*");
        } else {
            exprStr = expr;
        }
        this.expr = exprStr.toCharArray();

        return evaluate();
    }

    /**
     * Починає обчислення виразу за допомогою рекурсивно-низхідного алгоритму.
     * Послідовно перебирає лексеми у виразі та рекурсивно викликає окремі
     * методи, що відповідають базовим математичним операціям.
     *
     * @return - результат обчислення
     */
    private double evaluate() throws Exception {
        double result;

        getToken();
        if (token.equals(EOP)) {
            //handleErr(NOEXP);       // немає виразу
        }

        // починаємо аналіз виразу
        result = evalExp1();
        putBack();
        return result;
    }

    /**
     * Обробляє оператор віднімання та додавання. Викликає метод evalExp2() для
     * обчислення операції множення та ділення. Повертає результат операції.
     *
     * @return - результат операції віднімання/додавання
     */
    private double evalExp1() throws Exception {
        char op;
        double result;
        double partialResult;

        result = evalExp2();

        while ((op = token.charAt(0)) == '+' || op == '-') {
            getToken();
            partialResult = evalExp2();
            switch (op) {
                case '-':
                    result = result - partialResult;
                    break;
                case '+':
                    result = result + partialResult;
                    break;
            }
        }
        return result;
    }

    /**
     * Обробляє оператор множення та ділення. Викликає метод evalExp3() для
     * обчислення операції піднесення до степеня. Повертає результат операції.
     *
     * @return - результат операції множення/ділення
     */
    private double evalExp2() throws Exception {
        char op;
        double result;
        double partialResult;

        result = evalExp3();

        while ((op = token.charAt(0)) == '*' || op == '/' || op == '%') {
            getToken();

            partialResult = evalExp3();
            switch (op) {
                case '*':
                    result = result * partialResult;
                    break;
                case '/':
                    if (partialResult == 0.0) {
                        //handleErr(DIVBYZERO);
                    }
                    result = result / partialResult;
                    break;
                case '%':
                    if (partialResult == 0.0) {
                        //handleErr(DIVBYZERO);
                    }
                    result = result % partialResult;
                    break;
            }
        }
        return result;
    }

    /**
     * Обробляє оператор піднесення до степеня. Викликає метод evalExp4() для
     * обчислення операції унарного плюса та мінуса. Повертає результат
     * операції.
     *
     * @return - результат операції піднесення до степеня
     */
    private double evalExp3() throws Exception {
        double result;
        double partialResult;
        double ex;
        int t;

        result = evalExp4();

        if (token.equals("^")) {
            getToken();
            partialResult = evalExp3();
            ex = result;
            if (partialResult == 0.0) {
                result = 1.0;
            } else {
                for (t = (int) partialResult - 1; t > 0; t--) {
                    result = result * ex;
                }
            }
        }
        return result;
    }

    /**
     * Обробляє оператор унарного плюса та мінуса. Викликає метод evalExp5() для
     * обчислення обробки дужок. Повертає результат операції.
     *
     * @return - результат операції унарного плюса/мінуса
     */
    private double evalExp4() throws Exception {
        double result;
        String op;

        op = "";
        if ((tokType == DELIMITER)
                && token.equals("+") || token.equals("-")) {
            op = token;
            getToken();
        }
        result = evalExp5();

        if (op.equals("-")) {
            result = -result;
        }

        return result;
    }

    /**
     * Обробляє тригонометричні функції та дужки. Якщо такі присутні, продовжує
     * рекурсію викливаючи метод evalExp1() початку обчислення виразу у дужках.
     * Якщо функцій немає, завершує рекурсію. Повертає результат операції.
     *
     * @return - результат обчислення виразу у дужках
     */
    private double evalExp5() throws Exception {
        double result;

        if (token.equals("(")) {
            getToken();
            result = evalExp1();
            if (!token.equals(")")) {
                //handleErr(UNBALPARENS);
            }
            getToken();
        } else if (tokType == COMMAND) {
            getToken();
            getToken();
            result = switch (kwToken) {
                case SIN ->
                    Math.sin(evalExp1());
                case COS ->
                    Math.cos(evalExp1());
                case TAN ->
                    Math.tan(evalExp1());
                case COT ->
                    1 / Math.tan(evalExp1());
                case ASIN ->
                    Math.asin(evalExp1());
                case ACOS ->
                    Math.acos(evalExp1());
                case ATAN ->
                    Math.atan(evalExp1());
                case ACOT ->
                    Math.atan(1 / evalExp1());
                case LOG10 ->
                    Math.log10(evalExp1());
                case LOG1P ->
                    Math.log1p(evalExp1());
                case SQRT ->
                    Math.sqrt(evalExp1());
                case ABS ->
                    Math.abs(evalExp1());
                case EXP ->
                    Math.exp(evalExp1());
                default ->
                    throw new Exception();
            };
            if (!token.equals(")")) {
                //handleErr(UNBALPARENS);
            }
            getToken();
        } else {
            result = atom();
        }

        return result;
    }

    /**
     * Отримує наступний елемент з потоку.
     */
    private void getToken() {
        tokType = NONE;
        token = "";

        if (exprIdx == expr.length) {       // чи не досягнуто кінець програми?
            token = EOP;
            return;
        }

        while (exprIdx < expr.length // пропускаємо пробіли
                && isSpaceOrTab(expr[exprIdx])) {
            exprIdx++;
        }

        if (isDelim(expr[exprIdx])) {                   // Оператор
            token += expr[exprIdx];
            exprIdx++;
            tokType = DELIMITER;
        } else if (Character.isLetter(expr[exprIdx])) { // ключове слово
            while (!isDelim(expr[exprIdx])) {
                token += expr[exprIdx];
                exprIdx++;
                if (exprIdx >= expr.length) {
                    break;
                }
            }

            kwToken = lookUp(token);
            if (kwToken == UNKNCOM) {
                //tokType = VARIABLE;
            } else {
                tokType = COMMAND;
            }
        } else if (Character.isDigit(expr[exprIdx])) {  // число
            while (!isDelim(expr[exprIdx])) {
                token += expr[exprIdx];
                exprIdx++;
                if (exprIdx >= expr.length) {
                    break;
                }
            }
            tokType = NUMBER;
        } else {
            // невідомий символ
            token = EOP;
        }
    }

    /**
     * Повертає поточний елемент назад у вхідний потік.
     */
    private void putBack() {
        if (EOP.equals(token)) {
            return;
        }

        for (int i = 0; i < token.length(); i++) {
            exprIdx--;
        }
    }

    /**
     * Метод використовує таблицю kwTable для перетворення лексем в їх
     * внутрішній формат. Якщо відповідність не знайдено, то повертається
     * значення UNKNCOM
     *
     * @param s - лексема
     * @return - формат лексеми у відповідності з таблицею kwTable
     */
    private int lookUp(String s) {
        int i;

        // перетворити в нижній регістр
        s = s.toLowerCase();

        // чи є елемент в таблиці
        for (i = 0; i < kwTable.length; i++) {
            if (kwTable[i].keyword.equals(s)) {
                return kwTable[i].keywordTok;
            }
        }
        return UNKNCOM;     // невідоме слово
    }

    /**
     * Повертає числове значення числа, яке представлене у стрічковому форматі.
     *
     * @return - дійсне число, що відповідає отриманому токену
     */
    private double atom() {
        double result = 0.0;

        switch (tokType) {
            case NUMBER:
                try {
                result = Double.parseDouble(token);
            } catch (NumberFormatException exc) {
                //handleErr(SYNTAX);
            }
            getToken();
            break;
            default:
                //handleErr(SYNTAX);
                break;
        }
        return result;
    }

    /**
     * Перевіряє чи отриманий символ є символом-розділювачем
     *
     * @param c - отриманий символ
     * @return - true, якщо символ є розділювачем, інакше - false
     */
    private boolean isDelim(char c) {
        return (" \r,;<>+-/*%^=()".indexOf(c) != -1);
    }

    /**
     * Перевіряє чи отриманий символ є пробілом чи табуляцією
     *
     * @param c - отриманий символ
     * @return - true, якщо символ є пробілом чи табуляцією, інакше - false
     */
    boolean isSpaceOrTab(char c) {
        return c == ' ' || c == '\t';
    }
}
