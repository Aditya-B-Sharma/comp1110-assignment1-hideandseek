package comp1110.ass1.gui;

import comp1110.ass1.Hide;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import static comp1110.ass1.Hide.EMPTY_CHAR;

/**
 * This is a JavaFX application that gives a graphical user interface (GUI) to the
 * simple hide game.
 *
 * The tasks set for assignment one do NOT require you to refer to this class, so...
 *
 *    YOU MAY IGNORE THE CODE HERE ENTIRELY
 *
 * ...while you do assignment one.
 *
 * However, the class serves as a working example of a number of JavaFX concepts
 * that you may need later in the semester, so you may find this code helpful
 * later in the semester.
 *
 * Among other things, the class demonstrates:
 *   - Using inner classes that subclass standard JavaFX classes such as ImageView
 *   - Using JavaFX groups to control properties such as visibility and lifetime of
 *     a collection of objects
 *   - Using opacity/transparency
 *   - Using mouse events to implement a draggable object
 *   - Making dropped objects snap to legal destinations
 *   - Using a clickable button with an associated event
 *   - Using a slider for user-input
 *   - Using keyboard events to implement toggles controlled by the player
 *   - Using bitmap images (public domain, CC0)
 *   - Using an mp3 audio track (public domain, CC0)
 *   - Using IllegalArgumentExceptions to check for and flag errors
 */
public class Game extends Application {

    /* board layout */
    private static final int ROWS = 6;
    private static final int COLS = 6;
    private static final int SQUARE_SIZE = 60;
    private static final int MINI_TILE_SIZE = 25;
    private static final int MINI_TILE_PITCH = 25;
    private static final int LARGE_SQUARE_SIZE = 3 * SQUARE_SIZE;
    private static final int MARGIN_X = 40;
    private static final int BOARD_X = MARGIN_X + LARGE_SQUARE_SIZE;
    private static final int MARGIN_Y = SQUARE_SIZE;
    private static final int BOARD_Y = MARGIN_Y;
    private static final int GAME_WIDTH = (2*BOARD_X) + (COLS * SQUARE_SIZE);
    private static final int GAME_HEIGHT = (2*BOARD_Y) + (ROWS * SQUARE_SIZE);

    /* make the masks semi-transparent */
    private static final float MASK_OPACITY = 0.75f;

    /* color the underlying board */
    private static final Paint SUBBOARD_FILL = Color.HONEYDEW;
    private static final Paint SUBBOARD_STROKE = Color.GREY;

    /* marker for unplaced masks */
    private static final char NOT_PLACED = ' ';

    /* where to find media assets */
    private static final String URI_BASE = "assets/";

    /* Loop in public domain CC 0 http://www.freesound.org/people/oceanictrancer/sounds/211684/ */
    private static final String LOOP_URI = Game.class.getResource(URI_BASE + "211684__oceanictrancer__classic-house-loop-128-bpm.wav").toString();
    private AudioClip loop;

    /* game variables */
    private boolean loopPlaying = false;

    /* node groups */
    private final Group root = new Group();
    private final Group masks = new Group();
    private final Group solution = new Group();
    private final Group board = new Group();
    private final Group controls = new Group();
    private final Group exposed = new Group();
    private final Group objective = new Group();

    /* the difficulty slider */
    private final Slider difficulty = new Slider();

    /* message on completion */
    private final Text completionText = new Text("Well done!");

    /* the underlying hide game */
    Hide hide;

    /* the state of the masks */
    char[] maskstate = new char[4];   //  all off screen to begin with


