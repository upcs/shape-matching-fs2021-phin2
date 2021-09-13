package edu.up.cs301.shapefitter;

/**
 * Created by vegdahl on 7/19/16.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;

import edu.up.cs301.scaleAndTouch.ScaleAndTouchSurfaceView;

/**
 * Displays a square-shaped boolean array, and allows user to modify the values by
 * touching/dragging
 *
 * @author Steven R. Vegdahl
 * @version 10 August 2016
 */

public abstract class ShapeAbstractSurface extends ScaleAndTouchSurfaceView {

    // the array of boolean values
    protected boolean[][] boolArray;

    /**
     * a standard constructor for a SurfaceView
     * @param context the context
     */
    public ShapeAbstractSurface(Context context) {
        super(context);
        init();
    }

    /**
     * a standard constructor for a SurfaceView
     * @param context the context
     * @param attrs the attributes
     */
    public ShapeAbstractSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes the instance variables. Common constructor functionality goes here.
     */
    private void init() {

        // create the array
        int arraySize = Math.max(1, initNumSquares());
        boolArray = new boolean[arraySize][arraySize];

        // create the paint objects, with default colors
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(lineWidth());
        truePaint = new Paint();
        truePaint.setColor(Color.BLACK);
        falsePaint = new Paint();
        falsePaint.setColor(Color.WHITE);
    }

    /**
     * @return width of the "world", in pixels
     */
    @Override
    protected float getWorldWidth() {
        return 1000;
    }

    /**
     * @return height of the "world" in pixels
     */
    @Override
    protected float getWorldHeight() {
        return 1000;
    }

    /**
     * @return initial scaling factor for displaying
     */
    @Override
    protected float getInitialScaleFactor() {
        return 1;
    }

    /**
     * @return the maximum number of squares on a side
     */
    protected abstract int maxNumSquares();

    /**
     *
     * @return the minimum number of squares on a side
     */
    protected abstract int minNumSquares();

    /**
     *
     * @return the starting number of squares on a side
     */
    protected abstract int initNumSquares();

    // paint objects for painting ...
    protected Paint truePaint; // squares with a value of true
    protected Paint falsePaint; // squares with value of false
    protected Paint linePaint; // lines between the squares

    // whether to allow changes to be made on elements in the array,
    // or to replace the array with a new one
    protected boolean allowChanges = true;

    /**
     * tell whether changes are allowed to be made by elements in the array
     * (or to change the array itself)
     * @param b true to allow changes; false to disallow
     */
    public void setAllowChanges(boolean b) {
        allowChanges = b;
    }

    /**
     * set the colors for displaying lines, full (true) and empty (false) squares
     * @param line color for displaying lines
     * @param full color for displaying true values
     * @param empty color for displaying false values
     */
    public void setColors(int line, int full, int empty) {
        linePaint.setColor(line);
        truePaint.setColor(full);
        falsePaint.setColor(empty);
    }

    /**
     * width, in pixels for drawing lines between the squares
     * @return
     */
    protected int lineWidth() {
        return 2;
    }

    /**
     * the paint object for painting the square in the given position
     * @param row the row position
     * @param col the column position
     * @return the paint object
     */
    protected Paint paintForPosition(int row, int col) {
        return boolArray[row][col] ? truePaint : falsePaint;
    }

    /**
     * Standard ScaleAndTouchSurfaceView method for painting the scalable portion of the
     * image.
     * @param c the canvas on which to draw
     */
    @Override
    protected void doRelativeDraw(Canvas c) {
        // compute the mininum of the height and width as the basis for scaling
        float height = c.getHeight();
        float width = c.getWidth();
        float min = Math.min(height, width);

        // length of a side of our square array
        int arraySize = boolArray.length;

        // number of pixels to draw per square
        float pixelsPerSquare = min/(arraySize+2);

        // draw each square, using the appropriate color for each
        for (int row = 0; row < boolArray.length; row++) {
            for (int col = 0; col < boolArray[row].length; col++) {
                Paint p = paintForPosition(row, col);
                c.drawRect((col+1)*pixelsPerSquare,(row+1)*pixelsPerSquare,
                        (col+2)*pixelsPerSquare,(row+2)*pixelsPerSquare,
                        p);
            }
        }

        // draw the grid of lines separating the squares
        for (int i = 0; i <= arraySize; i++) {
            // vertical line
            c.drawLine((i+1)*pixelsPerSquare,pixelsPerSquare,(i+1)*pixelsPerSquare,
                    (arraySize+1)*pixelsPerSquare, linePaint);
            // horizontal line
            c.drawLine(pixelsPerSquare,(i+1)*pixelsPerSquare,
                    (arraySize+1)*pixelsPerSquare,(i+1)*pixelsPerSquare,
                    linePaint);
        }
    }

