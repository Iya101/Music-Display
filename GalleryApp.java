package cs1302.gallery;

import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.MenuBar;
import javafx.animation.KeyFrame;
import javafx.event.ActionEvent;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.TilePane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.net.URL;
import java.net.URLEncoder;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import java.lang.RuntimeException;
import java.io.InputStreamReader;
import javafx.scene.control.Label;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;

/**
 * Represents an iTunes GalleryApp.
 */

public class GalleryApp extends Application {

    TilePane grid = new TilePane();
    ImageView [] imgHolder  = new ImageView[20];
    String [] urlResults;
    String encodedString = "";
    URL url = null;
    JsonArray jsonResults;
    JsonArray usedResults;
    JsonArray unusedResults;
    ProgressBar progressBar = new ProgressBar();
    double progress = 0.0;
    String userInput = "";
    Button pause;
    Text searchQuery;
    Timeline timeline;
    TextField searchText;
    Button updateImage;
    boolean play = true;
    double playCount = -1.0;
    boolean isRunning = false;
    Stage stage;

    /**
     *This method starts the GUI app.
     * @param stage of type Stage.
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        VBox pane = new VBox();
        pane.getChildren().addAll(menuMaker(), toolBarMaker());
        getMusicAlbums("Rick Astley"); //lol get Ricked!
        pane.getChildren().add(imgDisplay());
        pane.getChildren().add(progressBarMethod());
        Scene scene = new Scene(pane, 500, 485);
        stage.setTitle("Gallery!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * This method creates a MenuBar object, while also adding the functionality of the exit
     * MenuItem.
     * @return a MenuBar
     */

    public MenuBar menuMaker() {

        Menu file = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
        file.getItems().add(exit);
        Menu help = new Menu("Help");

        MenuItem about = new MenuItem("About");

        // start

        about.setOnAction(e -> {
            Stage presentMe = new Stage();
            StackPane root = new StackPane();
            presentMe.setTitle("About Me");
            Text aboutText = new Text("About Iyanna Yapo :]");
            ImageView myPic = new ImageView();
            Image me = new Image("shorturl.at/dxBH4", 100, 100, false, false);
            myPic.setImage(me);
            VBox aboutMeBox = new VBox(10);
            Text infoText = new Text("About Iyanna Yapo :]");
            infoText.setText( " Iyanna Yapo \n ihy66481@uga.edu \n Application V 2021");
            infoText.setTextAlignment(TextAlignment.CENTER);
            aboutText.setTextAlignment(TextAlignment.CENTER);
            aboutMeBox.getChildren().addAll(myPic, infoText);
            root.getChildren().addAll(aboutText, aboutMeBox);
            StackPane.setAlignment(aboutText, Pos.TOP_CENTER);
            aboutMeBox.setAlignment(Pos.CENTER);

            Scene aboutMe = new Scene(root, 400, 400);

            presentMe.setScene(aboutMe);
            presentMe.initOwner(stage);
            presentMe.initModality(Modality.APPLICATION_MODAL);
            presentMe.showAndWait();
        });

        help.getItems().add(about);


        MenuBar menuBar = new MenuBar();

        menuBar.getMenus().addAll(file,help);



        return menuBar;

    } //meNuBar


    /**
     *This method creates a ToolBar object that contains the pause/play button as well as the
     * the update image button.
     * @return ToolBar object containing the functional tool bar
     */


    public ToolBar toolBarMaker () {

        pause = new Button("Play");

        Thread task1 = new Thread(() -> {

            pause.setOnAction(e -> {
                imageRotater(); //make this method
            });

        });

        task1.setDaemon(true);
        task1.start();
        searchQuery = new Text("Search Query");

        searchText = new TextField();

        updateImage = new Button("Update Images");

        updateImage.setOnAction(e -> {
            // boolean isRunning = false;
            if (timeline != null) {
                //get status returns the status of the animation
                if (timeline.getStatus() == Animation.Status.RUNNING) {
                    isRunning = true;
                    timeline.pause();
                }
            }
            String readUserText = readUserQuery(searchText);
            progressBar.setProgress(0.0);
            Thread task = new Thread(() -> {
                getMusicAlbums(readUserText);
                //then go back in and run changes to the scene graph on javafx thread
                Platform.runLater(() -> {
                    imgDisplay();
                });
            });
            userInput = readUserQuery(searchText);

            task.setDaemon(true);
            task.start();

            if (isRunning) {
                timeline.play();
            }
        });
        ToolBar mainTool = new ToolBar(pause, searchQuery, searchText, updateImage);
        return mainTool;
    } //toolBar


