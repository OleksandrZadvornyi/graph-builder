package graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import graph.expression.Function;
import graph.parser.ExpressionParser;

/**
 * Клас, який відповідає за побудову координатної площини та безпосередньо самих
 * графіків.
 *
 * @author Oleksandr
 */
public class Window extends JPanel implements MouseWheelListener, KeyListener, Runnable {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    private final BufferedImage buff;
    private Graphics2D g2d;

    private final ExpressionParser parser;
    private Function function;
    private Function function1;

    private double windowX, windowY, windowWidth, windowHeight;
    private Point mousePt;

    private String textBox;
    private String textBox1;

    private boolean textBoxActive = true;
    private boolean textBox1Active = false;

    // Списки для збереження обрахованих координат графіків
    private List<Double> bxs;
    private List<Double> bys;
    private List<Double> rxs;
    private List<Double> rys;

    // Масиви для збереження координат графіків відносно екрану.
    private int[] bxa;
    private int[] bya;
    private int[] rxa;
    private int[] rya;

    // Чи наведено курсором на точку перетину графіків
    private boolean isHover = false;

    // Координати курсора
    private double movedX;
    private double movedY;

    /**
     * Коснструктор класу для ініціалізації основних полів та оголошення методів
     * для відстеження подій.
     */
    public Window() {
        addMouseWheelListener(this);
        addKeyListener(this);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePt = e.getPoint();
                int y = e.getY();
                if (y >= 673 && y <= 720) {
                    textBoxActive = false;
                    textBox1Active = true;
                } else if (y > 720) {
                    textBoxActive = true;
                    textBox1Active = false;
                }
                repaint();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - mousePt.x;
                int dy = e.getY() - mousePt.y;
                windowX -= dx / (double) WIDTH * windowWidth;
                windowY += dy / (double) HEIGHT * windowHeight;
                mousePt = e.getPoint();

                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                isHover = false;
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("courier new", Font.ITALIC, 25));
                if (!textBox.equals(textBox1)) {
                    for (int i = 0; i < rxa.length; i++) {
                        if (rxa[i] == bxa[i] && rya[i] == bya[i]
                                && (event.getX() >= rxa[i] - 3 && event.getX() <= rxa[i] + 3)
                                && (event.getY() >= rya[i] - 3 && event.getY() <= rya[i] + 3)) {
                            movedX = (double) event.getX();
                            movedY = (double) event.getY();
                            isHover = true;
                        }
                    }
                }
                repaint();
            }
        });

        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        buff = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g2d = buff.createGraphics();

        parser = new ExpressionParser();
        textBox = "";
        function = new Function(textBox);

        textBox1 = "";
        function = new Function(textBox1);

        windowX = 0.0;
        windowY = 0.0;
        windowHeight = 2.0;
        windowWidth = windowHeight * WIDTH / HEIGHT;
    }

    /**
     * Обраховує всі точки графіка, вказаного в першому текстовому полі
     * (синьому). Зберігає результат обрахунку в списках bxs, bys та масивах
     * bxa, bya.
     *
     * @throws Exception - помилка аналізу виразу
     */
    private void calculateFirstGraphCoordinates() throws Exception {
        bxs = new ArrayList<>();
        bys = new ArrayList<>();

        for (int x = 0; x < WIDTH; x++) {
            double xx = toRealX(x);

            double yy = 0.0;
            if (function != null) {
                yy = function.evaluateAt(xx);
            }

            double scaledX = x;
            double scaledY = toScreenY(yy);
            scaledY = Math.min(Math.max(scaledY, -5), HEIGHT + 5);

            bxs.add(scaledX);
            bys.add(scaledY);
        }

        bxa = new int[bxs.size()];
        bya = new int[bys.size()];

        for (int i = 0; i < bxa.length; i++) {
            bxa[i] = bxs.get(i).intValue();
        }
        for (int i = 0; i < bya.length; i++) {
            bya[i] = bys.get(i).intValue();
        }
    }

    /**
     * Обраховує всі точки графіка, вказаного в другому текстовому полі
     * (червоному). Зберігає результат обрахунку в списках rxs, rys та масивах
     * rxa, rya.
     *
     * @throws Exception - помилка аналізу виразу
     */
    private void calculateSecondGraphCoordinates() throws Exception {
        rxs = new ArrayList<>();
        rys = new ArrayList<>();

        for (int x = 0; x < WIDTH; x++) {
            double xx = toRealX(x);

            double yy = 0.0;
            if (function1 != null) {
                yy = function1.evaluateAt(xx);
            }

            double scaledX = x;
            double scaledY = toScreenY(yy);
            scaledY = Math.min(Math.max(scaledY, -5), HEIGHT + 5);

            rxs.add(scaledX);
            rys.add(scaledY);
        }

        rxa = new int[rxs.size()];
        rya = new int[rys.size()];

        for (int i = 0; i < rxa.length; i++) {
            rxa[i] = rxs.get(i).intValue();
        }
        for (int i = 0; i < rya.length; i++) {
            rya[i] = rys.get(i).intValue();
        }
    }

    /**
     * Перемальовує систему координат. Обчилює координати обох графіків та
     * відображує їх на координатній площині.
     *
     * @param g - компонент графіки
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        synchronized (this) {
            try {
                calculateFirstGraphCoordinates();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                textBox = "";
                function = new Function(textBox);
            }
            try {
                calculateSecondGraphCoordinates();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                textBox1 = "";
                function1 = new Function(textBox);
            }

            // Намалювати графіки
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            if (!textBox.equals("")) {
                g2d.setColor(Color.BLUE);
                g2d.drawPolyline(bxa, bya, bxa.length);
            }
            if (!textBox1.equals("")) {
                g2d.setColor(Color.RED);
                g2d.drawPolyline(rxa, rya, rxa.length);
            }

            g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            // Намалювати вісь X
            g2d.setColor(Color.BLACK);
            int xAxisY = toScreenY(0.0);
            g2d.drawLine(0, xAxisY, WIDTH, xAxisY);

            // Намалювати вісь Y
            g2d.setColor(Color.BLACK);
            int yAxisX = toScreenX(0.0);
            g2d.drawLine(yAxisX, 0, yAxisX, HEIGHT);

            // Намалювати поділки
            drawDivisions();

            // Намалювати текстові поля для вводу функцій
            g2d.setFont(new Font("courier new", Font.ITALIC, 40));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, HEIGHT - g2d.getFontMetrics().getHeight(), WIDTH, HEIGHT);
            g2d.fillRect(0, HEIGHT - g2d.getFontMetrics().getHeight() - 50, WIDTH, 50);
            g2d.setColor(Color.BLACK);
            g2d.drawString("f(x) = " + textBox, 0.0f, HEIGHT - 10.0f);
            g2d.drawString("f(x) = " + textBox1, 0.0f, HEIGHT - 60.0f);

            // Намалювати позначення для осей координат
            g2d.drawString("x", 0, xAxisY - 10);
            g2d.drawString("y", yAxisX + 10, g2d.getFontMetrics().getHeight() - 20);

            // Намалювати рамку навколо активного текстового поля
            if (textBoxActive) {
                g2d.setColor(Color.BLUE);
                g2d.drawRect(0, HEIGHT - g2d.getFontMetrics().getHeight(), WIDTH - 2, 45);
            } else if (textBox1Active) {
                g2d.setColor(Color.RED);
                g2d.drawRect(0, HEIGHT - g2d.getFontMetrics().getHeight() - 50, WIDTH - 2, 45);
            }

            // Відображення значення точки перетину двох графіків 
            // при наведенні курсору миші на область перетину
            if (!textBox.equals("") && !textBox1.equals("")) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("courier new", Font.ITALIC, 25));
                if (!textBox.equals(textBox1)) {
                    for (int i = 0; i < rxa.length; i++) {
                        if (rxa[i] == bxa[i] && rya[i] == bya[i]) {
                            Ellipse2D.Double circle = new Ellipse2D.Double(rxa[i] - 5, rya[i] - 5, 10, 10);
                            g2d.fill(circle);

                            if (isHover) {
                                if (movedX >= rxa[i] - 5 && movedX <= rxa[i] + 5
                                        && movedY >= rya[i] - 5 && movedY <= rya[i] + 5) {
                                    g2d.drawString(String.format("%.2f", toRealX(rxa[i])) + ";" + String.format("%.2f", toRealY(rya[i])), rxa[i] - 100, (int) rya[i] + 35);
                                }
                            }
                        }
                    }
                }
            }
        }

        g.drawImage(buff, 0, 0, null);
    }

    /**
     * Малює поділки на координатній площині для осей X та Y.
     */
    private void drawDivisions() {
        g2d.setFont(new Font("courier new", Font.ITALIC, 25));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

        // Намалювати поділки для осі Y
        int yAxisX = toScreenX(0.0);
        for (int i = 0; i <= HEIGHT; i++) {
            if (toRealY(i) % 1 >= -0.01 && toRealY(i) % 1 <= 0.01) {
                g2d.drawLine(yAxisX - 5, i, yAxisX + 5, i);
                if ((int) toRealY(i) != 0) {
                    if ((int) toRealY(i) < 0) {
                        g2d.drawString(String.valueOf((int) toRealY(i)), yAxisX - 40, i + 10);
                    } else if ((int) toRealY(i) > 9) {
                        g2d.drawString(String.valueOf((int) toRealY(i)), yAxisX - 50, i + 10);
                    } else {
                        g2d.drawString(String.valueOf((int) toRealY(i)), yAxisX - 30, i + 10);
                    }
                }
            }
        }

        // Намалювати поділки для осі X
        int xAxisY = toScreenY(0.0);
        for (int i = 0; i <= WIDTH; i++) {
            if (toRealX(i) % 1 >= -0.01 && toRealX(i) % 1 <= 0.01) {
                g2d.drawLine(i, xAxisY + 5, i, xAxisY - 5);
                if ((int) toRealX(i) != 0) {
                    if ((int) toRealX(i) < 0) {
                        g2d.drawString(String.valueOf((int) toRealX(i)), i - 20, xAxisY + 30);
                    } else {
                        g2d.drawString(String.valueOf((int) toRealX(i)), i - 10, xAxisY + 30);
                    }
                } else {
                    g2d.drawString(String.valueOf((int) toRealX(i)), i - 30, xAxisY + 30);
                }
            }
        }
    }

    /**
     * Оновлює графіче відображення координатної площини кожну мілісекунду.
     */
    @Override
    public void run() {
        boolean running = true;
        while (running) {
            repaint();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Відстежує подію натиснення на кнопку на клавіатурі.
     *
     * @param e - об'єкт події натиснення на кнопку
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Відстежує подію натиснення на кнопку на клавіатурі. Залежно від
     * натисненої кнопки змінює текстове поле, в яке користувач вводить
     * математичний вираз для графіку. При натисненні клавіші "Enter" обраховує
     * значення функцій.
     *
     * @param e - об'єкт події натиснення на кнопку
     */
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (textBoxActive) {
                if (textBox.length() > 0) {
                    textBox = textBox.substring(0, textBox.length() - 1);
                }
            } else {
                if (textBox1.length() > 0) {
                    textBox1 = textBox1.substring(0, textBox1.length() - 1);
                }
            }
        } else if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '^' || e.getKeyChar() == '-'
                || e.getKeyChar() == '+' || e.getKeyChar() == '*' || e.getKeyChar() == '/' || e.getKeyChar() == '('
                || e.getKeyChar() == ')' || e.getKeyChar() == '%' || e.getKeyChar() == ',' || e.getKeyChar() == '.') {
            if (textBoxActive) {
                textBox += e.getKeyChar();
            } else {
                textBox1 += e.getKeyChar();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            function = new Function(textBox);
            function1 = new Function(textBox1);
        }
    }

    /**
     * Відстежує подію, коли кнопку на клавіатурі було відпущено.
     *
     * @param e - об'єкт події відпускання кнопки
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Обчислює координату нижньої межі графіку.
     *
     * @return - нижня межа координатної площини
     */
    private double bottom() {
        return windowY - halfWindowHeight();
    }

    /**
     * Обчислює координату правої межі графіку.
     *
     * @return - права межа координатної площини
     */
    private double right() {
        return windowX - halfWindowWidth();
    }

    /**
     * Перетворює значення координати відносно екрану в реальне значення
     * графіку.
     *
     * @param screenX - координата відносно екрану
     * @return - реальне значення відносно графіку
     */
    private double toRealX(int screenX) {
        return screenX / (double) WIDTH * windowWidth + right();
    }

    /**
     * Перетворює значення координати відносно екрану в реальне значення
     * графіку.
     *
     * @param screenY - координата відносно екрану
     * @return - реальне значення відносно графіку
     */
    private double toRealY(int screenY) {
        return (HEIGHT - screenY) / (double) HEIGHT * windowHeight + bottom();
    }

    /**
     * Перетворює отримане значення координати X відносно координат екрану.
     *
     * @param realX - реальне значення координати
     * @return - обчислене відносне значення до ширини екрану
     */
    private int toScreenX(double realX) {
        return (int) ((realX - right()) / windowWidth * WIDTH);
    }

    /**
     * Перетворює отримане значення координати Y відносно координат екрану.
     *
     * @param realY - реальне значення координати
     * @return - обчислене відносне значення до висоти екрану
     */
    private int toScreenY(double realY) {
        return HEIGHT - (int) ((realY - bottom()) / windowHeight * HEIGHT);
    }

    /**
     * Повертає половину від ширини вікна.
     *
     * @return - 1/2 від значення ширини вікна.
     */
    private double halfWindowWidth() {
        return windowWidth / 2.0;
    }

    /**
     * Повертає половину від висоти вікна.
     *
     * @return - 1/2 від значення висоти вікна.
     */
    private double halfWindowHeight() {
        return windowHeight / 2.0;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double scale = Math.pow(1.15, e.getPreciseWheelRotation());
        double mxReal = toRealX(e.getX());
        double myReal = toRealY(e.getY());
        double sx = (windowX - mxReal) / windowWidth;
        double sy = (windowY - myReal) / windowHeight;
        windowWidth *= scale;
        windowHeight *= scale;
        windowX = mxReal + sx * windowWidth;
        windowY = myReal + sy * windowHeight;
    }
}
