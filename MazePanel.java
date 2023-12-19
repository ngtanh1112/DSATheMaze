package ShortestPath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;
import static ShortestPath.TheMaze.mazeFrame;

public class MazePanel extends JPanel {
    JTextField rowsField, columnsField;

    int rows    = 50;       // the number of rows of the grid
    int columns = 50;           // the number of columns of the grid
    int squareSize = 500/rows;  // the cell size in pixels, with the space of grid is 500 pixels
    ArrayList<Cell> openSet   = new ArrayList<Cell>();// the list of cells that were not expanded
    ArrayList<Cell> closedSet = new ArrayList<Cell>();// the list of cells that were expanded and do not need to pass again
    ArrayList<Cell> graph     = new ArrayList<Cell>();// the graph to be explored by Dijkstra's algorithm
    Cell robotStart; // the initial position of the robot
    Cell targetPos;  // the position of the target
    JLabel message;  // message to the user buttons for selecting the algorithm
    JRadioButton dfs, bfs, dijkstra;
    int[][] grid;        // the grid
    boolean found;       // flag that the goal was found
    boolean searching;   // flag that the search is in progress
    boolean endOfSearch; // flag that the search came to an end
    int delay;           // time delay of animation (in ms)
    int expanded;        // the number of nodes that have been expanded

    // the object that controls the animation
    RepaintAction action = new RepaintAction();
    Timer timer;
    JLabel groupName = new JLabel("GROUP 16");

    //Instruction Variables
    protected final static String
            msgDrawAndSelect =
                    "Click 'Maze', then click 'Animation'",
            msgClearToStop =
                    "Click 'Clear' to stop",
            msgNoSolution =
                    "There is no path to the target !!!";

    //Index that represents the cell
    protected final static int
            INFINITY = Integer.MAX_VALUE, // The representation of the infinite
            EMPTY    = 0,  // empty cell
            OBST     = 1,  // cell with obstacle
            ROBOT    = 2,  // the position of the robot
            TARGET   = 3,  // the position of the target
            FRONTIER = 4,  // cells that form the frontier (OPEN SET)
            CLOSED   = 5,  // cells that form the CLOSED SET (cells that are explored and do not need to pass again)
            ROUTE    = 6;  // cells that form the robot to target path

    ImageIcon icon = new ImageIcon("maze.png");
    Image resizedImage = icon.getImage().getScaledInstance(170, 125, Image.SCALE_SMOOTH);
    ImageIcon resizedIcon = new ImageIcon(resizedImage);
    JLabel imageLabel = new JLabel();

