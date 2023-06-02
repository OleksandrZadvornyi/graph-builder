package graphbuilder;

import javax.swing.JFrame;
import graph.Window;
import javax.swing.ImageIcon;

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
        
        ImageIcon img = new ImageIcon("E:\\Dev\\MyProjects\\NetBeansProjects\\GraphBuilder\\src\\line-chart.png");
        frame.setIconImage(img.getImage());
        frame.setVisible(true);

        new Thread(window).start();
    }
}
