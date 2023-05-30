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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import graph.expression.Function;
import graph.parser.ExpressionParser;
import java.awt.event.MouseListener;

public class Window extends JPanel implements MouseWheelListener, KeyListener, Runnable {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    private BufferedImage buff;
    private Graphics2D g2d;

    private ExpressionParser parser;
    private Function function;
    private Function function1;

    private double windowX, windowY, windowWidth, windowHeight;
    private Point mousePt;

    private String textBox;
    private String textBox1;

    private boolean textBoxActive = true;
    private boolean textBox1Active = false;

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
        });

        setFocusable(true);
        requestFocusInWindow();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setMaximumSize(new Dimension(WIDTH, HEIGHT));

        buff = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g2d = buff.createGraphics();

        parser = new ExpressionParser();
        textBox = "0";
        function = parser.parse(textBox);

        textBox1 = "0";
        function1 = parser.parse(textBox1);

        windowX = 0.0;
        windowY = 0.0;
        windowHeight = 2.0;
        windowWidth = windowHeight * WIDTH / HEIGHT;
    }

    // Time variables
    private double yVar = 0.0;	// Constantly increasing
    private double zVar = 0.0;	// Cycles smoothly from -1 to 1

    private synchronized void updateDT(double dt) {
        yVar += dt;
        zVar = Math.sin(yVar);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        synchronized (this) {
            ////////////////////////////////////////
            List<Double> bxs = new ArrayList<>();
            List<Double> bys = new ArrayList<>();

            for (int x = 0; x < WIDTH; x++) {
                double xx = toRealX(x);

                double yy = 0.0;
                if (function != null) {
                    yy = function.evaluateAt(xx, yVar, zVar);
                }

                double scaledX = x;
                double scaledY = toScreenY(yy);
                scaledY = Math.min(Math.max(scaledY, -5), HEIGHT + 5);

                bxs.add(scaledX);
                bys.add(scaledY);
            }

            int[] bxa = new int[bxs.size()];
            int[] bya = new int[bys.size()];

            for (int i = 0; i < bxa.length; i++) {
                bxa[i] = bxs.get(i).intValue();
            }
            for (int i = 0; i < bya.length; i++) {
                bya[i] = bys.get(i).intValue();
            }

            ////////////////////////////////////////
            List<Double> rxs = new ArrayList<>();
            List<Double> rys = new ArrayList<>();

            for (int x = 0; x < WIDTH; x++) {
                double xx = toRealX(x);

                double yy = 0.0;
                if (function1 != null) {
                    yy = function1.evaluateAt(xx, yVar, zVar);
                }

                double scaledX = x;
                double scaledY = toScreenY(yy);
                scaledY = Math.min(Math.max(scaledY, -5), HEIGHT + 5);

                rxs.add(scaledX);
                rys.add(scaledY);
            }

            int[] rxa = new int[rxs.size()];
            int[] rya = new int[rys.size()];

            for (int i = 0; i < rxa.length; i++) {
                rxa[i] = rxs.get(i).intValue();
            }
            for (int i = 0; i < rya.length; i++) {
                rya[i] = rys.get(i).intValue();
            }

            ///////////////////////////////////////
            g2d.setColor(Color.BLACK);

            // Намалювати вісь X
            int xAxisY = toScreenY(0.0);
            g2d.drawLine(0, xAxisY, WIDTH, xAxisY);

            // Намалювати вісь Y
            int yAxisX = toScreenX(0.0);
            g2d.drawLine(yAxisX, 0, yAxisX, HEIGHT);

            // Намалювати поділки
            drawDivisions();

            // Намалювати графіки
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(0, 0, 250));
            g2d.drawPolyline(bxa, bya, bxa.length);
            g2d.setColor(new Color(255, 0, 0));
            g2d.drawPolyline(rxa, rya, rxa.length);

            // Намалювати стрічки для вводу рівнянь
            g2d.setFont(new Font("courier new", Font.ITALIC, 40));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, HEIGHT - g2d.getFontMetrics().getHeight(), WIDTH, HEIGHT);
            g2d.fillRect(0, HEIGHT - g2d.getFontMetrics().getHeight() - 50, WIDTH, 50);
            g2d.setColor(Color.BLACK);
            g2d.drawString("f(x) = " + textBox, 0.0f, HEIGHT - 10.0f);
            g2d.drawString("f(x) = " + textBox1, 0.0f, HEIGHT - 60.0f);

            g2d.drawString("x", 0, xAxisY - 10);
            g2d.drawString("y", yAxisX + 10, g2d.getFontMetrics().getHeight() - 20);

            if (textBoxActive) {
                g2d.setColor(Color.blue);
                g2d.drawRect(0, HEIGHT - g2d.getFontMetrics().getHeight(), WIDTH - 2, 45);
            } else if (textBox1Active) {
                g2d.setColor(Color.red);
                g2d.drawRect(0, HEIGHT - g2d.getFontMetrics().getHeight() - 50, WIDTH - 2, 45);
            }

            ////////////////////////////////////////
            for (int i = 0; i < rxa.length; i++) {
                if (rxa[i] == bxa[i] && rya[i] == bya[i]) {
                    g2d.drawString(rxa[i] + "," + rya[i], 50, 50);
                }
            }
        }

        g.drawImage(buff, 0, 0, null);
    }

    private void drawDivisions() {
        g2d.setFont(new Font("courier new", Font.ITALIC, 25));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

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

    @Override
    public void run() {
        boolean running = true;

        long oldTime = 0;
        double dt = 0.0;

        while (running) {

            long newTime = System.nanoTime();
            dt = (newTime - oldTime) / 1000000000.0;
            oldTime = newTime;

            updateDT(dt);
            repaint();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (textBoxActive) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (textBox.length() > 0) {
                    textBox = textBox.substring(0, textBox.length() - 1);
                }
            } else if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '^' || e.getKeyChar() == '-'
                    || e.getKeyChar() == '+' || e.getKeyChar() == '*' || e.getKeyChar() == '/' || e.getKeyChar() == '('
                    || e.getKeyChar() == ')' || e.getKeyChar() == '%' || e.getKeyChar() == ',' || e.getKeyChar() == '.') {
                textBox += e.getKeyChar();
            }
        } else if (textBox1Active) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (textBox1.length() > 0) {
                    textBox1 = textBox1.substring(0, textBox1.length() - 1);
                }
            } else if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '^' || e.getKeyChar() == '-'
                    || e.getKeyChar() == '+' || e.getKeyChar() == '*' || e.getKeyChar() == '/' || e.getKeyChar() == '('
                    || e.getKeyChar() == ')' || e.getKeyChar() == '%' || e.getKeyChar() == ',' || e.getKeyChar() == '.') {
                textBox1 += e.getKeyChar();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            function = parser.parse(textBox);
            function1 = parser.parse(textBox1);
            if (function == null) {
                textBox = "";
            }
            if (function1 == null) {
                textBox1 = "";
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private double bottom() {
        return windowY - halfWindowHeight();
    }

    private double right() {
        return windowX - halfWindowWidth();
    }

    private double toRealX(int screenX) {
        return screenX / (double) WIDTH * windowWidth + right();
    }

    private double toRealY(int screenY) {
        return (HEIGHT - screenY) / (double) HEIGHT * windowHeight + bottom();
    }

    private int toScreenX(double realX) {
        return (int) ((realX - right()) / windowWidth * WIDTH);
    }

    private int toScreenY(double realY) {
        return HEIGHT - (int) ((realY - bottom()) / windowHeight * HEIGHT);
    }

    private double halfWindowWidth() {
        return windowWidth / 2.0;
    }

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
