package ShortestPath;

//This class represents cells of the grid
public class Cell {
    int row;   // the row index of the cell(row 0 is the top)
    int col;   // the column index of the cell (Column 0 is the left)
    int dist;  // the distance of the cell from the initial position of the robot
    // this is just for updating the Dijkstra's algorithm
    Cell prev; // Each state corresponds to a cell
    // and each state has a predecessor which
    // is stored in this variable

    public Cell(int row, int col){
        this.row = row;
        this.col = col;
    }
}

