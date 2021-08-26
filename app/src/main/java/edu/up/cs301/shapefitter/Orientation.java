package edu.up.cs301.shapefitter;

/**
 * Defines orientations corresponding to reflection and rotation transformations.
 * These orientations are respect to Java drawing coordinates (which would be
 * the reverse of mathematical coordinates in which increasing the y value would
 * move you high on the screen).
 *
 * @author Steven R. Vegdahl
 * @version 4 August 2016
 */
public enum Orientation {
    ROTATE_NONE, // no rotation
    ROTATE_CLOCKWISE, // 90-degree clockwise rotation
    ROTATE_180, // 180-degree rotation
    ROTATE_COUNTERCLOCKWISE, // 90 degree counterclockwise rotation
    ROTATE_NONE_REV, // left-right reflection
    ROTATE_CLOCKWISE_REV, // 90-degree clockwise rotation; then left-right reflection
    ROTATE_180_REV, // 180-degree rotation; then left-right reflection
    ROTATE_COUNTERCLOCKWISE_REV; // 90 degree counterclockwise rotation; then left-right reflection
}
