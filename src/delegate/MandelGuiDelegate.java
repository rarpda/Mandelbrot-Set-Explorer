package delegate;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.sun.org.apache.xml.internal.utils.ObjectStack;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.MandelModel;
import model.MandelbrotStruct;


/**
 * The purpose of this class is to implement the delegate for the model-delegate structure of the Practical 4
 *
 * @author rarpda
 */
public class MandelGuiDelegate extends Application implements PropertyChangeListener {

    private static final int FRAME_HEIGHT = 850; /*Size of the Frame Height*/
    private static final int FRAME_WIDTH = 850; /* Size of the Frame Width*/
    private static final int MAX_STACK_SIZE = 30; /*Maximum size of both stacks */

    /*Top level components*/
    private Stage stage;
    private Scene scene;
    private BorderPane mainPane;
    private TextField iterationField;
    private Canvas canvas;
    private MenuBar menuBar;

    /*Components*/
    private ToolBar toolbar;
    private Button startButton;
    private Button resetButton;
    private Button colorButton;
    private RadioButton zoomMode;
    private RadioButton panMode;
    private Button undoButton;
    private Button redoButton;
    private CheckBox magnificationBox;

    /*Model*/
    private MandelModel model;

    /*Mouse related variables*/
    boolean mouseDragged = false;
    mouseMode currentDragMode = mouseMode.ZOOM;
    double xPressed, xDragged, yPressed, yDragged;
    int[][] mandelbrookData;

    enum mouseMode {
        ZOOM,
        PAN,
    }

    private Color colorSelected = Color.WHITE;
    WritableImage canvasImage;

    /*Redo and undo stacks*/
    private ObjectStack undoStack = new ObjectStack(MAX_STACK_SIZE);
    private ObjectStack redoStack = new ObjectStack(MAX_STACK_SIZE);
    LogStruc currentLog = new LogStruc();
    LogStruc previousLog = null;

    /**
     * Class used to store mandelbrook parameters and color of the drawing.
     * This is used for not only redo and undo but also for storing and loading parameters.
     */
    public static class LogStruc implements Cloneable {
        private MandelbrotStruct params;
        private Color colorSelected;

        public LogStruc() {
            this.params = new MandelbrotStruct();
            this.colorSelected = null;
        }

        public LogStruc(LogStruc another) {
            this.params = new MandelbrotStruct(another.params);
            this.colorSelected = another.colorSelected;
        }

        public MandelbrotStruct getParams() {
            return params;
        }

        public void setParams(MandelbrotStruct params) {
            this.params = params;
        }

        public Color getColorSelected() {
            return colorSelected;
        }

