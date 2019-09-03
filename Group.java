/*
This is a self-defined class for row/column/group ->(r/c/g) 
storing its candidate and sequence number.
 */
package singli;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Li Sing
 */
public class Group {

    int id; //no.of r/c/g
    Set<Integer> candi = new HashSet<>();//its unfilled number

    Group(int i, HashSet<Integer> set) {//set contains 1-size,initial status
        this.id = i;
        this.candi = set;
    }

}