    /**
     * An inner class that represents a square of the board.  The class extends
     * Java FX's ImageView class (used for displaying bitmap images).
     *
     * Images used for game are all in the public domain, CC 0
     *
     * https://pixabay.com/en/bunny-2025642_1280.png
     * https://pixabay.com/en/cartoon-1296405_1280.png
     * https://pixabay.com/en/cats-30746_1280.png
     * https://pixabay.com/en/cow-30710_1280.png
     * https://pixabay.com/en/dog-163527_1280.jpg
     * https://pixabay.com/en/rat-152162_1280.png
     * https://pixabay.com/en/sheep-183057_1920.png
     * https://pixabay.com/en/squirrel-306218_1280.png
     */
    class Square extends ImageView {
        /**
         * Construct a particular square
         * @param id A character representing the type of square to be created.
         */
        Square(char id) {
            if (!(id == EMPTY_CHAR || (id >= 'A' && id <= 'H'))) {
                throw new IllegalArgumentException("Bad tile id: '" + id +"'");
            }
            setImage(new Image(Game.class.getResource(URI_BASE + id + ".png").toString()));
        }

        /**
         * Construct a particular square at a given position
         * @param id A character representing the type of square to be created.
         * @param pos An integer reflecting the position on the grid (0 .. 35)
         */
        Square(char id, int pos) {
            this(id);

            setFitHeight(SQUARE_SIZE);
            setFitWidth(SQUARE_SIZE);
            if (pos < 0 || pos >= Hide.PLACES) {
                throw new IllegalArgumentException("Bad tile position: " + pos);
            }
            int rowminor = (pos % Hide.SMALL_PLACES) / Hide.SMALL_SIDE;
            int colminor = (pos % Hide.SMALL_PLACES) % Hide.SMALL_SIDE;
            int rowmajor = (pos / Hide.SMALL_PLACES) / 2;
            int colmajor = (pos / Hide.SMALL_PLACES) % 2;

            setLayoutX(BOARD_X + (colminor + (Hide.SMALL_SIDE * colmajor)) * SQUARE_SIZE);
            setLayoutY(BOARD_Y + (rowminor + (Hide.SMALL_SIDE * rowmajor)) * SQUARE_SIZE);
        }


        /**
         * Construct a particular square in an array at the top center of the board
         * @param id A character representing the type of square to be created.
         * @param pos An integer reflecting the position in the array.
         * @param y The y position of the piece.
         */
        Square(char id, int pos, int x, int y) {
            this(id);

            setFitHeight(MINI_TILE_SIZE);
            setFitWidth(MINI_TILE_SIZE);
            if (pos < 0 || pos >= Hide.PLACES) {
                throw new IllegalArgumentException("Bad tile position: " + pos);
            }

            setLayoutX(x);
            setLayoutY(y);
        }
    }

    /**
     * An inner class that represents masks used in the game.
     * Each of these is a visual representaton of an underlying mask.
     */
    class FXMask extends ImageView {
        int mask;

        /**
         * Construct a particular playing mask
         * @param mask The letter representing the mask to be created.
         */
        FXMask(char mask) {
            if (!(mask >= 'W' && mask <= 'Z')) {
                throw new IllegalArgumentException("Bad mask: \"" +mask + "\"");
            }
            setImage(new Image(Game.class.getResource(URI_BASE + mask + ".png").toString()));
            this.mask = mask - 'W';
            setFitHeight(LARGE_SQUARE_SIZE);
            setFitWidth(LARGE_SQUARE_SIZE);
            setOpacity(MASK_OPACITY);
        }

        /**
         * Construct a particular playing mask at a particular place on the
         * board at a given orientation.
         * @param position A character describing the position and orientation of the mask
         */
        FXMask(char mask, char position) {
            this(mask);
            if (position < 'A' || position > 'P') {
                throw new IllegalArgumentException("Bad position string: " + position);
            }
            position -= 'A';
            int o = (position % 4);
            int x = (position / 4) % 2;
            int y = (position / 4) / 2;
            setLayoutX((GAME_WIDTH/2) + ((x -1) * LARGE_SQUARE_SIZE));
            setLayoutY((GAME_HEIGHT/2) + ((y -1) * LARGE_SQUARE_SIZE ));
            setRotate(90 * o);
        }
    }


