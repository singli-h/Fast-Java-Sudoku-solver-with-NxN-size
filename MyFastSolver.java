/*
Student Name: Li Sing
SID: 5480 5981

This class is an implementation of the ISolver interface, providing a fast
algorithm to solve a NxN Suduko puzzle

The following strategies are implemented:
S1: private boolean s1(int c, int num)               at line 240
    private boolean s1_2(int c, int num)             at line 256
    private boolean s1_3(int c, int num)             at line 270
S2: public boolean rolcolgpValid(int count, int num) at line 286
S3: private void s3(int c)                           at line 308
S4: private boolean s4(int c)                        at line 355
S5: private boolean s5(int c)                        at line 400
    private boolean s5_2(int c)                      at line 453

 */
package singli;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import sudoku.ISolver;

/**
 *
 * @author cwting
 */
public class MyFastSolver {

    /**
     * Load a puzzle from a text file.
     *
     * @param filename
     * @return a 2D integer array storing the sudoku clues.
     */
    public static int[][] loadPuzzleFromFile(String filename) {

        int[][] puzzle = null;
        try (Scanner scanner = new Scanner(new File(MyFastSolver.class.getResource(filename).getFile()))) {
            int size = scanner.nextInt();
            puzzle = new int[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    puzzle[i][j] = scanner.nextInt();
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File not found.");
            Logger.getLogger(MyFastSolver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return puzzle;
        }
    }

    /**
     * Print the given and solved puzzle side by side.
     *
     * @param given the given puzzle
     * @param solved the solved puzzle
     */
    public static void printPuzzle(int[][] given, int[][] solved) {

        for (int i = 0; i < given.length; i++) {

            for (int j = 0; j < given[0].length; j++) {
                System.out.printf("%3d", given[i][j]);
            }

            System.out.print("\t\t");

            for (int j = 0; solved != null && j < solved[0].length; j++) {
                System.out.printf("%3d", solved[i][j]);
            }
            System.out.println("");
        }
    }

    private static class BacktrackSolver implements ISolver {

        ArrayList<Cell> p = new ArrayList<>();
        ArrayList<HashSet<Cell>> group = new ArrayList<>();
        ArrayList<HashSet<Cell>> col = new ArrayList<>();
        ArrayList<HashSet<Cell>> row = new ArrayList<>();
        Stack<ArrayList<Cell>> p1 = new Stack<>();
        Stack<ArrayList<HashSet<Cell>>> group1 = new Stack<>();
        Stack<ArrayList<HashSet<Cell>>> col1 = new Stack<>();
        Stack<ArrayList<HashSet<Cell>>> row1 = new Stack<>();
        int size = 0;//initialze puzzle size

        BacktrackSolver() {

        }

        /**
         * Solve the sudoku puzzle by filling in all numbers without conflict.
         *
         * @return a deep copy of the solved puzzle; or null if it cannot be
         * solved.
         */
        @Override
        public int[][] solve() {
            min();
            forward();
            //minimize and backup before solve
            if (solve1()) {
                return getPuzzle();
            } else {
                System.out.println("Can't be solved");
                return getPuzzle();
            }
        }

        private boolean min() {// minimize the puzzle before backtracking
            boolean done = false;
            int reduce = 0;
            while (!done) {//done when no further candidate reduced
                for (int i = 0; i < size * size; i++) {
                    if (p.get(i).v == 0) {//not set then need remove its candidate
                        Integer cand[] = new Integer[p.get(i).candi.size()];
                        cand = p.get(i).candi.toArray(cand);
                        for (Integer cand1 : cand) {//try every candidate in this cell and see if can be removed
                            if (!rolcolgpValid(i, cand1)) {// S2 method
                                candiRemove(i, cand1);
                            } else {
                                if (s1(i, cand1)) {//S1 for group
                                    p.get(i).setv(cand1);
                                    reduce++;
                                    break;
                                } else if (s1_2(i, cand1)) {//S1 for row
                                    p.get(i).setv(cand1);
                                    reduce++;
                                    break;
                                } else if (s1_3(i, cand1)) {//S1 for col
                                    p.get(i).setv(cand1);
                                    reduce++;
                                    break;
                                }
                            }
                        }
                        s5(i);//box/line method
                        s5_2(i);
                        if (p.get(i).candi.size() >= 2) {
                            s4(i);
                        }
                        if (p.get(i).candi.size() == 2) {//S3 naked pair
                            s3(i);
                        }
                        if (p.get(i).candi.size() == 1) {//the only choice so can be set directly
                            p.get(i).setv(p.get(i).candi.iterator().next());
                            reduce++;
                            continue;
                        }
                        if (p.get(i).candi.isEmpty()) {//means this solution is wrong
                            //System.out.println("Error: NO CANDIDATE!");
                            return false;
                        }
                    }
                }
                //System.out.println("reduced: " + reduce);
                if (reduce == 0) {//no further reduced then finish minimizing
                    done = true;
                }
                reduce = 0;
            }
            //System.out.println("total reduec: " + sum);
            return true;
        }

        public void forward() {//backup the current status
            p1.push(p);
            group1.push(group);
            row1.push(row);
            col1.push(col);

        }

        public void backward() {//remove fail try and recover the last stauts
            p1.pop();//remove
            group1.pop();
            row1.pop();
            col1.pop();

            p = p1.peek();//recover
            group = group1.peek();
            row = row1.peek();
            col = col1.peek();
        }

        ;
        private boolean solve1() {
            int temp = 0;
            boolean done = true;
            //check if finish.
            for (int i = 0; i < size * size; i++) {//if group 1 done>>start from group two
                if (p.get(i).v == 0) { //the ith cell value is 0 then not finish solving
                    done = false;
                    temp = i;
                    break;
                }
            }
            if (done) {
                return true;
            }
            Integer cand[] = new Integer[p.get(temp).candi.size()];
            cand = p.get(temp).candi.toArray(cand);
            for (Integer cand1 : cand) {//check every candidate possible
                //backtracking satrt
                int num = cand1; //use stack restore;
                //S1:by number,check row and column duplicate
                if (rolcolgpValid(temp, num)) {// check valid using the methods
                    forward();
                    p.get(temp).v = num;
                    if (!min()) {//can not be minimize then means no solution
                        backward();
                    } else {
                        if (solve1()) {
                            return true;
                        } else {
                            backward(); // replace it backtracking
                        }
                    }
                }
            }
            return false;
        }

        public void candiRemove(int count, int num) {//Remove Candidate :D
            p.get(count).candi.remove(num);
        }
//-----------------------------------------------------------------------------
        //S1 Method

        private boolean s1(int c, int num) {//search all need reduce in group
            // boolean only = true;
            for (Cell in : group.get(p.get(c).group_c.id)) {
                if (in.v == 0) {
                    if (!in.equals(p.get(c))) {
                        if (in.candi.contains(num)) {
                            //other cell contain the candidate so it cant be the only one
                            return false;
                        }
                    }
                }
            }
            return true;

        }

        private boolean s1_2(int c, int num) {//search all need reduce in row
            for (Cell in : row.get(p.get(c).row_c.id)) {
                if (in.v == 0) {
                    if (!in.equals(p.get(c))) {
                        if (in.candi.contains(num)) {
                            return false;
                        }
                    }
                }
            }
            return true;

        }

        private boolean s1_3(int c, int num) {//search all need reduce in col
            for (Cell in : col.get(p.get(c).column_c.id)) {
                if (in.v == 0) {
                    if (!in.equals(p.get(c))) {
                        if (in.candi.contains(num)) {
                            return false;
                        }
                    }
                }
            }
            return true;

        }
//------------------------------------------------------------------------------
        // S2:by group,check unique no within group row col

        public boolean rolcolgpValid(int count, int num) {//num is guess number,count is nth cell
            for (Cell can : col.get(p.get(count).column_c.id)) {
                if (can.v == num) {
                    return false;
                }
            }
            for (Cell can : row.get(p.get(count).row_c.id)) {
                if (can.v == num) {
                    return false;
                }
            }
            for (Cell can : group.get(p.get(count).group_c.id)) {
                if (can.v == num) {
                    return false;
                }
            }
            return true;

        }
//------------------------------------------------------------------------------
        //S3 Method

        private void s3(int c) {//Naked pair
            for (Cell in : group.get(p.get(c).group_c.id)) {
                if (in.v == 0 && in.candi.size() == 2 && !in.equals(p.get(c))) {
                    //only size==2 have chance be naked pair
                    if (p.get(c).candi.containsAll(in.candi)) {
                        for (Integer a : p.get(c).candi) {
                            for (Cell other : group.get(p.get(c).group_c.id)) {
                                if (in.v == 0 && !other.equals(in) && !other.equals(p.get(c))) {
                                    other.candi.remove(a);
                                }
                            }
                        }
                    }
                }
            }
            for (Cell in : row.get(p.get(c).row_c.id)) {
                if (in.v == 0 && in.candi.size() == 2 && !in.equals(p.get(c))) {
                    if (p.get(c).candi.containsAll(in.candi)) {
                        for (Integer a : p.get(c).candi) {
                            for (Cell other : row.get(p.get(c).row_c.id)) {
                                if (in.v == 0 && !other.equals(in) && !other.equals(p.get(c))) {
                                    other.candi.remove(a);
                                }
                            }
                        }
                    }
                }
            }
            for (Cell in : col.get(p.get(c).column_c.id)) {
                if (in.v == 0 && in.candi.size() == 2 && !in.equals(p.get(c))) {
                    if (p.get(c).candi.containsAll(in.candi)) {
                        for (Integer a : p.get(c).candi) {
                            for (Cell other : col.get(p.get(c).column_c.id)) {
                                if (in.v == 0 && !other.equals(in) && !other.equals(p.get(c))) {
                                    other.candi.remove(a);
                                }
                            }
                        }
                    }
                }
            }

        }
//------------------------------------------------------------------------------
        //S4 is garbage , waste my whole day to do 
        //I hate this so much.If S4 is a guy,I will eliminate him.

        private boolean s4(int c) {
            int[] cand = new int[p.get(c).candi.size()];
            int len = 0;
            for (int a : p.get(c).candi) {
                cand[len] = a;
                len++;
            }

            for (int i = 0; i < cand.length - 1; i++) { //check every possible pair
                for (int j = i + 1; j < cand.length; j++) {
                    int chance = 0;
                    int real = 0;
                    Cell temp = null;
                    for (Cell in : group.get(p.get(c).group_c.id)) {
                        if (in != p.get(c)) {
                            if (in.candi.contains(cand[i]) || in.candi.contains(cand[j])) {
                                chance++;
                                if (chance > 1) {
                                    return false;
                                }
                                if (in.candi.contains(cand[i]) && in.candi.contains(cand[j])) {
                                    real++;
                                    temp = in;
                                }
                            }

                        }
                    }
                    if (chance == 1 && real == 1) {//find the only one
                        for (int z = 1; z <= size; z++) {
                            if (z != cand[i] && z != cand[j]) {
                                temp.candi.remove(z);
                                p.get(c).candi.remove(z);
                                return true;
                            }
                        }
                    }

                }
            }
            return false;
        }
//------------------------------------------------------------------------------
        //S5 Method

        private boolean s5(int c) {//cehck for box/row
            for (Integer val : p.get(c).candi) {
                int[] gp = new int[size];
                boolean pass = false;
                for (Cell in : group.get(p.get(c).group_c.id)) {
                    if (in.v == 0 && in.candi.contains(val) && in.row_c.id != p.get(c).row_c.id) {
                        pass = true;
                    }
                }
                if (pass) {
                    continue;
                }
                for (Cell in : row.get(p.get(c).row_c.id)) {
                    if (in.v == 0 && in.candi.contains(val)) {
                        // && in.group_c.id==p.get(c).group_c.id
                        gp[in.group_c.id]++;
                    }
                }

                if (gp[p.get(c).group_c.id] != 2) {
                } else {
                    int no = 0;
                    for (int z = 0; z < size; z++) {
                        if (z != p.get(c).group_c.id) {
                            if (gp[z] > 1) {
                                pass = true;
                                break;
                            } else if (gp[z] == 1) {
                                no++;
                            }
                        }

                    }
                    if (pass) {
                        continue;
                    }

                    if (no > 0) {
                        for (Cell remove : row.get(p.get(c).row_c.id)) {
                            if (remove.group_c.id != p.get(c).group_c.id) {
                                remove.candi.remove(val);

                            }
                        }
                        //System.out.println("S5 remove");
                        return true;
                    }
                }

            }
            return false;
        }

        private boolean s5_2(int c) {//check for box/col
            for (Integer val : p.get(c).candi) {
                int[] gp = new int[size];
                boolean pass = false;
                for (Cell in : group.get(p.get(c).group_c.id)) {
                    if (in.v == 0 && in.candi.contains(val) && in.column_c.id != p.get(c).column_c.id) {
                        pass = true;
                    }
                }
                if (pass) {
                    continue;
                }
                for (Cell in : col.get(p.get(c).column_c.id)) {
                    if (in.v == 0 && in.candi.contains(val)) {
                        // && in.group_c.id==p.get(c).group_c.id
                        gp[in.group_c.id]++;
                    }
                }

                if (gp[p.get(c).group_c.id] != 2) {
                } else {
                    int no = 0;
                    for (int z = 0; z < size; z++) {
                        if (z != p.get(c).group_c.id) {
                            if (gp[z] > 1) {
                                pass = true;
                                break;
                            } else if (gp[z] == 1) {
                                no++;
                            }
                        }

                    }
                    if (pass) {
                        continue;
                    }
                    if (no > 0) {
                        for (Cell remove : col.get(p.get(c).column_c.id)) {
                            if (remove.group_c.id != p.get(c).group_c.id) {
                                remove.candi.remove(val);

                            }
                        }
                        //System.out.println("S5 remove");
                        return true;
                    }
                }

            }
            return false;
        }

        /**
         * Store a deep copy of the given sudoku puzzle.
         *
         * @param puzzle the givens or clues of the sudoku puzzle.
         */
        @Override
        public void setPuzzle(int[][] puzzle) {
            size = puzzle[0].length;//puzzle size
            ArrayList<HashSet<Integer>> candi_in_c = new ArrayList<>();
            ArrayList<HashSet<Integer>> candi_in_r = new ArrayList<>();
            ArrayList<HashSet<Integer>> candi_in_g = new ArrayList<>();
            for (int i = 0; i < size; i++) {//initialze each arraylist
                col.add(new HashSet());
                row.add(new HashSet());
                group.add(new HashSet());
            }

            int c = 0;
            int column_c;
            int row_c;
            int group_c;
            Set<Integer> candi = new HashSet<>();//A Set that contain 1 to size for candidate initialze
            for (int z = 1; z <= size; z++) {
                candi.add(z);
            }
            for (int i = 0; i < size; i++) {//use candi to initialze
                candi_in_c.add(new HashSet(candi));
                candi_in_r.add(new HashSet(candi));
                candi_in_g.add(new HashSet(candi));
            }
            for (int i = 0; i < size; i++) {//read in each cell and initialze them with the information
                for (int j = 0; j < size; j++) {
                    int sqrt = (int) Math.sqrt(size);
                    column_c = c % size;//col no.
                    row_c = c / size;//row no.
                    group_c = column_c / sqrt + sqrt * (row_c / sqrt);//group no.

                    HashSet<Integer> new_can = new HashSet<>(candi);//temporate set contain 1-9
                    Group c_g = new Group(column_c, candi_in_c.get(column_c));//c_g only refrence the candi_in_c
                    Group r_g = new Group(row_c, candi_in_r.get(row_c));
                    Group g_g = new Group(group_c, candi_in_g.get(group_c));
                    //Group pass by reference, so save space
                    Cell temp = new Cell(puzzle[i][j], c, size, c_g, r_g, g_g, new_can);
                    p.add(temp);
                    row.get(row_c).add(temp);
                    col.get(column_c).add(temp);
                    group.get(group_c).add(temp);
                    if (puzzle[i][j] != 0) {
                        //provided number so eliminate other cell candidate
                        for (Cell ctemp : row.get(row_c)) {
                            ctemp.candi.remove(puzzle[i][j]);
                        }
                        for (Cell ctemp : col.get(column_c)) {
                            ctemp.candi.remove(puzzle[i][j]);
                        }
                        for (Cell ctemp : group.get(group_c)) {
                            ctemp.candi.remove(puzzle[i][j]);
                        }
                    }
                    c++;//next one
                }
            }
        }

        /**
         * @return a deep copy of the sudoku puzzle of its current state.
         */
        @Override
        public int[][] getPuzzle() {
            int[][] cp = new int[size][size];
            int c = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    cp[i][j] = p.get(c++).v;
                }
            }
            return cp;

        }

    }

    public static void main(String[] args) {

        ISolver solver = new BacktrackSolver();
        int[][] given = loadPuzzleFromFile("../puzzle/16-level5.txt");
        solver.setPuzzle(given);

        // measure time used
        long start = System.currentTimeMillis();
        int[][] solved = solver.solve();
        long time = System.currentTimeMillis() - start;

        printPuzzle(given, solved);
        System.out.println("Running Time (ms): " + time);

    }

}
