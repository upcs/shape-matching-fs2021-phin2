package edu.up.cs301.shapefitter;

/**
 * Created by vegdahl on 7/19/16.
 */
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import java.util.Random;

/**
 * The "world" surface, for holding and displaying the world into whichh a
 * shape is to be fit.
 *
 * @author Steven R. Vegdahl
 * @version 10 August 2016
 */
public class ShapeFitSurface extends ShapeAbstractSurface {

    // maximum, minimum and initial size of our square array
    private static final int MAX_SQUARES_PER_SIDE = 256;
    private static final int MIN_SQUARES_PER_SIDE = 4;
    private static final int INIT_SQUARES_PER_SIDE = 40;

    // more recent solution proposed by the solver (null if none)
    private boolean[][] proposedSolution; // the boolean array
    private int proposedRowSolutionOffset; // the row position in the world
    private int proposedColSolutionOffset; // the column position in the world
    private Orientation proposedOrientation; // the orientation

    // handler for scheduling things in our thread
    Handler handler = new Handler();

    // paint objects for displaying "true" and "false" squares that overlap with
    // the proposed solution
    private Paint trueOverlapPaint;
    private Paint falseOverlapPaint;

    /**
     * standard SurfaceView constructor
     * @param context the context
     */
    public ShapeFitSurface(Context context) {
        super(context);
        init();
    }

    /**
     * standard SurfaceView constructor
     * @param context the context
     * @param attrs the attributes
     */
    public ShapeFitSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * common code for all constructors--initializes instance variables
     */
    private void init() {
        // generate the initial random values for the squares in the array
        bumpSquaresPerSide(0);

        // create the initial paint objects, with default colors
        trueOverlapPaint = new Paint();
        trueOverlapPaint.setColor(Color.GRAY);
        falseOverlapPaint = new Paint();
        falseOverlapPaint.setColor(Color.LTGRAY);
    }

    /**
     *
     * @return maximum size of our square array
     */
    @Override
    protected int maxNumSquares() {
        return MAX_SQUARES_PER_SIDE;
    }

    /**
     *
     * @return minimum size of our square array
     */
    @Override
    protected int minNumSquares() {
        return MIN_SQUARES_PER_SIDE;
    }

    /**
     *
     * @return initial size of our square array
     */
    @Override
    protected int initNumSquares() {
        return INIT_SQUARES_PER_SIDE;
    }

    /**
     * increase or decrease the size of our square array, randomly
     * resetting the values
     * @param delta amount to change the size
     * @param trueProb probability of a square being initialized to 'true'
     */
    public void bumpSquaresPerSide(int delta, float trueProb) {

        // if changes are not allowed, ignore
        if (!allowChanges) return;

        // change the size of the array
        super.bumpSquaresPerSide(delta);

        // randomly set each value
        Random ran = new Random();
        for (int i = 0; i < boolArray.length; i++) {
            for (int j = 0; j < boolArray[i].length; j++) {
                boolArray[i][j] = ran.nextFloat() < trueProb;
            }
        }
    }

    /**
     * set the display colors
     * @param line color for the lines
     * @param full color for 'true' squares
     * @param empty color for 'false' squares
     * @param fullOverlap color for 'true' squares that overlap the solution
     * @param emptyOverlap color for 'false' squares that overlap the solution
     */
    public void setColors(int line, int full, int empty, int fullOverlap, int emptyOverlap) {
        // perform operation for colors that superclass knows about
        super.setColors(line, full, empty);

        // set the colors for our new paint objects
        trueOverlapPaint.setColor(fullOverlap);
        falseOverlapPaint.setColor(emptyOverlap);
    }

    /**
     * causes a new proposed solution to be displayed
     * @param arr array containing the proposed solution
     * @param row row-position in our world-array corrsponding to top-left of proposed solution
     * @param col column-position in our world-array corrsponding to top-left of proposed solution
     * @param or orientation of the proposed solution
     */
    public void display(boolean[][] arr, int row, int col, Orientation or) {

        // set our instance variables appropriately
        proposedSolution = arr;
        proposedRowSolutionOffset = row;
        proposedColSolutionOffset = col;
        proposedOrientation = or;

        // perform the GUI-invalidate on our object's thread
        handler.post(new Runnable() {
            public void run() {
                invalidate();
            }
        });
    }

    /**
     * remove any previously proposed solution
     */
    public void undisplay() {
        // null out the proposed solution
        proposedSolution = null;

        // do the GUI-invalidate on our object's thread
        handler.post(new Runnable() {
            public void run() {
                invalidate();
            }
        });
    }

    /**
     * get the paint method for a given array position
     * @param row the row position
     * @param col the column position
     * @return
     */
    @Override
    protected Paint paintForPosition(int row, int col) {

        if (proposedSolution != null) {
            // if proposed solution exists, we may need to consider it

            // perform the arithmetic to "map" the square from the proposed solution
            int len = proposedSolution.length; // proposed solution's size
            int solutionRow = row - proposedRowSolutionOffset; // unrotated row position
            int solutionCol = col - proposedColSolutionOffset; // unrotated columun
            int solRow = solutionRow; // we will transform these by rotation ...
            int solCol = solutionCol; //  ... reflection as necessary

            // perform the rotation transformation, if applicable
            switch (proposedOrientation) {
                case ROTATE_CLOCKWISE: case ROTATE_COUNTERCLOCKWISE_REV:
                    // transform coordinates to effect 90-degree rotation clockwise
                    solRow = len - 1 - solutionCol;
                    solCol = solutionRow;
                    break;
                case ROTATE_180: case ROTATE_180_REV:
                    // transform coordinates to effect 180-degree rotation
                    solRow = len - 1 - solutionRow;
                    solCol = len - 1 - solutionCol;
                    break;
                case ROTATE_COUNTERCLOCKWISE: case ROTATE_CLOCKWISE_REV:
                    // transform coordinates to effect 90-degree rotation counter-clockwise
                    solRow = solutionCol;
                    solCol = len - 1 - solutionRow;
                    break;
            }

            // if there is a reflection, transform the column
            switch (proposedOrientation) {
                case ROTATE_NONE_REV:
                case ROTATE_CLOCKWISE_REV:
                case ROTATE_180_REV:
                case ROTATE_COUNTERCLOCKWISE_REV:
                    solCol = len - 1 - solCol;
                    break;
            }

            // if we are in range of the proposed solution, and that position of the
            // proposed solution is 'true', return the "overlap" version
            if (proposedSolution != null && boolArray != null &
                    solRow >= 0 && solCol >= 0 &&
                    solRow < proposedSolution.length &&
                    solCol < proposedSolution[solRow].length &&
                    proposedSolution[solRow][solCol]) {
                // there is a 'true' value in the corresponding solution
                // slot
                return boolArray[row][col] ? trueOverlapPaint : falseOverlapPaint;
            }
        }

        // there was no overlap, so return the normal solution
        return boolArray[row][col] ? truePaint : falsePaint;
    }
}