    /**
     * This class extends FXMask with the capacity for it to be dragged and dropped,
     * and snap-to-grid.
     */
    class DraggableFXMask extends FXMask {
        int homeX, homeY;           // the position in the window where the mask should be when not on the board
        double mouseX, mouseY;      // the last known mouse positions (used when dragging)

        /**
         * Construct a draggable mask
         * @param mask The mask identifier ('W' - 'Z')
         */
        DraggableFXMask(char mask) {
            super(mask);
            maskstate[mask - 'W'] = NOT_PLACED; // start out off board
            homeX = ((mask - 'W') % 2) == 0 ? MARGIN_X/2 : GAME_WIDTH - (LARGE_SQUARE_SIZE + MARGIN_X/2);
            setLayoutX(homeX);
            homeY = MARGIN_X + (2 * SQUARE_SIZE * (2 * ((mask - 'W') / 2)));
            setLayoutY(homeY);

            /* event handlers */
            setOnScroll(event -> {            // scroll to change orientation
                hideCompletion();
                rotate();
                event.consume();
            });
            setOnMousePressed(event -> {      // mouse press indicates begin of drag
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                if (!canRotate())             // apply rotation constraint
                    setRotate(hide.getObjective().getMaskWConstraint() * 90);
            });
            setOnMouseDragged(event -> {      // mouse is being dragged
                hideCompletion();
                toFront();
                double movementX = event.getSceneX() - mouseX;
                double movementY = event.getSceneY() - mouseY;
                setLayoutX(getLayoutX() + movementX);
                setLayoutY(getLayoutY() + movementY);
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                event.consume();
            });
            setOnMouseReleased(event -> {     // drag is complete
                snapToGrid();
            });
        }


        /**
         * Snap the mask to the nearest grid position (if it is over the grid)
         */
        private void snapToGrid() {
            if (onBoard()) {
                setLayoutX((GAME_WIDTH/2) + (((getLayoutX() + (1.5*SQUARE_SIZE))> GAME_WIDTH/2 ? 0 : -3) * SQUARE_SIZE));
                setLayoutY((GAME_HEIGHT/2) + ((getLayoutY() + (1.5*SQUARE_SIZE) > GAME_HEIGHT/2 ? 0 : -3) * SQUARE_SIZE ));
                setPosition();
            } else {
                snapToHome();
            }
            makeExposed();
        }

        /**
         * @return true if the mask is on the board
         */
        private boolean onBoard() {
            return getLayoutX() > (BOARD_X-LARGE_SQUARE_SIZE) && (getLayoutX() < (GAME_WIDTH - BOARD_X))
                    && getLayoutY() > (BOARD_Y-LARGE_SQUARE_SIZE) && (getLayoutY() < (GAME_HEIGHT - BOARD_Y));
        }


        /**
         * Snap the mask to its home position (if it is not on the grid)
         */
        private void snapToHome() {
            setLayoutX(homeX);
            setLayoutY(homeY);
            setRotate(0);
            maskstate[mask] = NOT_PLACED;
        }


        /**
         * Rotate the mask by 90 degrees (unless this is mask zero and there is a constraint on mask zero)
         */
        private void rotate() {
            if (canRotate()) {
                setRotate((getRotate() + 90) % 360);
                setPosition();
                makeExposed();
            }
        }

        /**
         * @return true if this mask can be rotated
         */
        private boolean canRotate() { return !(mask == 0 && hide.getObjective().getMaskWConstraint() != -1); }


        /**
         * Determine the grid-position of the origin of the mask (0 .. 15)
         * or -1 if it is off the grid, taking into account its rotation.
         */
        private void setPosition() {
            int x = (int) (getLayoutX() - BOARD_X) / LARGE_SQUARE_SIZE;
            int y = (int) (getLayoutY() - BOARD_Y) / LARGE_SQUARE_SIZE;
            int rotate = (int) getRotate() / 90;
            char val = (char) ('A' + (4 * (x + (2*y)) + rotate));
            maskstate[mask] = val;
        }


