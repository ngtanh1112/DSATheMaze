package ShortestPath;

import java.util.Comparator;
//This class overrides the default compare function with the cells
public class CellComparatorByDist implements Comparator<Cell> {
    @Override
    public int compare(Cell cell1, Cell cell2) {
        return cell1.dist - cell2.dist; //return the distance between the cells
    }
}



