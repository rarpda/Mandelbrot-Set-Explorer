package delegate;

import delegate.MandelGuiDelegate.LogStruc;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import model.MandelbrotStruct;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.List;


/**
 * Function used to store and load data into and from program.
 *
 * @author rarpda
 */
public class DataStorage {

    
    final static String COLOR_NAME = "Color";
    final static String MAX_ITERATIONS_NAME = "Max Iterations";
    final static String MAX_IMAG_NAME = "Max Imag";
    final static String MIN_IMAG_NAME = "Min Imag";
    final static String MAX_REAL_NAME = "Max Real";
    final static String MIN_REAL_NAME = "Min Real";
    final static String RADIUS_SQUARE_NAME = "Radius Squared";
    final static int NUMBER_INPUTS_STORED = 7; /* Maximum number of inputs allowed*/
    final static int PARAMETER_ELEMENT_COUNT = 2; /* Attribute name and value size */

    /**
     * Function used to store current parameters as data into file inputed.
     *
     * @param dataStore data to store
     * @param fileStore data to store data to
     * @return true if operation was successful.
     */
    public static boolean storeData(LogStruc dataStore, File fileStore) {
        BufferedWriter logWriter = null; /* Buffered writer for log information */
        FileWriter myFileWriter = null; /* File writer for log information */
        boolean dataStored;
        try {
            /*Check if file exists*/
            if (fileStore.exists()) {
                fileStore.delete();
            }
            /*Create new file*/
            fileStore.createNewFile();
            /*open and write file*/
            myFileWriter = new FileWriter(fileStore.getPath(), true); /* Open file and set it to append */
            logWriter = new BufferedWriter(myFileWriter); /* Initialise buffered writer. */

            String dataLog = "";
            dataLog += COLOR_NAME + "\t" + dataStore.getColorSelected() + "\n";
            dataLog += MAX_ITERATIONS_NAME + "\t" + dataStore.getParams().getMaxIterations() + "\n";
            dataLog += MAX_IMAG_NAME + "\t" + dataStore.getParams().getMaxImag() + "\n";
            dataLog += MIN_IMAG_NAME + "\t" + dataStore.getParams().getMinImag() + "\n";
            dataLog += MAX_REAL_NAME + "\t" + dataStore.getParams().getMaxReal() + "\n";
            dataLog += MIN_REAL_NAME + "\t" + dataStore.getParams().getMinReal() + "\n";
            dataLog += RADIUS_SQUARE_NAME + "\t" + dataStore.getParams().getRadiusSquared() + "\n";

            logWriter.write(dataLog); /* Write data into file once it has been fetched. */
            dataStored = true;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            dataStored = false;
            ;
        } finally {
            try {
                if (logWriter != null) {
                    logWriter.close();
                }
                if (myFileWriter != null) {
                    myFileWriter.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return dataStored;
    }

    /**
     * Function used to store current parameters as data into file inputed.
     *
     * @param imageToWrite data to store
     * @param fileStore    data to store data to
     * @return true if operation was successful.
     */
    public static boolean storeImage(WritableImage imageToWrite, File fileStore) {
        boolean dataSaved;
        try {
            /*Delete file if it exists*/
            if (fileStore.exists()) {
                fileStore.delete();
            }
            /*Create new file*/
            fileStore.createNewFile();
            /*Export image*/
            BufferedImage bImage = SwingFXUtils.fromFXImage(imageToWrite, null);
            ImageIO.write(bImage, "png", fileStore);
            dataSaved = true;
        } catch (Exception e) {
            dataSaved = false;
            System.out.println(e.getMessage());
        }
        return dataSaved;
    }

    /**
     * Function ot load data from text file.
     *
     * @param fileStore The file to load.
     * @return new parameters to execute.
     */
    public static LogStruc loadData(File fileStore) {
        /*Check file exists*/
        LogStruc localLog = null;
        if (fileStore.exists()) {
            try {
                List<String> stringData = Files.readAllLines(fileStore.toPath()); /* Read all the data */
                /*Check format of input*/
                if (stringData.size() == NUMBER_INPUTS_STORED) {
                    localLog = new LogStruc();
                    MandelbrotStruct localStruct = localLog.getParams();
                    /*Process parameter*/
                    for (int lineIndex = 0; lineIndex < NUMBER_INPUTS_STORED; lineIndex++) {
                        /* Separate parameter*/
                        String[] parameterArray = stringData.get(lineIndex).split("\t");

                        /* Check parameter type and value pair exist. */
                        if (parameterArray.length == PARAMETER_ELEMENT_COUNT) {
                            String parameter = parameterArray[0];
                            String parameterValue = parameterArray[1];
                            /*Check if data is correct.*/
                            if (parameter.contains(COLOR_NAME)) {
                                if (parameterValue.matches(Color.WHITE.toString())) {
                                    localLog.setColorSelected(Color.WHITE);
                                } else {
                                    localLog.setColorSelected(Color.valueOf(parameterValue));
                                }
                            } else if (parameter.contains(MAX_ITERATIONS_NAME)) {
                                localStruct.setMaxIterations(Integer.parseInt(parameterValue));
                            } else if (parameter.contains(MAX_IMAG_NAME)) {
                                localStruct.setMaxImag(Double.parseDouble(parameterValue));
                            } else if (parameter.contains(MIN_IMAG_NAME)) {
                                localStruct.setMinImag(Double.parseDouble(parameterValue));
                            } else if (parameter.contains(MAX_REAL_NAME)) {
                                localStruct.setMaxReal(Double.parseDouble(parameterValue));
                            } else if (parameter.contains(MIN_REAL_NAME)) {
                                localStruct.setMinReal(Double.parseDouble(parameterValue));
                            } else if (parameter.contains(RADIUS_SQUARE_NAME)) {
                                localStruct.setRadiusSquared(Double.parseDouble(parameterValue));
                            } else {
                                /*Clear all*/
                                throw new Exception("Input not valid");
                            }
                        } else {
                            /*Clear struct*/
                            throw new Exception("Input not valid");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                localLog = null;
            }
        }
        return localLog;
    }
}
