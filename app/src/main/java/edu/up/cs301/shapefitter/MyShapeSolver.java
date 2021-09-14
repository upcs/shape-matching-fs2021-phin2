package edu.up.cs301.shapefitter;

import android.util.Log;

/**
 * Solver: finds fit for a shape; completed solution by Vegdahl.
 *
 * @author **** Phi Nguyen ****
 * @version **** put completion date here ****
 */
public class MyShapeSolver extends ShapeSolver {
    /**
     * Creates a solver for a particular problem.
     *
     * @param parmShape the shape to fit
     * @param parmWorld the world to fit it into
     * @param acc       to send notification messages to
     */
    public MyShapeSolver(boolean[][] parmShape, boolean[][] parmWorld, ShapeSolutionAcceptor acc) {
        // invoke superclass constructor
        super(parmShape, parmWorld, acc);
    }

    /**
     * Solves the problem by finding a fit, if possible. The last call to display tells where
     * the fit is. If there is no fit, no call to display should be made--alternatively, a call to
     * undisplay can be made.
     */

    // finds a match, the parameter determines what orientation the match will be displayed in
    public void solve() {
        // ****dummied up****
        int sR = 0, sC = 0, wR = 0, wC = 0;
        int numSquares = 0; //# of squares in shape
        int countSquares = 0;//# current number of squares in shape

        //Bug: in the nested for loops, program is skipping last row and column.
        //counts how many elements in  shape are true, this will let us know when to stop
        for (sR = 0; sR < shape.length; sR++) {
            for (sC = 0; sC < shape.length; sC++) {
                if (shape[sR][sC]) {
                    numSquares++;
                }
            }
        }

        //iterates through the world array
        for (Orientation or : Orientation.values()) {
            for (wR = 0; wR < (world.length - shape.length); wR++) {
                for (wC = 0; wC < (world[wR].length - shape[0].length); wC++) {
                    countSquares = 0;
                    //at each element in world array iterates through the shape array
                    for (sR = 0; sR < shape.length; sR++) {
                        for (sC = 0; sC < shape[sR].length; sC++) {
                            if (shape[sR][sC] && world[wR + sR][wC + sC]) {
                                countSquares++;
                            }
                            if (countSquares == numSquares) { //when all of the true squares have been counted
                                display(wR, wC, or);
                                return;
                            } else {
                                undisplay();
                                continue;
                            }
                        }
                    }
                }
            }
            if (or == Orientation.ROTATE_NONE_REV){
            reflect();
            }
            rotate();
            }
        undisplay();
        return;
        }

    protected void rotate() { //rotates the shape 90 degrees clockwise
        boolean[][] array = new boolean[shape.length][shape[0].length]; //temporary array to store rotated shape
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                array[j][shape.length - 1 - i] = shape[i][j]; //rotates the shape
            }
        }

        //copies the values of array into shape;
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                shape[i][j] = array[i][j];
            }
        }
    }

    protected void reflect() { //reflects the shape
        boolean[][] array = new boolean[shape.length][shape[0].length];
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                array[i][j] = shape[i][j];
                shape[i][j] = shape[i][shape.length - 1 - j];
                shape[i][shape.length - 1 - j] = array[i][j];
            }
        }
    }

    /**
     * Checks if the shape is well-formed: has at least one square, and has all squares connected.
     *
     * @return whether the shape is well-formed
     */
    public boolean check() {
        //in the instructions it says the button will stay blue if true is returned
        //however it seems to be the opposite, it will stay blue when false is returned and red if true
        boolean wellFormed = true;
        int emptySquares = 0;
        int adjSquares = 4;
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (i == 0 || i == shape.length - 1 || j == 0 || j == shape[i].length - 1) {
                    adjSquares = 3;
                }
                if((i == 0 && j == 0) || (i == 0 && j == shape[i].length - 1) || (i == shape.length - 1 && j == 0) || (i == shape.length - 1 && j == shape[i].length - 1)) {
                    adjSquares = 2;
                }
                if (shape[i][j]) {
                    if (i != shape.length - 1 && (!shape[i + 1][j])) {
                        emptySquares++;
                    }
                    if (i != 0 && (!shape[i - 1][j])) {
                        emptySquares++;
                    }
                    if (j != 0 && (!shape[i][j - 1])) {
                        emptySquares++;
                    }
                    if (j != shape.length && (!shape[i][j + 1])) {
                        emptySquares++;
                    }
                    if (emptySquares == adjSquares) {
                        wellFormed = false;
                        return wellFormed;
                    }
                }
            }
        }
        return wellFormed;
    }
}


