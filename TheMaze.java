package ShortestPath;

import javax.swing.*;

public class TheMaze {

    public static JFrame mazeFrame;  // The main form of the program

    public static void main(String[] args) {
        int width  = 693;
        int height = 545;
        mazeFrame = new JFrame("Maze");
        mazeFrame.setContentPane(new MazePanel(width,height));
        mazeFrame.pack();
        mazeFrame.setResizable(false);
        mazeFrame.setLocation(500,130);
        mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mazeFrame.setVisible(true);
    }
}
