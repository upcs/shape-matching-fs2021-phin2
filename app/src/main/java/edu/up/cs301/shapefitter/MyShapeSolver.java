package edu.up.cs301.shapefitter;

/**
 * Solver: finds fit for a shape; completed solution by Vegdahl.
 *
 * @author **** put your name here ****
 * @version **** put completion date here ****
 */
public class MyShapeSolver extends ShapeSolver {

    /**
     * Creates a solver for a particular problem.
     *
     * @param parmShape the shape to fit
     * @param parmWorld the world to fit it into
     * @param acc to send notification messages to
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
    public void solve() {

        // ****dummied up****
        int sR, sC, wR, wC;
        int xMatch = 0;
        int yMatch = 0; //coordinates of the first found match
        int numSquares = 0; //# of squares in shape
        int countSquares = 0;//# current number of squares in shape
        boolean matchFound = false;
    //Bug: in the nested for loops, program is skipping last row and column.

    //counts how many elements in  shape are true, this will let us know when to stop.
    for (sR = 0; sR != shape.length; sR++) {
        for (sC = 0; sC < shape.length; sC++) {
            if (shape[sR][sC]) {
                numSquares++;
            }
        }
    }





    //iterates through the world array
    for (wR = 0; wR < (world.length - shape.length); wR++) {
        for (wC = 0; wC < (world[wR].length - shape[sR].length); wC++) {
            countSquares = 0;
            //at each element in world array iterates through the shape array
            for (sR = 0; sR < shape.length - 1; sR++) {
                for (sC = 0; sC < shape[sR].length - 1; sC++) {
                    if ((shape[sR][sC] && !world[wR + sR][wC + sC])) {
                        continue;
                    }
                    if (!shape[sR][sC] && !world[wR + sR][wC + sC]) {
                        continue;
                    }
                    if (!shape[sR][sC] && world[wR + sR][wC + sC]) {
                        continue;
                    }
                    countSquares++;
                    if (countSquares == numSquares) { //when all of the true squares have been counted
                        display(wR, wC, Orientation.ROTATE_NONE);
                        return;
                    } else {
                        undisplay();
                    }
                }
            }
        }
    }
    }

    /**
     * Checks if the shape is well-formed: has at least one square, and has all squares connected.
     *
     * @return whether the shape is well-formed
     */
    public boolean check() {
        return Math.random() < 0.5;
    }

}
