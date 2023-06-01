package graphbuilder;

import javax.swing.JFrame;

import graph.Window;

public class Main {

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