    /**
     * The construction method:
     * width:  the width of the panel.
     * height: the height of the panel.
     */
    public MazePanel(int width, int height) {

        this.setLayout(null);
        this.setPreferredSize( new Dimension(width,height) ); // set the size of the panel
        this.setBorder(BorderFactory.createMatteBorder(5,5,5,5,Color.BLACK)); // set the border

        MouseHandler listener = new MouseHandler();
        this.addMouseListener(listener); // add listener
        this.addMouseMotionListener(listener); // to know the mouse position

        grid = new int[rows][columns]; // create a matrix

        // Create the contents of the panel
        //Text
        message = new JLabel(msgDrawAndSelect, JLabel.CENTER); // create the instructional message
        message.setForeground(Color.blue); // set the color of the message
        message.setFont(new Font("Helvetica",Font.PLAIN,16)); // set font

        JLabel rowsLbl = new JLabel("ROWS", JLabel.RIGHT);
        rowsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

        rowsField = new JTextField(); // create the field to edit number of rows
        rowsField.setHorizontalAlignment(JTextField.CENTER);
        rowsField.setText(Integer.toString(rows));

        JLabel columnsLbl = new JLabel("COLUMNS", JLabel.RIGHT);
        columnsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

        columnsField = new JTextField(); // create the field to edit number of columns
        columnsField.setHorizontalAlignment(JTextField.CENTER);
        columnsField.setText(Integer.toString(columns));

        JButton resetButton = new JButton("NEW GRID"); // create the button to reset to origin
        resetButton.setBackground(Color.lightGray);
        resetButton.setToolTipText("Create a new grid");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        JButton mazeButton = new JButton("MAKE MAZE"); // create the button to make maze
        mazeButton.addActionListener(new ActionHandler());
        mazeButton.setBackground(Color.lightGray);
        mazeButton.setToolTipText("Create a new maze");
        mazeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mazeButtonActionPerformed(evt);
            }
        });

        JButton clearButton = new JButton("CLEAR"); // create the button to
        clearButton.addActionListener(new ActionHandler());
        clearButton.setBackground(Color.lightGray);
        clearButton.setToolTipText("Clear the current path");

        JButton animationButton = new JButton("ANIMATION");
        animationButton.addActionListener(new ActionHandler());
        animationButton.setBackground(Color.lightGray);
        animationButton.setToolTipText("Animation algorithm");

        delay = 0;

        // ButtonGroup that contains the five RadioButtons choosing algorithm
        ButtonGroup algoGroup = new ButtonGroup();

        dfs = new JRadioButton("DFS");
        dfs.setToolTipText("Depth First Search algorithm");
        algoGroup.add(dfs);
        dfs.addActionListener(new ActionHandler());

        bfs = new JRadioButton("BFS");
        bfs.setToolTipText("Breadth First Search algorithm");
        algoGroup.add(bfs);
        bfs.addActionListener(new ActionHandler());

        dijkstra = new JRadioButton("Dijkstra");
        dijkstra.setToolTipText("Dijkstra's algorithm");
        algoGroup.add(dijkstra);
        dijkstra.addActionListener(new ActionHandler());

        dfs.setSelected(true);  // DFS is initially selected

        JLabel robot = new JLabel("Robot", JLabel.CENTER);
        robot.setForeground(Color.red);
        robot.setFont(new Font("Helvetica",Font.PLAIN,14));

        JLabel target = new JLabel("Target", JLabel.CENTER);
        target.setForeground(new Color(58, 144, 255));
        target.setFont(new Font("Helvetica",Font.PLAIN,14));

        JLabel frontier = new JLabel("Frontier", JLabel.CENTER);
        frontier.setForeground(new Color(255, 0, 128));
        frontier.setFont(new Font("Helvetica",Font.PLAIN,14));

        JLabel closed = new JLabel("Closed set", JLabel.CENTER);
        closed.setForeground(new Color(255, 171, 187));
        closed.setFont(new Font("Helvetica",Font.PLAIN,14));

        JButton aboutButton = new JButton("INFORMATION");
        aboutButton.addActionListener(this::aboutButtonActionPerformed);
        aboutButton.setBackground(Color.lightGray);

        imageLabel.setIcon(resizedIcon);
        groupName.setForeground(Color.RED);
        groupName.setFont(new Font("Helvetica",Font.BOLD,20));

        // Add the contents of the panel
        add(groupName);
        add(imageLabel);
        add(message);
        add(rowsLbl);
        add(rowsField);
        add(columnsLbl);
        add(columnsField);
        add(resetButton);
        add(mazeButton);
        add(clearButton);
        add(animationButton);
        add(dfs);
        add(bfs);
        add(dijkstra);
        add(robot);
        add(target);
        add(frontier);
        add(closed);
        add(aboutButton);

        // Edit the sizes and positions (setBounds method used to adjust the size and position)
        groupName.setBounds(555, 5, 150, 25);
        imageLabel.setBounds(510, 35, 170, 125);
        message.setBounds(0, 515, 500, 23);
        rowsLbl.setBounds(500, 175, 70, 25);
        rowsField.setBounds(510, 200, 80, 30);
        columnsLbl.setBounds(600, 175, 70, 25);
        columnsField.setBounds(600, 200, 80, 30);
        resetButton.setBounds(510, 240, 170, 25);
        mazeButton.setBounds(510, 270, 170, 25);
        clearButton.setBounds(510, 300, 170, 25);
        animationButton.setBounds(510, 330, 170, 25);
        dfs.setBounds(510, 370, 50, 25);
        bfs.setBounds(630, 370, 50, 25);
        dijkstra.setBounds(560, 400, 70, 25);
        robot.setBounds(520, 465, 80, 25);
        target.setBounds(605, 465, 80, 25);
        frontier.setBounds(520, 485, 80, 25);
        closed.setBounds(605, 485, 80, 25);
        aboutButton.setBounds(510, 510, 170, 25);

        // Create the timer
        timer = new Timer(delay, action);

        // We attach to cells in the grid initial values.
        fillGrid();
    } // End of MazePanel constructor

    /** Gives initial values for the cells in the grid.
     * With the first click on button 'Clear' clears the data
     * of any search was performed (Frontier, Closed Set, Route)
     * and leaves intact the obstacles and the robot and target positions
     * in order to be able to run another algorithm
     * with the same data.
     * With the second click removes any obstacles also.*/
    private void fillGrid() {
        if (searching || endOfSearch) { // Clear the current search
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (grid[r][c] == FRONTIER || grid[r][c] == CLOSED || grid[r][c] == ROUTE) {
                        grid[r][c] = EMPTY;
                    }
                    if (grid[r][c] == ROBOT){
                        robotStart = new Cell(r,c);
                    }
                    if (grid[r][c] == TARGET){
                        targetPos = new Cell(r,c);
                    }
                }
            }
            searching = false;
        } else { // Clear all
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    grid[r][c] = EMPTY;
                }
            }
            robotStart = new Cell(rows-2,1); // Set to the original position
            targetPos = new Cell(1,columns-2); // Set to the original position
        }
        expanded = 0;
        found = false;
        endOfSearch = false;

        // Remove all the data of any search was performed
        openSet.removeAll(openSet);
        openSet.add(robotStart);
        closedSet.removeAll(closedSet);

        grid[targetPos.row][targetPos.col] = TARGET;
        grid[robotStart.row][robotStart.col] = ROBOT;
        message.setText(msgDrawAndSelect);
        timer.stop();
        repaint();
    } // end fillGrid()

    /**
     * Creates a new clean grid or a new maze when we click on "NEW GRID" button
     */
    private void initializeGrid(Boolean makeMaze) {
        // Get the number of rows and columns in the TextField
        rows = Integer.parseInt(rowsField.getText());
        columns = Integer.parseInt(columnsField.getText());

        // Set the size of the grid
        squareSize = 500/(Math.max(rows, columns)); // This will ensure that the grid will still being displayed in 500 pixels

        if (makeMaze && rows % 2 == 0) {
            rows -= 1;
        }
        if (makeMaze && columns % 2 == 0) {
            columns -= 1;
        }

        // If we create a new maze, then we need to set all thing to original
        grid = new int[rows][columns];
        robotStart = new Cell(rows-2,1);
        targetPos = new Cell(1,columns-2);
        dfs.setEnabled(true);
        dfs.setSelected(true);
        bfs.setEnabled(true);
        dijkstra.setEnabled(true);
        if (makeMaze) {
            new MyMaze(rows/2,columns/2);
        } else {
            fillGrid();
        }
    } // end initializeGrid()

    /**
     * Creates a random, perfect (without cycles) maze
     * The code of the class is the answer given
     * by user DoubleMx2 on August 25 to a question posted by user nazar_art at stackoverflow.com:
     * http://stackoverflow.com/questions/18396364/maze-generation-arrayindexoutofboundsexception
     */
    private class MyMaze {
        private int dimensionX, dimensionY; // dimension of maze
        private int gridDimensionX, gridDimensionY; // dimension of output grid
        private char[][] mazeGrid; // output grid
        private Cell[][] cells; // 2d array of Cells
        private Random random = new Random(); // The random object

        // Constructor
        public MyMaze(int xDimension, int yDimension) {
            dimensionX = xDimension;
            dimensionY = yDimension;
            gridDimensionX = xDimension * 2 + 1;
            gridDimensionY = yDimension * 2 + 1;
            mazeGrid = new char[gridDimensionX][gridDimensionY];
            init();
            generateMaze();
        }

        private void init() {
            // create cells
            cells = new Cell[dimensionX][dimensionY];
            for (int x = 0; x < dimensionX; x++) {
                for (int y = 0; y < dimensionY; y++) {
                    cells[x][y] = new Cell(x, y, false); // create cell (see Cell constructor)
                }
            }
        }

        // inner class to represent a cell
        private class Cell {
            int x, y; // coordinates
            // cells this cell is connected to
            ArrayList<Cell> neighbors = new ArrayList<>();
            // impassable cell
            boolean wall = true;
            // if true, has yet to be used in generation
            boolean open = true;
            // construct Cell at x, y
            Cell(int x, int y) {
                this(x, y, true);
            }
            // construct Cell at x, y and with whether it isWall
            Cell(int x, int y, boolean isWall) {
                this.x = x;
                this.y = y;
                this.wall = isWall;
            }
            // add a neighbor to this cell, and this cell as a neighbor to the other
            void addNeighbor(Cell other) {
                if (!this.neighbors.contains(other)) { // avoid duplicates
                    this.neighbors.add(other);
                }
                if (!other.neighbors.contains(this)) { // avoid duplicates
                    other.neighbors.add(this);
                }
            }
            // used in updateGrid()
            boolean isCellBelowNeighbor() {
                return this.neighbors.contains(new Cell(this.x, this.y + 1));
            }
            // used in updateGrid()
            boolean isCellRightNeighbor() {
                return this.neighbors.contains(new Cell(this.x + 1, this.y));
            }
            // useful Cell equivalence
            @Override
            public boolean equals(Object other) {
                if (!(other instanceof Cell)) return false;
                Cell otherCell = (Cell) other;
                return (this.x == otherCell.x && this.y == otherCell.y);
            }

            // should be overridden with equals
            @Override
            public int hashCode() {
                // random hash code method designed to be usually unique
                return this.x + this.y * 256;
            }

        }
        // generate from upper left (In computing the y increases down often)
        private void generateMaze() {
            generateMaze(0, 0);
        }
        // generate the maze from coordinates x, y
        private void generateMaze(int x, int y) {
            generateMaze(getCell(x, y)); // generate from Cell
        }
        private void generateMaze(Cell startAt) {
            // don't generate from cell not there
            if (startAt == null) return;
            startAt.open = false; // indicate cell closed for generation
            ArrayList<Cell> cellsList = new ArrayList<>();
            cellsList.add(startAt);

            while (!cellsList.isEmpty()) {
                Cell cell;
                // this is to reduce but not completely eliminate the number
                // of long twisting halls with short easy to detect branches
                // which results in easy mazes
                if (random.nextInt(10)==0)
                    cell = cellsList.remove(random.nextInt(cellsList.size()));
                else cell = cellsList.remove(cellsList.size() - 1);
                // for collection
                ArrayList<Cell> neighbors = new ArrayList<>();
                // cells that could potentially be neighbors
                Cell[] potentialNeighbors = new Cell[]{
                        getCell(cell.x + 1, cell.y),
                        getCell(cell.x, cell.y + 1),
                        getCell(cell.x - 1, cell.y),
                        getCell(cell.x, cell.y - 1)
                };
                for (Cell other : potentialNeighbors) {
                    // skip if outside, is a wall or is not opened
                    if (other==null || other.wall || !other.open) continue;
                    neighbors.add(other);
                }
                if (neighbors.isEmpty()) continue;
                // get random cell
                Cell selected = neighbors.get(random.nextInt(neighbors.size()));
                // add as neighbor
                selected.open = false; // indicate cell closed for generation
                cell.addNeighbor(selected);
                cellsList.add(cell);
                cellsList.add(selected);
            }
            updateGrid();
        }
        // used to get a Cell at x, y; returns null out of bounds
        public Cell getCell(int x, int y) {
            try {
                return cells[x][y];
            } catch (ArrayIndexOutOfBoundsException e) { // catch out of bounds
                return null;
            }
        }
        // draw the maze
        public void updateGrid() {
            char backChar = ' ', wallChar = 'X', cellChar = ' ';
            // fill background
            for (int x = 0; x < gridDimensionX; x ++) {
                for (int y = 0; y < gridDimensionY; y ++) {
                    mazeGrid[x][y] = backChar;
                }
            }
            // build walls
            for (int x = 0; x < gridDimensionX; x ++) {
                for (int y = 0; y < gridDimensionY; y ++) {
                    if (x % 2 == 0 || y % 2 == 0)
                        mazeGrid[x][y] = wallChar;
                }
            }
            // make meaningful representation
            for (int x = 0; x < dimensionX; x++) {
                for (int y = 0; y < dimensionY; y++) {
                    Cell current = getCell(x, y);
                    int gridX = x * 2 + 1, gridY = y * 2 + 1;
                    mazeGrid[gridX][gridY] = cellChar;
                    if (current.isCellBelowNeighbor()) {
                        mazeGrid[gridX][gridY + 1] = cellChar;
                    }
                    if (current.isCellRightNeighbor()) {
                        mazeGrid[gridX + 1][gridY] = cellChar;
                    }
                }
            }

            // We create a clean grid ...
            searching = false;
            endOfSearch = false;
            fillGrid();
            // ... and copy into it the positions of obstacles
            // created by the maze construction
            for (int x = 0; x < gridDimensionX; x++) {
                for (int y = 0; y < gridDimensionY; y++) {
                    if (mazeGrid[x][y] == wallChar && grid[x][y] != ROBOT && grid[x][y] != TARGET){
                        grid[x][y] = OBST;
                    }
                }
            }
            repaint();
        }
    } // end class MyMaze

    /**
     * The class that is responsible for the animation
     */
    private class RepaintAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            // Here we decide whether we can continue or not
            // the search with 'Animation'.
            // In the case of DFS, BFS algorithms
            // If OPEN SET = null, then terminate. There is no solution.
            if ((dijkstra.isSelected() && graph.isEmpty()) || (!dijkstra.isSelected() && openSet.isEmpty())) {
                endOfSearch = true;
                grid[robotStart.row][robotStart.col]=ROBOT;
                message.setText(msgNoSolution);
            } else {
                expandNode();
                if (found) {
                    timer.stop();
                    endOfSearch = true;
                    plotRoute();
                }
            }
            repaint();
        }
    }

    /**
     * Class that handles mouse movements as we "paint"
     * obstacles or move the robot and/or target.
     */
    private class MouseHandler implements MouseListener, MouseMotionListener {
        private int cur_row, cur_col, cur_val;
        @Override
        public void mousePressed(MouseEvent evt) {
            int row = evt.getY() / squareSize;
            int col = evt.getX() / squareSize;
            if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found) {
                cur_row = row;
                cur_col = col;
                cur_val = grid[row][col];
                if (cur_val == EMPTY){
                    grid[row][col] = OBST;
                }
                if (cur_val == OBST){
                    grid[row][col] = EMPTY;
                }
            }
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            int row = evt.getY() / squareSize;
            int col = evt.getX() / squareSize;
            if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found){
                if ((cur_val == ROBOT || cur_val == TARGET)){
                    int new_val = grid[row][col];
                    if (new_val == EMPTY){
                        grid[row][col] = cur_val;
                        if (cur_val == ROBOT) {
                            robotStart.row = row;
                            robotStart.col = col;
                        } else {
                            targetPos.row = row;
                            targetPos.col = col;
                        }
                        grid[cur_row][cur_col] = new_val;
                        cur_row = row;
                        cur_col = col;
                        if (cur_val == ROBOT) {
                            robotStart.row = cur_row;
                            robotStart.col = cur_col;
                        } else {
                            targetPos.row = cur_row;
                            targetPos.col = cur_col;
                        }
                        cur_val = grid[row][col];
                    }
                } else if (grid[row][col] != ROBOT && grid[row][col] != TARGET){
                    grid[row][col] = OBST;
                }
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent evt) { }
        @Override
        public void mouseEntered(MouseEvent evt) { }
        @Override
        public void mouseExited(MouseEvent evt) { }
        @Override
        public void mouseMoved(MouseEvent evt) { }
        @Override
        public void mouseClicked(MouseEvent evt) { }

    } // end class MouseHandler

    /**
     * When the user presses a button performs the corresponding functionality
     */
    private class ActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String cmd = evt.getActionCommand();
            if (cmd.equals("CLEAR")) {
                fillGrid();
                dfs.setEnabled(true);
                bfs.setEnabled(true);
                dijkstra.setEnabled(true);
            } else if (cmd.equals("ANIMATION") && !endOfSearch) {
                if (!searching && dijkstra.isSelected()) {
                    initializeDijkstra();
                }
                searching = true;
                message.setText(msgClearToStop);
                dfs.setEnabled(false);
                bfs.setEnabled(false);
                dijkstra.setEnabled(false);
                timer.start();
            }
        }
    }

    /**
     * Function executed if the user presses the button "NEW GRID"
     */
    private void resetButtonActionPerformed(ActionEvent evt) {
        initializeGrid(false);
    }

    /**
     * Function executed if the user presses the button "MAKE MAZE"
     */
    protected void mazeButtonActionPerformed(ActionEvent evt) {
        initializeGrid(true);
    } // end mazeButtonActionPerformed()

    /**
     * Information Dialog
     */
    private static class InformationDialog extends JDialog{

        public InformationDialog(Frame parent){
            super(parent);

            int width = 350;
            int height = 190;
            super.setSize(width,height);

            // the InformationDialog is located in the center of the screen
            super.setLocationRelativeTo(null);

            super.setResizable(false);
            super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JLabel title = new JLabel("THE MAZE", JLabel.CENTER);
            title.setFont(new Font("Helvetica",Font.BOLD,24));
            title.setForeground(Color.RED);

            JLabel subject = new JLabel("Data Structures and Algorithms", JLabel.CENTER);
            subject.setFont(new Font("Helvetica",Font.BOLD,14));

            JLabel programmer_01 = new JLabel("Nguyen Tuan Anh: Code and Report", JLabel.CENTER);
            programmer_01.setFont(new Font("Helvetica",Font.PLAIN,16));

            JLabel programmer_02 = new JLabel("Dinh Truong An: Theory and Slides", JLabel.CENTER);
            programmer_02.setFont(new Font("Helvetica",Font.PLAIN,16));

            JLabel sourceCode = new JLabel("Code and documentation:", JLabel.CENTER);
            sourceCode.setFont(new Font("Helvetica",Font.PLAIN,14));

            JLabel linkReport = new JLabel("Report", JLabel.CENTER);
            linkReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
            linkReport.setFont(new Font("Helvetica",Font.ITALIC,16));
            linkReport.setForeground(Color.BLUE);
            linkReport.setToolTipText
                    ("Click this link to retrieve the report");

            JLabel linkSlides = new JLabel("Slides", JLabel.CENTER);
            linkSlides.setCursor(new Cursor(Cursor.HAND_CURSOR));
            linkSlides.setFont(new Font("Helvetica",Font.ITALIC,16));
            linkSlides.setForeground(Color.BLUE);
            linkSlides.setToolTipText
                    ("Click this link to retrieve the slides");

            JLabel dummy = new JLabel("");

            super.add(title);
            super.add(subject);
            super.add(programmer_01);
            super.add(programmer_02);
            super.add(linkReport);
            super.add(linkSlides);
            super.add(dummy);

            goReport(linkReport);
            goSlides(linkSlides);


            title.     setBounds(5,  0, 330, 30);
            subject.   setBounds(5, 30, 330, 20);
            programmer_01.setBounds(5, 55, 330, 20);
            programmer_02.     setBounds(5, 80, 330, 20);
            linkReport.      setBounds(5,105, 330, 20);
            linkSlides.     setBounds(5,130, 330, 20);
        }

        private void goReport(JLabel reportLink) {
            reportLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://drive.google.com/file/d/1LEomYnOcwXLjmnBQzfB5-Ltxn3NXgtN1/view?usp=sharing"));
                    } catch (URISyntaxException | IOException ex) {
                        // The link has a problem
                    }
                }
            });
        }

        private void goSlides(JLabel slidesLink) {
            slidesLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://web.facebook.com/profile.php?id=100088948717251"));
                    } catch (URISyntaxException | IOException ex) {
                        // The link has a problem
                    }
                }
            });
        }


    } // end class AboutBox
    private void aboutButtonActionPerformed(ActionEvent evt) {
        InformationDialog aboutBox = new InformationDialog(mazeFrame);
        aboutBox.setVisible(true);
    } // end aboutButtonActionPerformed()

    /**
     * Expands a node and creates his successors
     */
    private void expandNode() {
        // Dijkstra's algorithm to handle separately
        if (dijkstra.isSelected()){
            Cell u;
            // while graph is not empty:
            if (graph.isEmpty()){
                return;
            }
            // u = vertex in Q (graph) with the smallest distance in dist[] ;
            // remove u from Q (graph);
            u = graph.remove(0);
            // Add vertex u in closed set
            closedSet.add(u);
            // If target has been found ...
            if (u.row == targetPos.row && u.col == targetPos.col){
                found = true;
                return;
            }
            // Counts nodes that have expanded.
            expanded++;
            // Update the color of the cell
            grid[u.row][u.col] = CLOSED;
            // if dist[u] = infinity:
            if (u.dist == INFINITY){
                // so there is no solution.
                // break;
                return;
            }
            // Create the neighbors of u
            ArrayList<Cell> neighbors = createSuccessors(u, false);
            // for each neighbor v of u:
            for (Cell v: neighbors){
                // alt := dist[u] + dist_between(u, v) ;
                int alt = u.dist + distBetween(u,v);
                // if alt < dist[v]:
                if (alt <= v.dist){
                    // dist[v] = alt ;
                    v.dist = alt;
                    // previous[v] = u ;
                    v.prev = u;
                    // Update the color of the cell
                    grid[v.row][v.col] = FRONTIER;
                    // decrease-key v in Q;
                    // (sort list of nodes with respect to dist)
                    Collections.sort(graph, new CellComparatorByDist());
                }
            }
            // The handling of the other 2 algorithms
        } else {
            Cell current;
            current = openSet.remove(0);
            // Remove it in OPEN SET and add it to CLOSED SET.
            closedSet.add(0,current);
            // Update the color of the cell
            grid[current.row][current.col] = CLOSED;
            // If the selected node is the target,
            if (current.row == targetPos.row && current.col == targetPos.col) {
                // then terminate
                Cell last = targetPos;
                last.prev = current.prev;
                closedSet.add(last);
                found = true;
                return;
            }
            // Count nodes that have been expanded.
            expanded++;
            // Create the successors of current
            // Each successor has a pointer to the current, as its predecessor.
            // In the case of DFS and BFS algorithms, successors should not
            // belong neither to the OPEN SET nor the CLOSED SET.
            ArrayList<Cell> successors;
            successors = createSuccessors(current, false);
            for (Cell cell: successors){
                // ... if we are running DFS ...
                if (dfs.isSelected()) {
                    // ... add the successor at the beginning of the list OPEN SET
                    openSet.add(0, cell);
                    // Update the color of the cell
                    grid[cell.row][cell.col] = FRONTIER;
                    // ... if we are running BFS ...
                } else if (bfs.isSelected()){
                    // ... add the successor at the end of the list OPEN SET
                    openSet.add(cell);
                    // Update the color of the cell
                    grid[cell.row][cell.col] = FRONTIER;
                }
            }
        }
        repaint();
    } //end expandNode()

    /**
     * Creates the successors of a state/cell
     * current:       the cell for which we ask successors
     * makeConnected: flag that indicates that we are interested only on the coordinates of cells and not on the label 'dist' (concerns only Dijkstra's)
     * return:              the successors of the cell as a list
     */
    private ArrayList<Cell> createSuccessors(Cell current, boolean makeConnected) {
        int r = current.row;
        int c = current.col;
        // We create an empty list for the successors of the current cell.
        ArrayList<Cell> temp = new ArrayList<>();
        // The priority is:
        // 1: Up 2: Right 3: Down 4: Left

        // r >0: If not at the topmost limit of the grid and
        // grid[r-1][c] != OBST and the up-side cell is not an obstacle
        if (r > 0 && grid[r-1][c] != OBST && (isInList(openSet, new Cell(r - 1, c)) == -1 && isInList(closedSet, new Cell(r - 1, c)) == -1)) {
            Cell cell = new Cell(r-1,c);
            // In the case of Dijkstra's algorithm we can not append to
            // the list of successors the "naked" cell we have just created.
            // The cell must be accompanied by the label 'dist',
            // so we need to track it down through the list 'graph'
            // and then copy it back to the list of successors.
            // The flag makeConnected is necessary to be able
            // the present method createSuccessors() to collaborate
            // with the method findConnectedComponent(), which creates
            // the connected component when Dijkstra's initializes.
            if (dijkstra.isSelected()) {
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // ... update the pointer of the up-side cell so it points the current one ...
                cell.prev = current;
                // ... and add the up-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        // If not at the rightmost limit of the grid
        // and the right-side cell is not an obstacle ...
        if (c < columns-1 && grid[r][c+1] != OBST && (isInList(openSet, new Cell(r, c + 1)) == -1 && isInList(closedSet, new Cell(r, c + 1)) == -1)) {
            Cell cell = new Cell(r,c+1);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // ... update the pointer of the right-side cell so it points the current one ...
                cell.prev = current;
                // ... and add the right-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        // If not at the lowermost limit of the grid
        // and the down-side cell is not an obstacle ...
        if (r < rows-1 && grid[r+1][c] != OBST && (isInList(openSet, new Cell(r + 1, c)) == -1 && isInList(closedSet, new Cell(r + 1, c)) == -1)) {
            Cell cell = new Cell(r+1,c);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // update the pointer of the down-side cell, so it points the current one ...
                cell.prev = current;
                // and add the down-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        // If not at the leftmost limit of the grid
        // and the left-side cell is not an obstacle ...
        if (c > 0 && grid[r][c-1] != OBST &&
                (isInList(openSet, new Cell(r, c - 1)) == -1 &&
                        isInList(closedSet, new Cell(r, c - 1)) == -1)) {
            Cell cell = new Cell(r,c-1);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // update the pointer of the left-side cell so it points the current one ...
                cell.prev = current;
                // and add the left-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
          // When the DFS algorithm is selected, we want to display the expanding on the screen from the bottom of the grid to the top
          // instead of the left to the right, so we just have to reverse the neighbors of the current cell. However, this is just
          // a optional choice.
        if (dfs.isSelected()){
            Collections.reverse(temp);
        }
        return temp;
    } // end createSuccessors()

    /**
     * Returns the index of the cell 'current' in the list 'list'
     * list:    the list in which we seek
     * current: the cell we are looking for
     * return:        the index of the cell in the list
     * if the cell is not found returns -1
     */
    private int isInList(ArrayList<Cell> list, Cell current){
        int index = -1;
        for (int i = 0 ; i < list.size(); i++) {
            if (current.row == list.get(i).row && current.col == list.get(i).col) {
                index = i;
                break;
            }
        }
        return index;
    } // end isInList()

    /**
     * Returns the distance between two cells
     * u: the first cell
     * v: the other cell
     * return:  the distance between the cells u and v
     */
    private int distBetween(Cell u, Cell v) {
        int dist;
        int dx = u.col-v.col;
        int dy = u.row-v.row;
        dist = Math.abs(dx)+Math.abs(dy);

        return dist;
    } // end distBetween()

    /**
     * Calculates the path from the target to the initial position
     * of the robot, counts the corresponding nodes.
     */
    private void plotRoute(){
        searching = false;
        endOfSearch = true;
        int index = isInList(closedSet,targetPos);
        Cell cur = closedSet.get(index);
        grid[cur.row][cur.col]= TARGET;
        do {
            cur = cur.prev;
            grid[cur.row][cur.col] = ROUTE;
        } while (!(cur.row == robotStart.row && cur.col == robotStart.col));
        grid[robotStart.row][robotStart.col]=ROBOT;
        String msg;
        msg = "Nodes expanded: " +  expanded ;
        message.setText(msg);
    } // end plotRoute()

    /**
     * Appends to the list containing the nodes of the graph only
     * the cells belonging to the same connected component with node v.
     * This is a Depth First Search of the graph starting from node v.
     * v:    the starting node
     */
    private void findConnectedComponent(Cell v) {
        Stack<Cell> stack = new Stack<>();
        ArrayList<Cell> succesors;
        stack.push(v);
        graph.add(v);
        while(!stack.isEmpty()){
            v = stack.pop();
            succesors = createSuccessors(v, true);
            for (Cell c: succesors) {
                if (isInList(graph, c) == -1){
                    stack.push(c);
                    graph.add(c);
                }
            }
        }
    }

    /**
     * Initialization of Dijkstra's algorithm
     * Only when we run out of queue and the target has not been found,
     * answer that there is no solution .
     * As is known, the algorithm models the problem as a connected graph.
     * It is obvious that no solution exists only when the graph is not
     * connected and the target is in a different connected component
     * of this initial position of the robot.
     */
    private void initializeDijkstra() {
        // First create the connected component
        // to which the initial position of the robot belongs.
        graph.removeAll(graph);
        findConnectedComponent(robotStart);
        // Here is the initialization of Dijkstra's algorithm
        // for each vertex v in Graph;
        for (Cell v: graph) {
            // dist[v] = infinity ;
            v.dist = INFINITY;
        }
        // dist[start] = 0;
        graph.get(isInList(graph,robotStart)).dist = 0;

        //Initializes the list of closed nodes
        closedSet.removeAll(closedSet);
    } // end initializeDijkstra()

    /**
     * paints the grid
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);  // Fills the background color.
        g.fillRect(10, 10, columns*squareSize + 1, rows*squareSize + 1); // Draw the Rectangle

        // Fill color to the Grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (grid[r][c] == EMPTY) {
                    g.setColor(Color.WHITE);
                } else if (grid[r][c] == ROBOT) {
                    g.setColor(Color.RED);
                } else if (grid[r][c] == TARGET) {
                    g.setColor(new Color(58, 91, 255));
                } else if (grid[r][c] == OBST) {
                    g.setColor(Color.BLACK);
                } else if (grid[r][c] == FRONTIER) {
                    g.setColor(new Color(255, 0, 128));
                } else if (grid[r][c] == CLOSED) {
                    g.setColor(new Color(255, 171, 187));
                } else if (grid[r][c] == ROUTE) {
                    g.setColor(Color.GREEN);
                }
                g.fillRect( 11 + c*squareSize, 11 + r*squareSize,  squareSize - 1, squareSize - 1);
            }
        }
    } // end paintComponent()
}