        /** @return the mask placement represented as a string */
        public String toString() {
            return ""+maskstate[mask];
        }
    }


    /**
     * Set up event handlers for the main game
     *
     * @param scene  The Scene used by the game.
     */
    private void setUpHandlers(Scene scene) {
        /* create handlers for key press and release events */
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.M) {
                toggleSoundLoop();
                event.consume();
            } else if (event.getCode() == KeyCode.Q) {
                Platform.exit();
                event.consume();
            } else if (event.getCode() == KeyCode.SLASH) {
                solution.setOpacity(1.0);
                event.consume();
            }
        });
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SLASH) {
                solution.setOpacity(0);
                event.consume();
            }
        });
    }


    /**
     * Set up the sound loop (to play when the 'M' key is pressed)
     */
    private void setUpSoundLoop() {
        try {
            loop = new AudioClip(LOOP_URI);
            loop.setCycleCount(AudioClip.INDEFINITE);
        } catch (Exception e) {
            System.err.println(":-( something bad happened ("+LOOP_URI+"): "+e);
        }
    }


    /**
     * Turn the sound loop on or off
     */
    private void toggleSoundLoop() {
        if (loopPlaying)
            loop.stop();
        else
            loop.play();
        loopPlaying = !loopPlaying;
    }


    /**
     * Set up the group that represents the solution (and make it transparent)
     * @param solution The solution string.
     */
    private void makeSolution(String solution) {
        this.solution.getChildren().clear();
        if (solution == null) return;

        if (solution.length() != 4) {
            throw new IllegalArgumentException("Solution incorrect length: " + solution);
        }
        for (int i = 0; i < solution.length(); i++) {
            this.solution.getChildren().add(new FXMask((char) ('W'+i), solution.charAt(i)));
        }
        this.solution.setOpacity(0);
    }


    /**
     * Set up the group that represents the places that make the board
     * @param b The board string
     */
    private void makeBoard(String b) {
        board.getChildren().clear();
        if (b.length() != Hide.PLACES) {
            throw new IllegalArgumentException("Hide board incorrect length: " + b);
        }
        for (int i = 0; i < 4; i++) {
            int side = Hide.SMALL_SIDE * SQUARE_SIZE;
            Rectangle r = new Rectangle(BOARD_X + ((i/2) * side),  BOARD_Y + ((i%2) * side), side, side);
            r.setFill(SUBBOARD_FILL);
            r.setStroke(SUBBOARD_STROKE);
            board.getChildren().add(r);
        }
        for (int i = 0; i < Hide.PLACES; i++) {
            char tile = b.charAt(i);
            if (tile != EMPTY_CHAR)
                board.getChildren().add(new Square(tile, i));
        }
        board.toBack();
    }


    /**
     * Set up each of the four masks
     */
    private void makeMasks() {
        masks.getChildren().clear();
        for (char m = 'W'; m <= 'Z'; m++) {
            masks.getChildren().add(new DraggableFXMask(m));
        }
    }


    /**
     * Make a list of shapes which will displayed centered on the board, at a given
     * y-offset from the top of the board.
     *
     * This is used two ways: one to show the objective (a list of shapes),
     * and the other to show the current state of the game (a list of shapes).
     *
     * @param group The JavaFX group to which this list of shapes will belong.
     * @param list A string describing the list of shapes.
     * @param yoffset The y-offset from the top of the board.
     * @param opacity The opacity of this list of shapes
     */
    private void makeShapeList(Group group, String list, int yoffset, double opacity) {
        group.getChildren().clear();
        for (int i = 0; i < list.length(); i++) {
            char c = list.charAt(i);
            if (c != EMPTY_CHAR)
                group.getChildren().add(new Square(c, i, (GAME_WIDTH/2 - ((list.length() * MINI_TILE_PITCH)/2)) + i*MINI_TILE_PITCH, yoffset));
        }
        group.setOpacity(opacity);
    }


    /**
     * Set up display of game objective
     */
    private void makeObjective() {
        makeShapeList(objective, hide.getObjective().getExposed(), 34, 1.0);
    }


    /**
     * Set up display of exposed tiles
     */
    private void makeExposed() {
        String exp = hide.getExposed(new String(maskstate));
        if (exp == null) return;

        makeShapeList(exposed, exp, 4, 0.5);
        if (exp.equals(hide.getObjective().getExposed()))
            showCompletion();
    }


    /**
     * Put all of the masks back in their home position
     */
    private void resetPieces() {
        masks.toFront();
        for (Node n : masks.getChildren()) {
            ((DraggableFXMask) n).snapToHome();
        }
    }


    /**
     * Create the controls that allow the game to be restarted and the difficulty
     * level set.
     */
    private void makeControls() {
        Button button = new Button("Restart");
        button.setLayoutX(GAME_WIDTH/2 + 70);
        button.setLayoutY(GAME_HEIGHT - 45);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                newGame();
            }
        });
        controls.getChildren().add(button);

        difficulty.setMin(0);
        difficulty.setMax(10);
        difficulty.setValue(0);
        difficulty.setShowTickLabels(true);
        difficulty.setShowTickMarks(true);
        difficulty.setMajorTickUnit(5);
        difficulty.setMinorTickCount(1);
        difficulty.setSnapToTicks(true);

        difficulty.setLayoutX(GAME_WIDTH/2 - 80);
        difficulty.setLayoutY(GAME_HEIGHT - 40);
        controls.getChildren().add(difficulty);

        final Label difficultyCaption = new Label("Difficulty:");
        difficultyCaption.setTextFill(Color.GREY);
        difficultyCaption.setLayoutX(GAME_WIDTH/2 - 150);
        difficultyCaption.setLayoutY(GAME_HEIGHT - 40);
        controls.getChildren().add(difficultyCaption);
    }


    /**
     * Create the message to be displayed when the player completes the puzzle.
     */
    private void makeCompletion() {
        DropShadow ds = new DropShadow();
        ds.setOffsetY(4.0f);
        ds.setColor(Color.color(0.4f, 0.4f, 0.4f));
        completionText.setFill(Color.WHITE);
        completionText.setEffect(ds);
        completionText.setCache(true);
        completionText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD,80));
        completionText.setLayoutX(GAME_WIDTH/2-200);
        completionText.setLayoutY(275);
        completionText.setTextAlignment(TextAlignment.CENTER);
        root.getChildren().add(completionText);
    }


    /**
     * Show the completion message
     */
    private void showCompletion() {
        completionText.toFront();
        completionText.setOpacity(1);
    }


    /**
     * Hide the completion message
     */
    private void hideCompletion() {
        completionText.toBack();
        completionText.setOpacity(0);
    }


    /**
     * Start a new game, resetting everything as necessary
     */
    private void newGame() {
        try {
            hideCompletion();
            hide = new Hide(difficulty.getValue());
            makeMasks();
            makeSolution(hide.getSolution());
            makeObjective();
            makeExposed();
        } catch (IllegalArgumentException e) {
            System.err.println("Uh oh. "+ e);
            e.printStackTrace();
            Platform.exit();
        }
        resetPieces();
    }


    /**
     * The entry point for JavaFX.  This method gets called when JavaFX starts
     * The key setup is all done by this method.
     *
     * @param primaryStage The stage (window) in which the game occurs.
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hide");
        Scene scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        root.getChildren().add(masks);
        root.getChildren().add(board);
        root.getChildren().add(solution);
        root.getChildren().add(controls);
        root.getChildren().add(exposed);
        root.getChildren().add(objective);

        setUpHandlers(scene);
        setUpSoundLoop();
        makeBoard(Hide.BOARD);
        makeControls();
        makeCompletion();

        newGame();

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
