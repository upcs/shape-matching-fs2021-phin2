package edu.up.cs301.shapefitter;

/**
 * Created by vegdahl on 7/19/16.
 */
        import android.content.Context;
        import android.util.AttributeSet;

/**
 * A class that displays (and allows editing of) the small shape that
 *
 * @author Steven R. Vegdahl
 * @version 10 August 2016
 *
 */
public class ShapeCreateSurface extends ShapeAbstractSurface {

    // minimum maximum and initial sizes
    private final static int MAX_SIZE = 50;
    private final static int MIN_SIZE = 4;
    private final static int INIT_SIZE = 6;

    /**
     * @return initial size of a square
     */
    protected int initNumSquares() {
        return INIT_SIZE;
    }

    /**
     * @return maximum size of a square
     */
    protected int maxNumSquares() {
        return MAX_SIZE;
    }

    /**
     * @return minimum size of a square
     */
    protected int minNumSquares() {
        return MIN_SIZE;
    }

    /**
     * standard SurfaceView constructor
     *
     * @param context the context
     */
    public ShapeCreateSurface(Context context) {
        super(context);
    }

    /**
     * standard SurfaceView construct
     *
     * @param context the context
     * @param attrs   the attibutes
     */
    public ShapeCreateSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