    /**
     * Replace are boolean square array with one of a different size. By default,
     * values are copied from the old array to the new, to the extent possible.
     * If the array size is increasing, new position received the value false by
     * default.
     * @param delta the amount (positive or negative) to change the size
     */
    public void bumpSquaresPerSide(int delta) {

        // if we're not allowed to make changes, do nothing
        if (!allowChanges) return;

        // compute the new size, ensuring that we stay within the maximum and minimum allowed
        int arraySize =
                Math.max(1,
                        Math.min(maxNumSquares(),
                                Math.max(minNumSquares(),
                                        boolArray.length + delta)));

        // create the new array
        boolean[][] newArray = new boolean[arraySize][arraySize];

        // minimum of the old and new array sizes
        int limit = Math.min(boolArray.length, arraySize);

        // copy elements from oldl to new
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                newArray[i][j] = boolArray[i][j];
            }
        }

        // replace old the array
        boolArray = newArray;
    }

    /**
     * convert an x-y coordinate position to a row-column position, based on where the array
     * is displayed
     * @param x the x pixel-coordinate
     * @param y the y pixel-coordinate
     * @return a Point object: first (x) element gives the row; second (y) element
     * gives the column
     */
    public Point mapPixelToCoord(float x, float y) {
        // compute minimum of the height and width of the SurfaceView
        float height = this.getHeight();
        float width = this.getWidth();
        float min = Math.min(height, width);

        // compute the number of pixels per square, accounting for the fact that a 1-square
        // border is drawn on each side
        float pixelsPerSquare = min/(boolArray.length+2);

        // map the row and colum from the y and x positions
        int rowMapped = (int)(y/pixelsPerSquare);
        int colMapped = (int)(x/pixelsPerSquare);
        rowMapped--; // accounts for 1-square border
        colMapped--; // accounts for 1-square border

        // returnn the row and column
        return new Point(rowMapped, colMapped);
    }

    /**
     * toggle the given position in the array, after doing a bounds-check
     * @param row the row position of the array
     * @param col the column position of the array
     */
    public void togglePosition(int row, int col) {
        // if no changes are allowed, do nothing
        if (!allowChanges) return;

        // if out of bounds, do nothing
        if (row < 0 || row >= boolArray.length ||
                col < 0 || col >= boolArray.length) return;

        // "flip" the value in the array
        boolArray[row][col] = !boolArray[row][col];
    }


    /**
     * sets the value of the given position in the array, after doing a bounds-check
     * @param row the row position of the array
     * @param col the column position of the array
     * @param val the value to set the element to
     */
    public void setPosition(int row, int col, boolean val) {
        // if no changes allowed, do nothing
        if (!allowChanges) return;

        // if out of bounds, do nothing
        if (row < 0 || row >= boolArray.length ||
                col < 0 || col >= boolArray.length) return;

        // set the value in the array
        boolArray[row][col] = val;
    }

    /**
     * standard onTouch method for ScaleAndTouchSurfaceView objects; gives "logical"
     * pixel coordinates, after any scaling and translation that has been done
     * @param x x pixel position touched
     * @param y y pixel position touched
     * @return whether the event was handled
     */
    @Override
    public boolean onTouch(float x, float y) {

        // map pixel position to coordinate position in the array
        Point coordPoint = mapPixelToCoord(x, y);
        int row = coordPoint.x;
        int col = coordPoint.y;

        // if out of bounds, ignore
        if (row < 0 || col < 0 || row >= boolArray[0].length || col >= boolArray.length) {
            return false;
        }

        // if in bounds, toggle the value, repaint, and return that we have handled the event
        togglePosition(row,col);
        invalidate();
        return true;
    }

    /**
     * standard onScroll callback method; pixel coordinates account for scaling
     * and translation
     * @param xOrig x coordinate at the beginning of the scroll
     * @param yOrig y coordinate at the beginning of the scroll
     * @param xCurrent x coordinate of this scroll event
     * @param yCurrent y coordinate of this scroll event
     * @param distanceX distance of scroll since last (?) event
     * @param distanceY distance of scroll since last (?) event
     * @return whether the event was handled
     */
    @Override
    public boolean onScroll(float xOrig, float yOrig, float xCurrent,
                            float yCurrent, float distanceX, float distanceY) {

        // map the original and current pixels to array coordinates
        Point coordPointOrig = mapPixelToCoord(xOrig, yOrig);
        Point coordPoint = mapPixelToCoord(xCurrent, yCurrent);
        int rowOrig = coordPointOrig.x;
        int colOrig = coordPointOrig.y;
        int row = coordPoint.x;
        int col = coordPoint.y;

        // if either coordinate is out of bounds, ignore
        if (rowOrig < 0 || colOrig < 0 ||
                rowOrig >= boolArray[0].length || colOrig >= boolArray.length) {
            return false;
        }
        if (row < 0 || col < 0 || row >= boolArray[0].length || col >= boolArray.length) {
            return false;
        }

        // copy the value from the original square into the current one
        boolean newValue = boolArray[rowOrig][colOrig];
        setPosition(row, col, newValue);

        // redraw the window
        invalidate();

        // return that we have handled the event
        return true;
    }

    /**
     * @return a copy of the object's boolean array
     */
    public boolean[][] getArray() {
        boolean[][] rtnVal;

        rtnVal = new boolean[boolArray.length][];
        for (int i = 0; i < rtnVal.length; i++) {
            rtnVal[i] = new boolean[boolArray[i].length];
            for (int j = 0; j < rtnVal[i].length; j++) {
                rtnVal[i][j] = boolArray[i][j];
            }
        }
        return rtnVal;
    }

    /**
     * sets a new boolean array for the surface (makes a copy)
     * @param array the new array
     */
    public void setArray(boolean[][] array) {
        boolean[][] newOne;

        newOne = new boolean[array.length][];
        for (int i = 0; i < newOne.length; i++) {
            newOne[i] = new boolean[array[i].length];
            for (int j = 0; j < newOne[i].length; j++) {
                newOne[i][j] = array[i][j];
            }
        }
        boolArray = newOne;
    }

    /**
     * shift elements within the array; vacated locations receive 'false'
     * @param rowDelta the number of rows to shift the elements (positive => down, negative => up)
     * @param colDelta the numbrer of columns to shift the elements (positive => right, negative
     *                 => left)
     */
    public void shift(int rowDelta, int colDelta) {
        // based on sign of rowDelta, determine which direction our row-loop should go
        int rowLimit;
        int rowStart;
        int rowBump;
        if (rowDelta <= 0) {
            rowStart = 0;
            rowLimit = boolArray.length;
            rowBump = 1;
        }
        else {
            rowStart = boolArray.length-1;
            rowLimit = -1;
            rowBump = -1;
        }
        // based on sign of colDelta, determine which direction our column-loop should go
        int colLimit;
        int colStart;
        int colBump;
        if (colDelta <= 0) {
            colStart = 0;
            colLimit = boolArray.length;
            colBump = 1;
        }
        else {
            colStart = boolArray.length-1;
            colLimit = -1;
            colBump = -1;
        }

        // copy each element to it's new location
        for (int row = rowStart; row != rowLimit; row += rowBump) {
            for (int col = colStart; col != colLimit; col += colBump) {
                int sourceRow = row - rowDelta;
                int sourceCol = col - colDelta;
                if (sourceRow < 0 || sourceRow >= boolArray.length ||
                        sourceCol < 0 || sourceCol >= boolArray[sourceRow].length) {
                    // source is out-of-bounds: fill in with 'false'
                    boolArray[row][col] = false;
                }
                else {
                    // source in bouncs: copy the element
                    boolArray[row][col] = boolArray[sourceRow][sourceCol];
                }
            }
        }
    }
}
