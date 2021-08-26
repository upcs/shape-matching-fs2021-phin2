package edu.up.cs301.shapefitter;

import android.util.Log;

/**
 * Created by vegdahl on 7/29/16.
 *
 * An abstract class that represents a solver that finds a location in a world

 * @author Steven R. Vegdahl
 * @version 4 August 2016
 *
 * where a shape fits.
 */
public abstract class ShapeSolver {

    // the shape
    protected boolean[][] shape;

    // the world
    protected boolean[][] world;

    // the acceptor, for reporting the result
    private ShapeSolutionAcceptor acceptor;

    /**
     * constructor
     * @param parmShape that shape object that denotes object to be fit
     * @param parmWorld the world object, into which the shape should be fit
     * @param acc the acceptor, to which results are reported
     */
    public ShapeSolver(boolean[][] parmShape, boolean[][] parmWorld, ShapeSolutionAcceptor acc) {
        // set the acceptor instance variable
        acceptor = acc;

        // make a copy of the shape instance variable
        shape = new boolean[parmShape.length][];
        for (int i = 0; i < shape.length; i++) {
            shape[i] = new boolean[parmShape[i].length];
            for (int j = 0; j < shape[i].length; j++) {
                shape[i][j] = parmShape[i][j];
            }
        }

        // make a copy of the world instance variable
        world = new boolean[parmWorld.length][];
        for (int i = 0; i < world.length; i++) {
            world[i] = new boolean[parmWorld[i].length];
            for (int j = 0; j < world[i].length; j++) {
                world[i][j] = parmWorld[i][j];
            }
        }
    }

    /**
     * reports a proposed solutin
     * @param row row in world corresponding to top-left in proposed solution
     * @param col column in world corresponding to top-left in proposed solution
     * @param orientation orientation of proposed solution
     */
    public void display(int row, int col, Orientation orientation) {
        acceptor.display(row, col, orientation);
    }

    /**
     * reports that there is no solution
     */
    public void undisplay() {
        acceptor.undisplay();
    }

    /**
     * Solves the problem by finding a fit, if possible. The last call to display tells where
     * the fit is. If there is no fit, no call to display should be made--alternatively, a call to
     * undisplay can be made.
     */
    public abstract void solve();

    /**
     * Checks if the shape is well-formed: has at least one square, and has all squares connected.
     *
     * @return whether the shape is well-formed
     */
    public abstract boolean check();
}
