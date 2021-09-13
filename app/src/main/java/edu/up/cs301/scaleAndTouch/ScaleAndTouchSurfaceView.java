package edu.up.cs301.scaleAndTouch;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;

/**
 * An implementation of a SurfaceView class that supports scaling and panning
 *
 * @author Steven R. Vegdahl
 * @version 28 July 2016
 *
 * Acts like a SurfaceView that is its own listener. Allows user to scale (and pan) by using two
 * fingers.
 *
 * The following methods must be overridden:
 * - protected float getInitialScaleFactor();
 *   The initial scaling of the image
 * - protected float getWorldHeight();
 *   The height of the world, in pixels. (The scaling limits the movement
 *   of the image. Not all of the world is allowed to be off
 *   the SurfaceView
 * - protected float getWorldWidth();
 *   The width of the world, in pixels
 *
 * The following methods may be overridden:
 * - protected void doFixedPreDraw(Canvas canvas);
 *   Drawing to be done on the SurfaceView before the scaled/translated
 *   drawing is done. For example, you may want to display a grid that
 *   is not affected by the user's scaling. Default behavior: draw nothing.
 * - protected void doRelativeDraw(Canvas canvas);
 *   The main drawing method. This draws the "world", but is scaled/translated
 *   by the user. Default behavior: draw nothing.
 * - protected void doFixedPostDraw(Canvas canvas);
 *   Drawing to be done after the scaled/translated drawing is done. This
 *   drawing is not effected by the user's scaling. An example might be
 *   text drawn onto the screen. Default behavior: draw nothing.
 * - public boolean onTouch(float x, float y);
 *   Called when the user touches the screen in a manner that is not part
 *   of a scroll. The call occurs when the user's finger is raised from
 *   the screen. The parameters "logical" parameters--in other words
 *   they are adjusted to account for scaling/movement of the image.
 * - public boolean onScroll(float xOrig, float Orig, float xCurrent,
 *     float yCurrent, float distanceX, float distanceY);
 *   Called when a scroll occurs. The parameters "logical" parameters--in
 *   other words, they are adjusted to account for scaling/movement of
 *   the image.
 *
 *   The ScaleAndTouchSurfaceView is initialized so that it is its own listener.
 */