    /**
     *This method rotates the images, as well as changing the button text to play or pause.
     */

    public void imageRotater () {

        playCount++;

        if (pause.getText().equals("Pause")) {

            play = false;

        } //if


        if (pause.getText().equals("Play")) {

            play = true;

        } //if


        Platform.runLater(() -> {
            ////if the animation is playing, change the button text to pause
            if (play) {
                pause.setText("Pause");
                isRunning = true;
                timeline.play();
            } //if the animation is paused, change the button text to play
            if (!play) {
                pause.setText("Play");
                isRunning = false;
                timeline.pause();
                return;
            }
        });
        if (play) {
            inPlayMode();
        }

    } //imageRotater

    /**
     *This method displays the images ultilizing an imageView object.
     * @return TilePane object that contains the image.
     */

    public TilePane imgDisplay() {

        int numResults = urlResults.length;

        if (numResults < 21) {
            return grid;
        }

        grid.getChildren().clear();

        //update progress bar in here

        int i = 0;
        int counter = 0;
        while (counter != 20 && i < imgHolder.length) {

            System.out.println("Result from url: " + urlResults[i]);

            if ( urlResults[i] != null ) {
                imgHolder[i].setImage(new Image(urlResults[i]));
                imgHolder[i].setFitWidth(100.0);
                imgHolder[i].setFitHeight(100.0);
                grid.getChildren().add(imgHolder[i]);
                counter++;

            } //if

            i++;


        } //while

        //deal with unused url images in here.

        for ( int x = 0; x < unusedResults.size(); x++ ) {

            if (usedResults.contains(unusedResults.get(x))) {

                unusedResults.remove(unusedResults.get(x));
            } //if


        } // for

        return grid;

    } //imgDisplay

    /**
     *This method takes in the user query and encodes it using a URL encoder.
     * @param userInput of type String.
     */

    public void getMusicAlbums (String userInput) {
        grid.setPrefColumns(5);
        grid.setPrefRows(4);

        try {
            encodedString =  URLEncoder.encode(userInput,"UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            System.out.println(e.getMessage());

        } //catch

        if (encodedString != null) {

            try {
                url = new URL("http://itunes.apple.com/search?term=" + encodedString);
            } catch (java.net.MalformedURLException e) {
                System.out.println(e.getMessage());

            }  //catch

            InputStreamReader reader = null;

            parseURL(url, reader);



            if (urlResults.length < 21) {
                Platform.runLater(() -> {
                //mentioning the error
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                        "There are not enough results.",
                        ButtonType.OK);
                    alert.showAndWait();
                    pause.setText("Play");
                });
                return;
            }
        }

        int i = 0;

        while ( i < 20 ) {

            downLoadAlbumCovers(i);

            // increment progress by 0.05
            progress = progress + 0.05;
            progressBar.setProgress(progress);


            i++;

        } //while



    } //getMusicAlbums



          /**
           *This method parses the URL.
           * @param url of type URL that contains the encoded URL.
           * @param reader of type InputStreamReader.
           */

    public void parseURL(URL url,InputStreamReader reader) {

        try {

            reader = new InputStreamReader(url.openStream());

        } catch (java.io.IOException e) {

            throw new RuntimeException(e.getMessage());

        } //catch



        JsonElement JsonElement = JsonParser.parseReader(reader);

        JsonObject root = JsonElement.getAsJsonObject();

        jsonResults = root.getAsJsonArray("results"); // array

        System.out.println("LENGTH OF ARRAY: " + jsonResults.size());

        unusedResults = root.getAsJsonArray("results");

        usedResults = new JsonArray();

        urlResults = new String[jsonResults.size()];

    } //parseURL