        public void setColorSelected(Color colorSelected) {
            this.colorSelected = colorSelected;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    /**
     * This function initialises all the components in the tool bar and set ups the event handlers for them.
     */
    private void setupToolbar() {
        startButton = new Button("Create");
        startButton.setOnAction(event -> {
                    canvas.setCursor(Cursor.WAIT); /* Set cursor to wait*/
                    model.generateMandelbrot(); /*Generate new data with the current parameters.*/
                    addLog(); /* Log this action */
                }
        );

        iterationField = new TextField(); /*Field to add number of iterations */
        iterationField.setPrefWidth(60); /*Set width*/

        /*When key is released get the input and check if it is a number.*/
        iterationField.setOnKeyReleased(event -> {
            String strInput = iterationField.getText();
            try {
                /*If it is a number update the model*/
                model.getCurrentStruct().setMaxIterations(Integer.parseInt(strInput));
            } catch (Exception exp) {
                System.out.println(strInput + " not number!");
            }
        });

        /*Button to reset the GUI to default. */
        resetButton = new Button("Reset");
        resetButton.setOnAction(event -> {
            resetUI();
        });

        /*Button to change color. It goes WHITE -> RED -> BLUE */
        colorButton = new Button("Color change");
        colorButton.setOnAction(event -> {
            if (colorSelected == Color.WHITE) {
                colorSelected = Color.RED;
            } else if (colorSelected == Color.RED) {
                colorSelected = Color.BLUE;
            } else {
                colorSelected = Color.WHITE;
            }

            addLog(); /*Changes to color must be logged.*/
            drawCanvas(mandelbrookData); /*Redraw with new color*/
        });

        magnificationBox = new CheckBox("Magnification");
        magnificationBox.setOnAction(event -> {
            drawCanvas(mandelbrookData); /*Redraw canvas with the magnification scale*/
        });


        zoomMode = new RadioButton("Zoom Mode");
        zoomMode.setOnAction(event -> {
            currentDragMode = mouseMode.ZOOM; /*Set current mouse mode. If clicked when already selected it should stay selected. */
            if (zoomMode.isSelected()) {
                panMode.setSelected(false);
            } else {
                zoomMode.setSelected(true);
            }
        });

        panMode = new RadioButton("Pan Mode");
        panMode.setOnAction(event -> {
            currentDragMode = mouseMode.PAN;/*Set current mouse mode. If clicked when already selected it should stay selected. */
            if (panMode.isSelected()) {
                zoomMode.setSelected(false);
            } else {
                panMode.setSelected(true);
            }
        });

        undoButton = new Button("Undo");
        undoButton.setOnAction(event -> {
            if (!undoStack.empty()) { /* Check stack is not empty*/
                redoStack.push(currentLog); /*Push current log to the redo stack.*/
                currentLog = (LogStruc) undoStack.pop();
                previousLog = new LogStruc(currentLog); /* Update previous log to be a copy of the current one. */
                updateModelParameters(currentLog);
                if (undoStack.empty()) { /*If stack is empty then disable the undo button*/
                    undoButton.setDisable(true);
                }
                redoButton.setDisable(false); /*Enable redo button if undo is clicked.*/

            }
        });

        redoButton = new Button("Redo");
        redoButton.setOnAction(event -> {
            if (!redoStack.empty()) { /*Check redo stack is not empty*/
                undoStack.push(currentLog);
                previousLog = new LogStruc(currentLog);
                currentLog = (LogStruc) redoStack.pop();
                updateModelParameters(currentLog);
                if (redoStack.empty()) {
                    redoButton.setDisable(true); /* Check if stack is not empty*/
                }
                undoButton.setDisable(false); /*Enable the undo if redo is clicked*/
            }
        });

        /*Set zoom ON by default. */
        zoomMode.setSelected(true);
        panMode.setSelected(false);

        toolbar.getItems().add(resetButton);
        toolbar.getItems().add(startButton);
        toolbar.getItems().add(new Label("Iteration count:"));
        toolbar.getItems().add(iterationField);
        toolbar.getItems().add(colorButton);
        toolbar.getItems().add(zoomMode);
        toolbar.getItems().add(panMode);
        toolbar.getItems().add(undoButton);
        toolbar.getItems().add(redoButton);
        toolbar.getItems().add(magnificationBox);
        mainPane.setCenter(toolbar);
    }


    /**
     * Function used to reset the user interface.
     * Called at startup and when the reset button is pressed.
     */
    private void resetUI() {
        colorSelected = Color.WHITE; /*default color*/
        zoomMode.setSelected(true); /* default to ZOOM*/
        currentDragMode = mouseMode.ZOOM;
        panMode.setSelected(false);
        /*Clear stacks*/
        redoStack = new ObjectStack(MAX_STACK_SIZE);
        undoStack = new ObjectStack(MAX_STACK_SIZE);
        /*Reset Mandlebrook model.*/
        model.resetModel();
        previousLog = null;
        undoButton.setDisable(true);  /*Disable all undo and redo buttons*/
        redoButton.setDisable(true);
        addLog(); /*Add resetting to undo stack.*/
    }


    /**
     * Function used to show error message using the inbuilt Alert class.
     *
     * @param errorMessage The message to display.
     */
    private void showErrorMessage(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Encountered!");
        alert.setContentText(errorMessage);
        alert.show(); /*Create and show alert*/
    }

    private void updateModelParameters(LogStruc logUsed) {
        colorSelected = logUsed.colorSelected; /*Update parameters */
        model.setCurrentStruct(logUsed.getParams());
        model.generateMandelbrot();
    }

    /**
     * Function used to setup the menubar. This contains all the the listeners for these controls.
     */
    private void setupMenu() {
        Menu menuOption = new Menu("File");
        MenuItem load = new MenuItem("Load");
        MenuItem save = new MenuItem("Save");
        MenuItem saveImage = new MenuItem("Save Image");
        /*Add items to the menu*/
        menuOption.getItems().add(load);
        menuOption.getItems().add(save);
        menuOption.getItems().add(saveImage);
        menuBar.getMenus().add(menuOption);

        /*Listener for the load option*/
        load.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser(); /*Create a new filechooser*/
            fileChooser.setTitle("Select file to load");
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text Files", "*.txt")); /*Only text files*/
            File newFile = fileChooser.showOpenDialog(stage); /*Allow user to select a file*/
            if (newFile != null) { /*If not null (i.e. caused by user pressing cancel)*/
                LogStruc loadedLog = DataStorage.loadData(newFile); /*Load data */
                if (loadedLog != null) {
                    /*Only take data if it is valid. */
                    currentLog = loadedLog; /*Copy into current log and update model*/
                    updateModelParameters(currentLog);
                    addLog();
                } else {
                    showErrorMessage("File could not be loaded."); /*file not valid*/
                }
            }
        });