public abstract class ScaleAndTouchSurfaceView
        extends SurfaceView implements View.OnTouchListener {

    // scaling and movement detectors/listeners
    private ScaleListener mScaleListener;
    private MoveListener mMoveListener;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mMoveDetector;

    // the current scaling and translation of the image
    private float loc0Horiz; // where (0,0) in the "world" appears
    private float loc0Vert; // ... on the SurfaceView
    private float scaleFactor; // how much the image has been scaled

    // the most recent scale-focus position
    private float mFocusX = 0.0f;
    private float mFocusY = 0.0f;

    // whether scaling or movement has occurred since the last
    // "down" has occurred
    private boolean scaleOrMoveSinceLastDown = false;

    // the three abstract methods
    protected abstract float getInitialScaleFactor();
    protected abstract float getWorldHeight();
    protected abstract float getWorldWidth();

    // the three standard SurfaceView constructors
    public ScaleAndTouchSurfaceView(Context context) {
        super(context);
        init(context);
    }
    public ScaleAndTouchSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ScaleAndTouchSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    // performs constructor tasks
    private void init(Context context) {

        // Setup Gesture Detectors
        mScaleListener = new ScaleListener();
        mMoveListener = new MoveListener();
        this.mScaleDetector = new ScaleGestureDetector(context, mScaleListener);
        this.mMoveDetector = new GestureDetector(context, mMoveListener);

        // initialize scalling/positioning variables
        loc0Horiz = 0;
        loc0Vert = 0;
        scaleFactor = getInitialScaleFactor();

        // set up as my own listener
        this.setOnTouchListener(this);
    }

    // standard "onDraw" method:
    // - does the pre-draw
    // - scales/translates the canvas
    // - does the normal draw
    // - unscales/untranslates the canvas
    // - does the post-draw
    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        doFixedPreDraw(canvas);
        canvas.save();
        canvas.translate(loc0Horiz, loc0Vert);
        canvas.scale(scaleFactor, scaleFactor, 0, 0);
        doRelativeDraw(canvas);
        canvas.restore();
        doFixedPostDraw(canvas);
    }

    // default implementations of the three drawing methods;
    // all do nothing
    protected void doFixedPreDraw(Canvas canvas) {
    }
    protected void doFixedPostDraw(Canvas canvas) {
    }
    protected void doRelativeDraw(Canvas canvas) {
    }

    public boolean onScrollBegin() {
        return false;
    }

    // standard onTouch method:
    // - performs/detects a scaling event
    // - if it did nothing, performs/detects a scrolling event
    // - if it did nothing, performs a touch event.
    @Override
    public final boolean onTouch(View v, MotionEvent ev) {

        // if finger down, register that no scales/moves have occurred
        // since
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            scaleOrMoveSinceLastDown = false;
        }

        // perform the scaling event; if it did something (counter > 0),
        // then do nothing more
        mScaleListener.clearCounter();
        mScaleDetector.onTouchEvent(ev);
        if (mScaleListener.getCounter() > 0) {
            scaleOrMoveSinceLastDown = true;
            return true;
        }

        // perform the moving event; if it did something (counter > 0),
        // then do nothing more
        mMoveListener.clearCounter();
        mMoveDetector.onTouchEvent(ev);
        if (mMoveListener.getCounter() > 0) {
            scaleOrMoveSinceLastDown = true;
            return true;
        }

        // if this is an "up", and no scaling/movement has occurred since the last
        // "down", do an "onTouch"
        if (ev.getAction() == MotionEvent.ACTION_UP && !scaleOrMoveSinceLastDown) {
            float xLoc = ev.getX();
            float yLoc = ev.getY();
            xLoc -= loc0Horiz;
            xLoc /= scaleFactor;
            yLoc -= loc0Vert;
            yLoc /= scaleFactor;
            return onTouch(xLoc, yLoc);
        }

        return true;
    }

    // onTouch callback; default behavior is "do nothing"
    public boolean onTouch(float x, float y) {
        return false;
    }

    // onScroll callback: default behavior is "do nothing"
    public boolean onScroll(float xOrig, float Orig, float xCurrent, float yCurrent,
                            float distanceX, float distanceY) {
        return false;
    }

    // setter/getter helper methods
    private float getMFocusX() {
        return mFocusX;
    }
    private float getMFocusY() {
        return mFocusY;
    }
    private void setMFocusX(float val) {
        mFocusX = val;
    }
    private void setMFocusY(float val) {
        mFocusY = val;
    }

    // a scaling event has occurred
    public final boolean onScale(float dScaleFactor, float focusX, float focusY) {

        // compute the new x and y origin-reference positions and scaling
        float newLoc0Horiz = loc0Horiz + focusX-getMFocusX();
        float newLoc0Vert = loc0Vert + focusY-getMFocusY();
        float newScaleFactor = scaleFactor;
        newScaleFactor *= dScaleFactor;

        // move horizontal and vertical position towards the focus point
        // by the scaling factor
        newLoc0Horiz = newLoc0Horiz*dScaleFactor + focusX*(1-dScaleFactor);
        newLoc0Vert = newLoc0Vert*dScaleFactor + focusY*(1-dScaleFactor);

        // if the top/left of our "world" will be displayed more than 90% of the
        // way down or across, abort the scaling
        if (newLoc0Horiz >= getWidth()*9/10 || newLoc0Vert >= getHeight()*9/10) {
            return false;
        }

        // compute the new x and y reference positions and scaling for the bottom-
        // right corner of our "world"
        float newLocMaxHoriz = getWorldWidth();
        newLocMaxHoriz *= newScaleFactor;
        newLocMaxHoriz += newLoc0Horiz;
        float newLocMaxVert = getWorldHeight();
        newLocMaxVert *= newScaleFactor;
        newLocMaxVert += newLoc0Vert;

        // if the bottom/right corner of the world will be more than 90% of the way
        // towards the top or left, abort the scaling
        if (newLocMaxHoriz < getWidth()/10 || newLocMaxVert < getHeight()/10) {
            return false;
        }

        // update the new x/y reference points and the scaling
        loc0Horiz = newLoc0Horiz;
        loc0Vert = newLoc0Vert;
        scaleFactor *= dScaleFactor;

        // update the "previous focus" position
        setMFocusX(focusX);
        setMFocusY(focusY);

        // force a redraw of the SurfaceView; return
        invalidate();
        return true;
    }

    // helper-class to listen for scaling (two-finger) events
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // event-counter, along with methods for setting/getting/bumping
        private int counter;
        public void clearCounter() {
            counter = 0;
        }
        public int getCounter() {
            return counter;
        }
        protected void bumpCounter() {
            counter++;
        }

        // callback method when scaling begins: set the "previous focus"
        // positions
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setMFocusX(detector.getFocusX());
            setMFocusY(detector.getFocusY());
            return true;
        }

        // callback method when scaling ends: do nothing
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }

        // callback method when a scaling occurs: invoke scaling
        // method, bumping counter is successful
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (ScaleAndTouchSurfaceView.this.onScale(detector.getScaleFactor(),
                    detector.getFocusX(), detector.getFocusY())) {
                bumpCounter();
                return true;
            }
            else {
                return false;
            }
        }
    }

    // helper-class to listen for movement (one-finger) events
    private class MoveListener extends GestureDetector.SimpleOnGestureListener {

        // event-counter, along with methods for setting/getting/bumping
        private int counter;
        public void clearCounter() {
            counter = 0;
        }
        public int getCounter() {
            return counter;
        }
        protected void bumpCounter() {
            counter++;
        }

        // "onDown" callback method: do nothing
        @Override
        public boolean onDown(MotionEvent e) {
            return onScrollBegin();
        }

        // "onScroll" callback method
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // adjust values based on scaling/movement
            float x1 = e1.getX();
            float y1 = e1.getY();
            float x2 = e2.getX();
            float y2 = e2.getY();
            x1 -= loc0Horiz;
            x1 /= scaleFactor;
            y1 -= loc0Vert;
            y1 /= scaleFactor;
            x2 -= loc0Horiz;
            x2 /= scaleFactor;
            y2 -= loc0Vert;
            y2 /= scaleFactor;

            // if scrolling event was successful, bump counter; then return
            if (ScaleAndTouchSurfaceView.this.onScroll(x1, y1, x2, y2, distanceX/scaleFactor, distanceY/scaleFactor)) {
                bumpCounter();
            }
            return false;
        }
    }
}
