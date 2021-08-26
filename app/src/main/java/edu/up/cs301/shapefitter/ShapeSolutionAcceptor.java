package edu.up.cs301.shapefitter;

/**
 * Created by vegdahl on 7/29/16.
 *
 * Interface that allows a shape solver to "submit" a solution.
 *
 * @author Steven R. Vegdahl
 * @version 4 August 2016
 */
public interface ShapeSolutionAcceptor {
    /**
     * reports a proposed solution
     * @param row row in world corresponding to top-left in proposed solution
     * @param col column in world corresponding to top-left in proposed solution
     * @param or orientation of proposed solution
     */
    public void display(int row, int col, Orientation or);

    /**
     * reports that there is no solution
     */
    public void undisplay();
}