        /*Listener for the save Image*/
        saveImage.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export image");
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image Files", "*.png"));
            File newFile = fileChooser.showSaveDialog(stage);
            if (newFile != null) {
                /*Store image*/
                if (!DataStorage.storeImage(canvas.snapshot(null, null), newFile)) {
                    showErrorMessage("Image could not be saved.");
                }
            }
        });

        /*Listener for the save data option*/
        save.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save data");
            File newFile = fileChooser.showSaveDialog(stage);
            if (newFile != null) {
                /*Store the data into text file*/
                if (!DataStorage.storeData(currentLog, newFile)) {
                    showErrorMessage("File could not be saved.");
                }
            }
        });

        mainPane.setTop(menuBar);
    }


    /**
     * Method to setup the menu and toolbar components
     */
    private void setupComponents() {
        setupMenu();
        setupToolbar();
        mainPane.setBottom(canvas);
        mainPane.setVisible(true);
    }


    /**
     * Function used to detect property changes.
     *
     * @param event the data changeds
     */
    public void propertyChange(final PropertyChangeEvent event) {

        if ((event.getSource() == model) && event.getPropertyName().equals(MandelModel.DATA_READY_EVENT)) {
            System.out.println("Event: " + MandelModel.DATA_READY_EVENT);
            Platform.runLater(new Runnable() {
                public void run() {
                    mandelbrookData = (int[][]) event.getNewValue();/*New Data*/
                    drawCanvas(mandelbrookData); /* Draw data*/
                }
            });
        }
    }

    /**
     * Function used to draw the canvas
     *
     * @param dataArray Array containing drawing information.
     */
    private void drawCanvas(int[][] dataArray) {
        PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();
        double brightnessScale = 1.0 / model.getCurrentStruct().getMaxIterations(); /*Set up a brightness scale*/

        /*iterate through array. each element is equivalent to a pixel in the canvas.*/
        for (int height = 0; height < dataArray.length; height++) {
            for (int width = 0; width < dataArray[height].length; width++) {
                int value = dataArray[height][width]; /*Mandlebrook value*/
                Color newColor; /*color to draw.*/
                /*If it didn't escape draw a black pixel*/
                if (value == model.getCurrentStruct().getMaxIterations()) {
                    newColor = Color.BLACK;
                } else {
                    /*If color selected is white do not apply the brightness scaling.*/
                    if (colorSelected != Color.WHITE) {
                        /*Apply scalign to red, green and blue colors and  create new color.*/
                        double red = colorSelected.getRed() * ((double) value * brightnessScale);
                        double green = colorSelected.getGreen() * ((double) value * brightnessScale);
                        double blue = colorSelected.getBlue() * ((double) value * brightnessScale);
                        newColor = Color.color(red, green, blue);
                    } else {
                        newColor = colorSelected;
                    }
                }
                /*Write new color.*/
                writer.setColor(width, (FRAME_WIDTH - 1) - height, newColor);
            }
        }
        /*Write the magnification scale if checkbox is ticked.*/
        if (magnificationBox.isSelected()) {
            canvas.getGraphicsContext2D().setLineWidth(1);
            canvas.getGraphicsContext2D().setStroke(Color.GREEN);
            canvas.getGraphicsContext2D().strokeText("Magnification value" + model.getMagnificationValue() + "x", 20, 20);
        }
        /*Set cursor to default*/
        canvas.setCursor(Cursor.DEFAULT);
        /*Save a snapshot of canvas into a WritableImage to be displayed later if needed. */
        canvasImage = canvas.snapshot(null, null);
        iterationField.setText(Integer.toString(model.getCurrentStruct().getMaxIterations())); /* Always update the iteration field ot the current one. */
    }


    /**
     * Function used to keep track of undo and redo stack.
     * Logs ZOOM,PAN, iteration number and color changes.
     */
    private void addLog() {
        currentLog.setColorSelected(colorSelected);
        currentLog.setParams(model.getCurrentStruct());
        if (previousLog != null) {
            undoStack.push(new LogStruc(previousLog));
        }
        if (!undoStack.empty()) { /*If undo stack is empty clear*/
            undoButton.setDisable(false);
        }
        previousLog = new LogStruc(currentLog); /*Update previous log*/
        redoStack = new ObjectStack(MAX_STACK_SIZE); /*Clear redo stack*/
        redoButton.setDisable(true); /*Disable redo button. */
        System.out.println("Log stack size: " + undoStack.size());
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.stage.setResizable(false);
        this.mainPane = new BorderPane();
        this.scene = new Scene(mainPane, FRAME_WIDTH, FRAME_HEIGHT);  // set up the main frame for this GUI
        toolbar = new ToolBar();
        this.canvas = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        this.model = MandelModel.getInstance((int) this.canvas.getWidth(), (int) this.canvas.getHeight()); /*Pass the X and Y resolution*/
        menuBar = new MenuBar();
        setupComponents();
        setupMouseListener();
        stage.setTitle("CS5001 - Practical 4 Assignment");
        stage.setScene(scene);
        stage.show();
        model.addObserver(this);

        resetUI(); /*Always reset the UI*/
    }

    /**
     * Function used to setup the mouse listeners.
     */
    private void setupMouseListener() {
        canvas.setOnMousePressed(event -> {
            /*Store X and Y coordinates when pressed.*/
            xPressed = event.getX();
            yPressed = event.getY();
        });

        canvas.setOnMouseReleased(event -> {
            /*Only execute any action when the mouse was previously dragged.*/
            if (mouseDragged) {
                mouseDragged = false;
                switch (currentDragMode) {
                    case PAN:
                        /*Calculate distance to move and pan. */
                        canvas.setCursor(Cursor.WAIT);
                        double distanceX = xPressed - event.getX();
                        double distanceY = yPressed - event.getY();
                        model.calculateMapping(distanceX * (-1.0), distanceY); /* X direction must be flipped*/
                        addLog();
                        break;
                    case ZOOM:
                        /*Calculate dimensions of square window and update model. */
                        canvas.setCursor(Cursor.WAIT);
                        double xZoomSquareLength = event.getX() - xPressed;
                        double yZoomSquareLength = event.getY() - yPressed;
                        double squareLength = Math.max(xZoomSquareLength, yZoomSquareLength);/*Get highest dimensions*/
                        model.calculateMapping(xPressed, yPressed, squareLength);
                        addLog();
                        break;
                    default:
                        /*Mode unsupported. Ignore*/
                        break;
                }
            }
        });

        canvas.setOnMouseDragged(event -> {
            xDragged = event.getX();
            yDragged = event.getY();
            switch (currentDragMode) {
                case PAN:
                    /*Pan function*/
                    scene.setCursor(Cursor.CLOSED_HAND);
                    mouseDragged = true;
                    canvas.getGraphicsContext2D().drawImage(canvasImage, 0, 0); /*Always draw image. */
                    /*Only draw if it is from top left to bottom right selection*/
                    canvas.getGraphicsContext2D().strokeLine(xPressed, yPressed, event.getX(), event.getY()); /*draw line*/
                    break;
                case ZOOM:
                    /*Always draw image*/
                    canvas.getGraphicsContext2D().drawImage(canvasImage, 0, 0);
                    /*Only draw if it is from top left to bottom right selection*/
                    if ((xPressed <= xDragged) && (yPressed <= yDragged)) {
                        double xZoomSquareLength = xDragged - xPressed;
                        double yZoomSquareLength = yDragged - yPressed;
                        double zoomSquareLength = Math.max(xZoomSquareLength, yZoomSquareLength); /*Get highest dimensions*/
                        /*Draw square*/
                        canvas.getGraphicsContext2D().setStroke(Color.GREEN);
                        canvas.getGraphicsContext2D().setLineWidth(2);
                        canvas.getGraphicsContext2D().strokeRect(xPressed, yPressed, zoomSquareLength, zoomSquareLength);
                        mouseDragged = true;
                    } else {
                        /*Only allow rectangle to be drawn when cursor goes from top left to bottom right direction*/
                        mouseDragged = false;
                    }
                    break;
                default:
                    /*Ignore mouse Dragged if not in any known mode.*/
                    mouseDragged = false;
                    break;
            }
        });
    }
}

