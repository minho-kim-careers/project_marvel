package major_project.view;

import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;
import major_project.controller.AppController;
import major_project.model.parser.marvel.OfflineMarvelParser;

public class NavBar extends BorderPane {
    private final AppController controller;
    private ComboBox<String> searchBar;
    private boolean musicPlaying = true;

    private HBox top = new HBox();
    private HBox left = new HBox();
    private HBox centre = new HBox();
    private HBox right = new HBox();
    
    public NavBar(AppController controller) {
        this.controller = controller;

        initMenuBar();
        initMusicPlayer();
        initSearchBar();
        initSearchButton();
        initSendReportButton();
        initClearCacheButton();

        this.setTop(top);
        this.setLeft(left);
        this.setCenter(centre);
        this.setRight(right);

        centre.setAlignment(Pos.CENTER);
        centre.setSpacing(15);
        right.setSpacing(15);
    }

    private void initMenuBar() {
        top.setStyle("-fx-border-color: white;");
        MenuBar menuBar = new MenuBar();
        menuBar.setPrefWidth(AppWindow.WIDTH);

        Menu helpMenu = new Menu("Help");

        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction((e) -> new About());

        menuBar.getMenus().add(helpMenu);
        helpMenu.getItems().add(aboutMenuItem);
        top.getChildren().add(menuBar);
    }

    private void initMusicPlayer() {
        Media media = new Media(new File("src\\main\\resources\\avengers_piano.mp3").toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
                musicPlaying = true;
            }
        });
        Button b = new Button("Play/Pause BGM");
        b.setOnAction((e) -> {
            if (musicPlaying) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
            musicPlaying = !musicPlaying;
        });
        HBox hbox = new HBox();
        hbox.setSpacing(100);
        hbox.getChildren().add(b);
        left.getChildren().add(hbox);
    }

    private void initSearchBar() {
        Label label = new Label("Character search:");
        label.setFont(new Font(15));

        searchBar = new ComboBox<>();
        searchBar.setPrefWidth(AppWindow.WIDTH * 0.3);
        searchBar.setEditable(true);
        searchBar.setOnKeyPressed((e) -> {
            if (!getInput().isBlank() || !getInput().isEmpty()) {
                if (e.getCode() == KeyCode.ENTER) {
                    List<String> suggestions = controller.model().getSuggestions(getInput());
                    searchBar.getItems().clear();
                    searchBar.getItems().addAll(suggestions);
                    searchBar.show();
                }
            }
        });
        searchBar.setOnKeyReleased((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
            }
        });
        centre.getChildren().addAll(
            label,
            searchBar
        );
    }

    private void initSearchButton() {
        Button searchButton = new Button("Search");
        searchButton.setOnAction((event) -> {
            if (!getInput().isBlank() || !getInput().isEmpty()) {
                String characterID = controller.model().getCharacterIDByName(getInput());

                if (characterID == null) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("That character does not exist");
                    alert.showAndWait();
                    return;
                }

                controller.view().viewer().setPage(characterID, true);
            }
        });
        centre.getChildren().add(searchButton);
    }

    private void initSendReportButton() {
        Button sendReportButton = new Button("Send Report");
        sendReportButton.setOnAction((event) -> {
            if (controller.view().viewer().getCurrentView() != null) {
                new ReportForm(controller);
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Nothing to report..");
                alert.showAndWait();
            }
        });
        right.getChildren().add(sendReportButton);
    }

    private void initClearCacheButton() {
        Button button = new Button("Clear Cache");
        button.setOnAction((e) -> {
            if (controller.model().marvelParser().getClass() == OfflineMarvelParser.class) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Incompatible with offline mode");
                alert.showAndWait();
                return;
            }
            Alert alert1 = new Alert(AlertType.CONFIRMATION);
            alert1.setHeaderText("Clear cache?");
            Optional<ButtonType> alert2 = alert1.showAndWait();
            if (alert2.get() == ButtonType.OK) {
                controller.model().marvelParser().clearCache();
            } else {}
        });
        right.getChildren().add(button);
    }

    private String getInput() {
        return searchBar.getEditor().getText();
    }
}