           /**
            * This method downloads the album covers using the Json.
            * @param i of type int
            */

    public void downLoadAlbumCovers(int i) {

        JsonObject result = jsonResults.get(i).getAsJsonObject();

        usedResults.add(result);

        JsonElement artworkUrl100 = result.get("artworkUrl100"); //artworkurl

        if (artworkUrl100 != null) {


            String artUrl =  artworkUrl100.getAsString();

            Image image = new Image(artUrl);

            urlResults[i] = artUrl;

            imgHolder[i] = new ImageView();

            imgHolder[i].setImage(image);

        } //if
    } //downLoadAlbumCovers

        /**
         *This method sets the layout up for the progress bar.
         * @return an HBox containing the progress bar.
         */

    public HBox progressBarMethod() {
        HBox  hBox = new HBox();
        progressBar.setLayoutX(25.0);
        progressBar.setLayoutY(500.0);
        Label label = new Label("Images courtesy of  iTunes API");
        hBox.getChildren().addAll(progressBar, label);
        return hBox;
    } // progressBarMethod

       /**
        *This method reads the user query and properly formats it to be read by the iTunes API.
        * @param textField of type  TextField
        * @return String containg user query.
        */
    public String readUserQuery ( TextField textField ) {

        userInput = textField.getText();

        String [] wordsArray = userInput.split(" ");

        userInput = "";

        for (int i = 0; i < wordsArray.length; i++) {
            if (i == 0) {
                userInput = userInput + wordsArray[i];
                continue;
            }
            userInput = userInput + "+" + wordsArray[i];
        }
        return userInput;


    } //readUserQuery

      /**
       * This method creates the TimeLine object in order to set the number of cycles.
       * @param handler of type EventHandler ActionEvent.
       */

    public void createTimeline(EventHandler<ActionEvent> handler) {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        //Defines the number of cycles in this animation
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.getKeyFrames().add(keyFrame);
        //Plays Animation from current position in the direction indicated by rate.
        //If the Animation is running, it has no effect.
        timeline.play();
    } //createTimeline

      /**
       * This method runs every time the appilcation is in play mode.
       * It converts the artworkUrl100 to a string and passes it to the {@code imgReplacement}.
       */

    public void inPlayMode() {
        EventHandler<ActionEvent> handler = (e -> {
            if (jsonResults.size() > 21) {

                if (pause.getText().equals( "Play")) {
                    timeline.pause();
                    return;
                }

                int UnusedAlbumCover = (int) (Math.random()
                        * unusedResults.size());

                int CurrentAlbumCover = (int) (Math.random()
                        * imgHolder.length);

                JsonObject result = unusedResults.get(UnusedAlbumCover)
                        .getAsJsonObject();
                JsonElement artworkUrl100 = result.get("artworkUrl100");
                if (artworkUrl100 != null) {
                    imgReplacement(artworkUrl100, CurrentAlbumCover);
                }
                if (usedResults.contains(unusedResults.get(UnusedAlbumCover))) {
                    unusedResults.remove(unusedResults.get(UnusedAlbumCover));
                }
            }
        });
        createTimeline(handler);
    }

    /**
     *This method replaces the images randomly within the tile pane object.
     * @param  artworkUrl100 of type JsonElement that holds the artwork URL.
     * @param   CurrentAlbumCover of type int.
     */

    public void imgReplacement(JsonElement artworkUrl100, int CurrentAlbumCover) {

        usedResults.add(artworkUrl100);
        String artUrl = artworkUrl100.getAsString();
        Image image = new Image(artUrl);
        imgHolder[CurrentAlbumCover] = new ImageView();
        unusedResults.add(jsonResults.get(CurrentAlbumCover));
        imgHolder[CurrentAlbumCover].setImage(image);
        imgHolder[CurrentAlbumCover].setFitHeight(100.0); //maybe
        imgHolder[CurrentAlbumCover].setFitWidth(100.0);
        grid.getChildren().clear();

        int i = 0;

        while ( i < 20 ) {

            grid.getChildren().add(imgHolder[i]);

            i++;


        } //while
    }






} // GalleryApp
