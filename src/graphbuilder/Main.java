package graphbuilder;

import javax.swing.JFrame;
import graph.Window;

/**
 * Головний клас, який задає властивості для вікна програми та ініціалізує
 * роботу самої програми з побудови графіків.
 *
 * @author Oleksandr
 */
public class Main {

    /**
     * Точка входу в програму.
     *
     * @param args - параметри командної стрічки
     */
    public static void main(String[] args) {
        Window window = new Window();

        JFrame frame = new JFrame("Graph Builder");
        frame.add(window);
        frame.setResizable(false);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(window).start();
    }
}
