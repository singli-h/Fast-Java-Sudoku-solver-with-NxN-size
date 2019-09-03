/*
Student Name: Li Sing
SID: 5480 5981

This is a Class for each cells in Suduko ,total N*N cells.

 */
package singli;

import singli.Group;
import java.util.HashSet;

/**
 *
 * @author Li Sing
 */
public class Cell {
    //Group contain Cell's group/column/row id and group/column/row's candidate.
    Group group_c; 
    Group column_c;
    Group row_c;
    int v;         // cell value,0 if not set.
    HashSet<Integer> candi = new HashSet<>();//cell's candidates.

    public void setv(int i) {//when this cell value is set,its c/r/g remove candidate.
        this.column_c.candi.remove(i);
        this.row_c.candi.remove(i);
        this.group_c.candi.remove(i);
        this.v = i;
    }

    Cell() {//just in case
        this.column_c.id = -1;
        this.row_c.id = -1;
        this.group_c.id = -1;
        this.v = -1;
    }

    //only use this constructor
    Cell(int value, int n, int size, Group c, Group r, Group g, HashSet<Integer> temp) {
        this.column_c = c;
        this.row_c = r;
        this.group_c = g;
        this.v = value;
        this.candi = temp; //shallow copy
        this.candi.remove(value);
    }
}
