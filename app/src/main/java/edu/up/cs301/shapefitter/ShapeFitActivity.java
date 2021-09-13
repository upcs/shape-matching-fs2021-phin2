package edu.up.cs301.shapefitter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * main activity for this application
 *
 * @author Steven R. Vegdahl
 * @version 10 August 2016
 */
public class ShapeFitActivity extends Activity
        implements ShapeSolutionAcceptor, SeekBar.OnSeekBarChangeListener {

    // internal file names for storing saved shapes and worlds
    private static final String SAVED_SHAPES_FILENAME = "saved_shapes";
    private static final String SAVED_WORLDS_FILENAME = "saved_worlds";


    // GUI widgets
    private ShapeCreateSurface createSurface; // surface for creating shapes
    private ShapeFitSurface fitSurface; // surface containing the "world" we fit things into
    private SeekBar percentSeekBar; // seek-bar allowing user to control percentage
    private TextView fillPercentLabel; // label telling user the percentage
    private TextView delayTextView; // label that tells user the delay
    private Button solveButton; // "solve" button (also doubles as pause button)

    // saved shapes and worlds, from internal android storage
    private ArrayList<boolean[][]> savedShapes = new ArrayList<boolean[][]>();
    private boolean hadShapeMove = false;
    private ArrayList<boolean[][]> savedWorlds = new ArrayList<boolean[][]>();
    private boolean hadWorldMove = false;

    // the artificial delay for our algorithm (sleeps for this many milliseconds each
    // time 'display' is called.
    private float delay = 6;

    // whether we are paused
    private boolean paused = false;

    // whether the solver is running
    private boolean solverRunning = false;

    // handler for scheduling things in GUI thread
    private Handler handler;

    // IDs of buttons that should disabled when the solver is running
    private static int[] disableButtonIds = {
            R.id.shape_minus_button,
            R.id.shape_plus_button,
            R.id.shape_del_button,
            R.id.shape_down_button,
            R.id.shape_left_button,
            R.id.shape_next_button,
            R.id.shape_prev_button,
            R.id.shape_right_button,
            R.id.shape_save_button,
            R.id.shape_up_button,
            R.id.world_new_button,
            R.id.world_plus_button,
            R.id.world_minus_button,
            R.id.world_del_button,
            R.id.world_next_button,
            R.id.world_prev_button,
            R.id.world_save_button,
            R.id.check_button,
    };

    // buttons that should be disabled when the solver is running
    private static Button[] disableButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // do superclass work
        super.onCreate(savedInstanceState);

        // load the GUI
        setContentView(R.layout.activity_shape_fit);

        // initialize the saved shapes and save worlds
        savedShapes = readSavedArrays(getApplicationContext(), SAVED_SHAPES_FILENAME);
        savedWorlds = readSavedArrays(getApplicationContext(), SAVED_WORLDS_FILENAME);

        // initialize widget variables
        createSurface = (ShapeCreateSurface) findViewById(R.id.shapeCreateSurface);
        fitSurface = (ShapeFitSurface) findViewById(R.id.shapeFitSurface);
        percentSeekBar = (SeekBar) findViewById(R.id.percent_filled_seekbar);
        delayTextView = (TextView) findViewById(R.id.delay_text);
        fillPercentLabel = (TextView) findViewById(R.id.fill_percent_label);
        solveButton = (Button) findViewById(R.id.solve_button);

        // set up listener for seek-bar
        percentSeekBar.setOnSeekBarChangeListener(this);

        // initialize the disable-button array
        disableButtons = new Button[disableButtonIds.length];
        for (int i = 0; i < disableButtonIds.length; i++) {
            disableButtons[i] = (Button) findViewById(disableButtonIds[i]);
        }

        // create our handler
        handler = new Handler();

        // cause "world" array to be recreated
        worldNewClicked(percentSeekBar);

        this.getResources().getColor(R.color.shape_create_empty);

        ///////// get color resources ///////
        // "full" color for shape array
        int createFullColor = this.getResources().getColor(R.color.shape_create_full);
        // "empty" color for shape array
        int createEmptyColor = this.getResources().getColor(R.color.shape_create_empty);
        // "full" color for "world" array
        int fitFullColor = this.getResources().getColor(R.color.shape_fit_full);
        // "empty" color for "world" array
        int fitEmptyColor = this.getResources().getColor(R.color.shape_fit_empty);
        // "full-with-overlap" color for "world" array
        int fitFullColorOverlap = this.getResources().getColor(R.color.shape_fit_full_with_overlap);
        // "empty-with-overlap" color for "world" array
        int fitEmptyColorOverlap = this.getResources().getColor(R.color.shape_fit_empty_with_overlap);
        // line-color for shape array
        int createLineColor = this.getResources().getColor(R.color.shape_create_line_color);
        // line-color for "world" array
        int fitLineColor = this.getResources().getColor(R.color.shape_fit_line_color);

        // set the GUI's delay-text
        setDelayTextValue();

        // set the GUI's percent-text
        updatePercentReport();

        // set the colors for both surface-view widgets; do redraws
        createSurface.setColors(createLineColor, createFullColor, createEmptyColor);
        fitSurface.setColors(fitLineColor, fitFullColor, fitEmptyColor,
                fitFullColorOverlap, fitEmptyColorOverlap);
        fitSurface.invalidate();
        createSurface.invalidate();
    }

    /**
     * standard menu-creation method
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shape_fit, menu);
        return true;
    }

    /**
     * standard menu-item-selected callback
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * callback for "NEW-" button being clicked
     *
     * @param v the view
     */
    public void worldMinusClicked(View v) {
        // regenerate "world" array, bumping size by -1; force redraw
        fitSurface.bumpSquaresPerSide(-1, percentSeekBar.getProgress() / 100.0f);
        fitSurface.invalidate();
    }

    /**
     * callback for "NEW" button being clicked
     *
     * @param v the view
     */
    public void worldNewClicked(View v) {
        // regenerate "world" array, using same size; force redraw
        fitSurface.bumpSquaresPerSide(0, percentSeekBar.getProgress() / 100.0f);
        fitSurface.invalidate();
    }

    /**
     * callback for "NEW+" button being clicked
     *
     * @param v the view
     */
    public void worldPlusClicked(View v) {
        // regenerate "world" array, bumping size by +1; force redraw
        fitSurface.bumpSquaresPerSide(1, percentSeekBar.getProgress() / 100.0f);
        fitSurface.invalidate();
    }

    /**
     * callback for "SIZE+" button being clicked
     *
     * @param v the view
     */
    public void shapePlusClicked(View v) {
        // regenerate "shape" array, bumping size by +1; force redraw
        createSurface.bumpSquaresPerSide(1);
        createSurface.invalidate();
    }

    /**
     * callback for "NEW-" button being clicked
     *
     * @param v the view
     */
    public void shapeMinusClicked(View v) {
        // regenerate "shape" array, bumping size by -1; force redraw
        createSurface.bumpSquaresPerSide(-1);
        createSurface.invalidate();
    }

    /**
     * callback for "DELAY-" button being clicked
     *
     * @param v the view
     */
    public void delayPlusClicked(View v) {
        // increase the delay by +20%, or by 1 (whichever is greater)
        delay = Math.max(delay + 1, delay * 1.2f);

        // update the delay's text field
        setDelayTextValue();
    }

    /**
     * callback for "DELAY-" button being clicked
     *
     * @param v the view
     */
    public void delayMinusClicked(View v) {
        // if delay is already at zero (since we round down), do nothing
        if (delay < 1.0) return;

        // decrease the delay by ~20% or by 1, which ever value is lower
        delay = Math.min(delay - 1, delay / 1.2f);

        // update the delay's text field
        setDelayTextValue();
    }

    /**
     * updates the delay text field in the GUI
     */
    private void setDelayTextValue() {
        delayTextView.setText("" + (int) delay);
    }

    /**
     * callback method when the "NEXT" button is pressed (right edge)
     *
     * @param v the view
     */
    public void worldNextClicked(View v) {
        // go to the next saved world
        nextClicked(savedWorlds, hadWorldMove, fitSurface);

        // marked that the "world" next/prev has moved
        hadWorldMove = true;
    }

    /**
     * callback method when the "PREV" button is pressed (right edge)
     *
     * @param v the view
     */
    public void worldPrevClicked(View v) {
        // go to the previous saved world
        prevClicked(savedWorlds, fitSurface);

        // marked that the "world" next/prev has moved
        hadWorldMove = true;
    }

    /**
     * callback method when the "SAVE" button is pressed (right edge)
     *
     * @param v the view
     */
    public void worldSaveClicked(View v) {
        // add the current world to the array-list of saved ones
        savedWorlds.add(fitSurface.getArray());

        // save the array-list of saved worlds
        saveArrays(getApplicationContext(), savedWorlds, SAVED_WORLDS_FILENAME);
    }

    /**
     * callback method when the "DEL" button is pressed (right edge)
     *
     * @param v the view
     */
    public void worldDelClicked(View v) {
        // remove the current world, if present, from the list of saved worlds
        delClicked(savedWorlds, fitSurface, SAVED_WORLDS_FILENAME);
    }

    /**
     * callback method when the "SAVE" button is pressed (bottom-left)
     *
     * @param v the view
     */
    public void shapeSaveClicked(View v) {
        // add the current shape to the array-list of saved ones
        savedShapes.add(createSurface.getArray());

        // save the array-list of saved shapes
        saveArrays(getApplicationContext(), savedShapes, SAVED_SHAPES_FILENAME);
    }

    /**
     * callback method when the "DEL" button is pressed (bottom-left)
     *
     * @param v the view
     */
    public void shapeDelClicked(View v) {
        // remove the current shape, if present, from the list of saved shapes
        delClicked(savedShapes, createSurface, SAVED_SHAPES_FILENAME);
    }

    /**
     * helper method for deleting a shape or world
     * @param list
     * @param surface
     * @param fileName
     */
    private void delClicked(ArrayList<boolean[][]> list, ShapeAbstractSurface surface, String fileName) {
        // if nothing to delete, just return
        if (list == null || list.size() == 0) return;

        // get the array that we plan to delete (the one at the front of the list
        boolean[][] mine = list.get(0);

        // get the object that the user presently has
        boolean[][] current = surface.getArray();

        // if these objects are identical, we presume that the user really wants to delete
        // it. (If not, we do nothing, as the object the user sees is not the one that would
        // be deleted.)
        if (Arrays.deepEquals(mine, current)) {
            // remove from list
            list.remove(0);
            // set the user's array to be an empty one so that he/she "sees" the delete happen
            surface.setArray(new boolean[current.length][current.length]);
            // update the screen
            surface.invalidate();
            // save the modified list of arrays out to internal storage
            saveArrays(getApplicationContext(), list, fileName);
        }
    }

    /**
     * callback method when the "<" button is pressed
     *
     * @param v the view
     */
    public void shapeLeftClicked(View v) {
        // shift the shape one to the left
        createSurface.shift(0, -1);
        // update the screen
        createSurface.invalidate();
    }

    /**
     * callback method when the "^" button is pressed
     *
     * @param v the view
     */
    public void shapeUpClicked(View v) {
        // shift the shape up by one
        createSurface.shift(-1, 0);
        // update the screen
        createSurface.invalidate();
    }

    /**
     * callback method when the "v" button is pressed
     *
     * @param v the view
     */
    public void shapeDownClicked(View v) {
        // shift the shape down by one
        createSurface.shift(+1, 0);
        // update the screen
        createSurface.invalidate();
    }

    /**
     * callback method when the ">" button is pressed
     *
     * @param v the view
     */
    public void shapeRightClicked(View v) {
        // shift the shape one to the right
        createSurface.shift(0, +1);
        // update the screen
        createSurface.invalidate();
    }

    /**
     * callback method when the "NEXT" button is pressed (bottom-left)
     *
     * @param v the view
     */
    public void shapeNextClicked(View v) {
        // go to the next saved shape
        nextClicked(savedShapes, hadShapeMove, createSurface);

        // mark that we've made a "shape move"
        hadShapeMove = true;
    }

    /**
     * helper method to process a "next" button press; if possible, it "rotates" the objects by
     * extracting the first one and moving it to the end of the array-list; also modifies the
     * surface-view's object
     *
     * @param list the array-list containting our elements
     * @param hadMove whether a rotation has already occurred on this array
     * @param surface the surface-view
     */
    private void nextClicked(ArrayList<boolean[][]> list, boolean hadMove, ShapeAbstractSurface surface) {
        // if there are saved shapes ...
        if (list != null && list.size() > 0) {
            // if we've already made a move, go to the next one by extracting the first
            // component and putting it at the end of the array-list
            if (hadMove) {
                boolean[][] prev = list.get(0);
                list.remove(0);
                list.add(prev);
            }
            // set the surface's array to be the one now at the front
            surface.setArray(list.get(0));

            // ensure GUI is updated
            surface.invalidate();
        }
    }

    /**
     * callback method when the "PREV" button is pressed (bottom-left)
     *
     * @param v the view
     */
    public void shapePrevClicked(View v) {
        // go to the previous saved shape
        prevClicked(savedShapes, createSurface);

        // mark that we've made a "shape move"
        hadShapeMove = true;
    }

    /**
     * helper-method for "PREV" button processing. "Rotates" the array-list by moving the last
     * element into the front position. Also updates the surface-view's array.
     * @param list the array-list containting our elements
     * @param surface the surface-view object
     */
    private void prevClicked(ArrayList<boolean[][]> list, ShapeAbstractSurface surface) {
        if (list != null) { // check that array-list exists
            int last = list.size() - 1;
            if (last >= 0) { // check that array-list is not empty
                // move the last element in the array-list to be the first
                boolean[][] mine = list.get(last);
                list.remove(last);
                list.add(0, mine);

                // change the array that the surfaceView has
                surface.setArray(mine);

                // update the GUI
                surface.invalidate();
            }
        }
    }

    /**
     * helper-method to read an array-list of 2D (square) boolean arrays from internal storage.
     * The format is as follows:
     * - a 4-byte integer denoting the size, N, of one of the sides
     * - N sequences of bytes denoting the values of each row of the array
     *   - each byte denotes the values of 8 elements in the array (LSB corresponding to the
     *     lowest index. If N is not a multiple of 8, the last byte is zero-padded.
     * @param context the Android context object
     * @param filename the name of the file
     * @return the ArrayList containing all of the saved 2D boolean arrays
     */
    private ArrayList<boolean[][]> readSavedArrays(Context context, String filename) {
        // create the array-list
        ArrayList<boolean[][]> rtnVal = new ArrayList<boolean[][]>();

        // input stream for reading ints and bytes
        DataInputStream dis = null;
        try {
            // create input stream
            FileInputStream fis = context.openFileInput(filename);
            dis = new DataInputStream(fis);

            // read as many records as are there--we'll exit the loop with an EOFException
            for (;;) {
                // read the size of the boolean array
                int size = dis.readInt();

                // create a square array of the appropriate size
                boolean[][] arrayReadingIn = new boolean[size][size];

                // read the bytes, using them to initialize the array elements
                for (int i = 0; i < size; i++) {
                    byte val = 0; // the current type
                    for (int j = 0; j < size; j++) {
                        if (j % 8 == 0) {
                            // every 8th time, we read a new byte
                            val = dis.readByte();
                        }
                        // initialize array element
                        arrayReadingIn[i][j] = (val & 0x1) != 0;
                        // shift byte
                        val >>= 1;
                    }
                }
                // add the array to the array-list
                rtnVal.add(arrayReadingIn);
            }
        } catch (EOFException eofx) {
            // on EOF, close the file
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException iox) {
                }
            }
        } catch (IOException iox) {
        }
        // return the array-list
        return rtnVal;
    }

    /**
     * helper-method to save an array-list of boolean arrays out to a file. The format is described
     * in the header-comment for the method readSavedArrays
     * @param context the Android context object
     * @param list the array-list of square 2D boolean arrays
     * @param fileName the name of the file
     */
    private void saveArrays(Context context, ArrayList<boolean[][]> list, String fileName) {
        // variable for writing out ints and bytes to the file
        DataOutputStream dos = null;

        try {
            // open the output stream
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            dos = new DataOutputStream(fos);

            // iterate over each element in the array-list
            for (boolean[][] arr : list) {

                // write the size out at a 32-bit int
                int size = arr.length;
                dos.writeInt(size);

                // iterate over each row in the array
                for (int i = 0; i < size; i++) {
                    byte val = 0; // the current byte value being built up
                    byte mask = 0x0; // mask containing a '1' in the current byte's bit position
                    for (int j = 0; j < size; j++) {
                        mask <<= 1; // shift mask into next position
                        if (mask == 0x0) {
                            // if mask is 0, then we're done with the byte; write it out and
                            // reset the byte-value and mask
                            if (j != 0) {
                                dos.writeByte(val);
                            }
                            val = 0;
                            mask = 0x1;
                        }
                        if (arr[i][j]) {
                            // if the array element is 'true', "or" the mask into the bit-position
                            val |= mask;
                        }
                    }
                    // write out the last byte for the array row
                    dos.writeByte(val);
                }
            }

            // close the file
            dos.close();
        }
        catch (IOException iox) {
        }
    }

    /**
     * callback method when the "SOLVE" button is pressed
     *
     * @param v the view
     */
    public void doSolve(View v) {
        if (solverRunning) {
            // if we're already running, toggle the "pause" state
            paused = !paused;
            // set button text appropriately
            solveButton.setText(paused ? ">" : "||");
        } else {
            // if we're not running, mark as runningn and not paused; then start up a thread
            // that runs the solver
            paused = false;
            solverRunning = true;
            SolverRunner runner = new SolverRunner(createSurface.getArray(), fitSurface.getArray());
            new Thread(runner).start();
        }
    }

    /**
     * callback method when the "SOLVE" button is pressed
     *
     * @param v the view
     */
    public void doCheck(View v) {
        Button myButton = (Button)v;
        ShapeSolver solver = new MyShapeSolver(createSurface.getArray(),
                fitSurface.getArray(), this);
        int colorId;
        if (solver.check()) {
            colorId = R.color.check_button_color;
        }
        else {
            colorId = R.color.solve_button_color;
        }
        int color = getApplicationContext().getResources().getColor(colorId);
        myButton.setBackgroundColor(color);
    }

    /**
     * callback method for seek-bar value changed
     *
     * @param seekBar  the seekbar
     * @param progress the progress
     * @param fromUser whether from the user
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // update the percent text to reflect the new seek-bar position
        updatePercentReport();
    }

    /**
     * callback method for seek-bar starting to track
     *
     * @param seekBar the seek-bar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // ignore
    }

    /**
     * callback method for seek-bar ending the track
     *
     * @param seekBar the seek-bar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // ignore
    }

    /**
     * updates the "percent" text field in the GUI
     */
    private void updatePercentReport() {
        // conmpute the percentage to the nearest integer; then display it
        int percent = (int) ((0.5 + (100 * percentSeekBar.getProgress() / (float) percentSeekBar.getMax())));
        fillPercentLabel.setText(percent + "%");
    }

    /**
     * enable or disable the buttons that are inactive during a "solve"
     *
     * @param val true for "enable", false for "disable"
     */
    private void enableButtons(boolean val) {
        // set the "Solve" button text to the "pause" icon if we're disabling;
        // otherwise to "Solve". (The "Solve" button acts as the "pause"
        // button when it would otherwise be disabled
        solveButton.setText(val ? "Solve" : "||");

        // disable or enable the buttons
        for (Button b : disableButtons) {
            b.setEnabled(val);
        }
    }

    /**
     * Called by the solver when it wants to display a shape onto the "world" screen
     *
     * @param row the row in the world array that corresponds to the top-left of the shape array
     * @param col the column in the world array that corresponds to the top-left of the shape array
     * @param or  the orientation
     */
    @Override
    public void display(int row, int col, Orientation or) {
        // tell the fit-surface the shape and orientation to display
        fitSurface.display(createSurface.getArray(), row, col, or);

        // if the pause button has been pressed, stay in the loop until
        // things are unpaused
        while (paused) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ix) {
            }
        }

        // if we are delaying, do the appropriate delay
        int theDelay = (int) delay;
        if (theDelay > 0) {
            try {
                Thread.sleep(theDelay);
            } catch (InterruptedException ix) {
            }
        }
    }

    /**
     * Called by the solver to report that there is no solution to the given problem.
     */
    @Override
    public void undisplay() {
        // tell the "world" not to display anything
        fitSurface.undisplay();
    }

    /**
     * inner class that runs the solver
     */
    private class SolverRunner implements Runnable {

        // our own copies of the shape and world arrays
        private boolean[][] shapeArray;
        private boolean[][] worldArray;

        /**
         * constructor
         *
         * @param shapeArray the shape array
         * @param worldArray the world array
         */
        public SolverRunner(boolean[][] shapeArray, boolean[][] worldArray) {
            this.shapeArray = shapeArray;
            this.worldArray = worldArray;
        }

        /**
         * run-method
         */
        public void run() {
            // tell the GUI thread to disable appropriate buttons
            handler.post(new Runnable() {
                public void run() {
                    enableButtons(false);
                }
            });

            // disable changes for both of our arrays
            createSurface.setAllowChanges(false);
            fitSurface.setAllowChanges(false);

            // create the solver
            MyShapeSolver solver =
                    new MyShapeSolver(shapeArray, worldArray, ShapeFitActivity.this);

            // run the solver
            solver.solve();

            // mark solver as not running
            solverRunning = false;

            // enable changes for both of our arrays
            createSurface.setAllowChanges(true);
            fitSurface.setAllowChanges(true);

            // tell the GUI thread to enable appropriate buttons
            handler.post(new Runnable() {
                public void run() {
                    enableButtons(true);
                }
            });
        }
    }

}

