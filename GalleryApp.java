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
/**
 * Represents an iTunes GalleryApp!
 */

public class GalleryApp extends Application {

    TilePane grid = new TilePane();
    ImageView imgHolder [] = new ImageView[20];
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


    /** {@inheritdoc} */
    @Override
    public void start(Stage stage) {
        VBox pane = new VBox();
        pane.getChildren().addAll(menuMaker(), toolBarMaker());
        getMusicAlbums("Rick Astley"); //?
        pane.getChildren().add(imgDisplay());
        pane.getChildren().add(progressBarMethod());
        // getMusicAlbums("pop");

         // pane.getChildren().add(imgDisplay());




        // Scene scene = new Scene(pane, 640, 480);

        Scene scene = new Scene(pane, 500, 485);
        // stage.setMaxWidth(640);
        //stage.setMaxHeight(480);
        stage.setTitle("Gallery!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    public MenuBar menuMaker() {

        Menu file = new Menu("File");


        MenuBar menuBar = new MenuBar();

        menuBar.getMenus().add(file);

        MenuItem exit = new MenuItem("Exit");

        file.getItems().add(exit);

        exit.setOnAction(e ->{
		Platform.exit();
		System.exit(0);
	    });

        return menuBar;

    } //meNuBar



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


            boolean isRunning = false;
            if (timeline != null) {
                //get status returns the status of the animation
                if (timeline.getStatus() == Animation.Status.RUNNING) {
                    isRunning = true;
                    timeline.pause();
                }
            }


            String readUserText = readUserQuery(searchText);


            progressBar.setProgress(0.0);
            //run on its own thread


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

    public void imageRotater () {

         playCount++;
         /*  //if playCount is even
        if (playCount % 2 == 0.0) {
            play = true;
        } //if playCount is odd
        if (playCount % 2 != 0.0) {
            play = false;
        }
         */ //not sure about this

         if(pause.getText().equals("Pause")) {

             play = false;

         } //if


         if(pause.getText().equals("Play")) {

             play = true;

         } //if


        Platform.runLater(() -> {
            ////if the animation is playing, change the button text to pause
            if (play) {
                pause.setText("Pause");
                timeline.play();
            } //if the animation is paused, change the button text to play
            if (!play) {
                pause.setText("Play");
                timeline.pause();
                return;
            }
        });
        if (play) {
             inPlayMode();
        }

    } //imageRotater

    public TilePane imgDisplay() {

        int numResults = urlResults.length;

        if (numResults < 21) {
            return grid;
        }

        grid.getChildren().clear();

        //update progress bar in here

        int i = 0;
        int counter = 0;
        while (counter != 20) {

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

        //deal with unused url images in here maybe?

        for ( int x = 0; x < unusedResults.size(); x++ ) {

            if (usedResults.contains(unusedResults.get(x))) {

                unusedResults.remove(unusedResults.get(x));
            } //if


        } // for

        return grid;

    } //imgDisplay

    public void getMusicAlbums (String userInput) {
        grid.setPrefColumns(5);
        grid.setPrefRows(4);

        try{
            encodedString =  URLEncoder.encode(userInput,"UTF-8");
        } catch(java.io.UnsupportedEncodingException e){
            System.out.println(e.getMessage());

        } //catch

        if (encodedString != null){

            try{
                url = new URL("http://itunes.apple.com/search?term=" + encodedString);
            }catch(java.net.MalformedURLException e){
                System.out.println(e.getMessage());

            }//catch

            InputStreamReader reader = null;

            parseURL(url, reader);

            // notEnoughImages(urlResults.length())

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
        } // test

            //ad something here for

            int i = 0;

            while ( i < 20 ) {

                downLoadAlbumCovers(i);

                i++;

            } //while



        } //getMusicAlbums




        public void parseURL(URL url,InputStreamReader reader){

            try {

              reader = new InputStreamReader(url.openStream());

            } catch (java.io.IOException e) {

             throw new RuntimeException(e.getMessage());

           } //catch

            // JsonParser JsonParser  = new JsonParser();

            JsonElement JsonElement = JsonParser.parseReader(reader);

            JsonObject root = JsonElement.getAsJsonObject();

             jsonResults = root.getAsJsonArray("results"); // array

             System.out.println("LENGTH OF ARRAY: " + jsonResults.size());

             unusedResults = root.getAsJsonArray("results");

             usedResults = new JsonArray();

                  urlResults = new String[jsonResults.size()];

        } //parseURL

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
                //maybe this works :]

                //increment progress here ?

                Platform.runLater(() -> incrementProgress());
            } //if
        } //downLoadAlbumCovers


    public HBox progressBarMethod() {
       HBox  hBox = new HBox();
        progressBar.setLayoutX(25.0);
        progressBar.setLayoutY(500.0);
        Label label = new Label("Images courtesy of  iTunes API");
        hBox.getChildren().addAll(progressBar, label);
        return hBox;
    } // progressBarMethod

    public void incrementProgress() {
        // increment progress by 0.05
        progress = progress + 0.05;
        progressBar.setProgress(progress);
    } //incrementProgress

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
          // progressBar = 0.0; //do I need this here?

        return userInput;


    } //readUserQuery


    public void createTimeline(EventHandler<ActionEvent> handler) {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        //Defines the number of cycles in this animation
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.getKeyFrames().add(keyFrame);
        //Plays Animation from current position in the direction indicated by rate.
        //If the Animation is running, it has no effect.
        timeline.play();
    }

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
