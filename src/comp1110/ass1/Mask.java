package comp1110.ass1;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;

/**
 * An enumeration representing the four masks in the game hide.
 *
 * In the provided version of this class, each of the masks do not have any
 * associated state.  You may wish to add that.  You will then need to use
 * constructors to initialized that state.
 *
 * You may want to look at the 'Planet' example in the Oracle enum tutorial for
 * an example of how to associate state (radius, density in that case) with each
 * item in the enumeration.
 *
 * http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
 */
public enum Mask {
    W(2, 7),
    X(1, 4),
    Y(0, 8),
    Z(1, 7);          // These do not use any state (or constructors).  You may want to add them.

    private int first;
    private int second;


    Mask(int first, int second) {
        this.first = first;
        this.second = second;
    }

    int[] transposer(int[] list) {
        int[] out = new int[list.length];
        out[0] = (list[6]);
        out[1] = (list[3]);
        out[2] = (list[0]);
        out[3] = (list[7]);
        out[4] = (list[4]);
        out[5] = (list[1]);
        out[6] = (list[8]);
        out[7] = (list[5]);
        out[8] = (list[2]);
        return out;
    }
    int[] quadrant(int[] list, int quadNumber) {
        int x = 9 * (quadNumber);
        int[] finalQuadrant = list;
        for (int j = 0; j < finalQuadrant.length; j++) {
            finalQuadrant[j] = finalQuadrant[j] + x;
        }
        return finalQuadrant;
    }
    int[] spotDeleter(int[] input, int a, int b ) {

        ArrayList<Integer> actualOut = new ArrayList<Integer>();
        for (int i = 0; i < input.length; i++) { //i is index counter
            if (input[i] == a || input[i] == b) {
                continue;
            } else {
                actualOut.add(i);
            }
        }
        int[] output = new int[actualOut.size()];
        for(int j = 0; j < actualOut.size(); j++) {
                output[j] = actualOut.get(j);
        }
        return output;
    }

    /**
     * Return indicies corresponding to which board squares would be covered
     * by this mask given the provided placement.
     *
     * A placement encodes which of the four quadrants a mask is located in,
     * and rotations of the masks.  Masks may only be rotated, not be flipped
     * (the game does not allow them to be turned over).
     *
     * Masks W and X have four interesting rotations.   Masks Y and Z only have two
     * interesting rotations, the other two are isomorphic:  Mask Y and Z in rotation
     * A are indistinguishable from masks Y and Z in rotation C. Likewise Mask Y and Z
     * in rotation B are indistinguishable from masks Y and Z in rotation D.
     *
     * The placement character describes the place as follows:
     *    - letters 'A' to 'D' describe the first quadrant of the board,
     *      corresponding to board positions 0-8.
     *    - letters 'E' to 'H' describe the second quadrant of the board,
     *      corresponding to board positions 9-17.
     *    - letters 'I' to 'L' describe the third quadrant of the board,
     *      corresponding to board positions 18-26.
     *    - letters 'M' to 'P' describe the fourth quadrant of the board,
     *      corresponding to board positions 27-35.
     *    - letters 'A', 'E', 'I', and 'M' describe the mask upright
     *    - letters 'B', 'F', 'J', and 'N' describe the mask turned 90 degrees clockwise
     *    - letters 'C', 'G', 'K', and 'O' describe the mask turned 180 degrees
     *    - letters 'D', 'H', 'L', and 'P' describe the mask turned 270 degrees clockwise
     *
     * Examples:
     *
     *   Given the placement character 'A', the mask 'W' would return the indices: {0,1,3,4,5,6,8}.
     *   Given the placement character 'O', the mask 'X' would return the indices: {27,28,29,30,32,33,35}.
     *
     *
     * Hint: You can associate values with each entry in the enum using a constructor,
     * so you could use that to somehow encode the properties of each of the four masks.
     * Then in this method you could use the value to calculate the required indicies.
     *
     * See the 'Grade' enum given in the O2 lecture as part of the lecture code (live coding),
     * for an example of an enum with associated state and constructors.
     *
     * The tutorial here: http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
     * has an example of a Planet enum, which includes two doubles in each planet's
     * constructor representing the mass and radius.   Those values are used in the
     * surfaceGravity() method, for example.
     *
     * @param placement A character describing the placement of this mask, as per the above encoding
     * @return A set of indices corresponding to the board positions that would be covered by this mask
     */

    int[] getIndices(char placement) {
        int[] indices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        int transposeAmount = 0;
        int quadrantType = 0;
        if (placement >= 'A' && placement <= 'D') {
            quadrantType = 0;
        } else if (placement >= 'E' && placement <= 'H'){
            quadrantType = 1;
        } else if (placement >= 'I' && placement <= 'L'){
            quadrantType = 2;
        } else if (placement >= 'M' && placement <= 'P'){
            quadrantType = 3;
        }
        switch (placement) {
            case 'B':
            case 'F':
            case 'J':
            case 'N': {
                indices = transposer(indices);
                break;
            }
            case 'C':
            case 'G':
            case 'K':
            case 'O': {
                indices = transposer(indices);
                indices = transposer(indices);
                break;
            }
            case 'D':
            case 'H':
            case 'L':
            case 'P': {
                indices = transposer(indices);
                indices = transposer(indices);
                indices = transposer(indices);
                break;
            }
        }
        indices = spotDeleter(indices, first, second);
        indices = quadrant(indices, quadrantType);
        return indices;
        // FIXME Task 4: implement code that correctly creates an array of integers specifying the indicies of masked pieces
    }

    /**
     * Mask an input string with a given string of mask positions.   The
     * four characters composing the mask position string describe the
     * positions and rotations of each of four masks
     *
     * The first character in the string describes the position and rotation
     * of the 'W' mask.  The second, third and fourth describe the positions
     * of the 'X', 'Y', and 'Z' positions respectively.
     *
     * If the character is a space ' ', then that means that the given mask
     * is not used.   Otherwise the encoding described above in getIndices() is used.
     *
     * Hint: The values() method of any enum type will return an array of the values in the enum.
     *
     * Hint: You cannot change strings, but you can convert from strings to
     * char arrays (.toCharArray()), and you can create new strings from
     * char arrays.
     *
     * @param maskPositions A string describing the positions of each of the masks, as per above
     * @param input An input string of 36 characters
     * @return The result of masking the input with the given mask, with masked characters replaced
     * by Hide.EMPTY_CHAR ('.').
     */
    public static String maskString(String maskPositions, String input) {
        // FIXME Task 5: implement code that correctly creates a masked string according to the comment above
        return null;
    }
}
