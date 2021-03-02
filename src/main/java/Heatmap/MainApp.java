package Heatmap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainApp extends Application {
    private final int width = 900;
    private final int height = 600;
    private final Pane view = new Pane();
    private final TilePane tp = new TilePane();
    private final ImageView iv = new ImageView();
    private final Label label = new Label();
    private final ArrayList<Double> list = new ArrayList();
    private TextField tfSSID = new TextField("UTB-OPEN");
    private final Slider slider = new Slider();
    private String currentFileName = "";
    private int box;
    private final String borderStyle = "-fx-border-color:rgba(0, 0, 0, 0.2);-fx-border-insets: 0;-fx-border-width:0 1px 1px 0;";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Hej");
        Properties props = System.getProperties();
        props.setProperty("-Dprism.poolstats", "true");
        props.setProperty("-Dprism.verbose", "true");
        VBox main = new VBox();

        HBox toolbar = new HBox(2);

        toolbar.setMinHeight(25);

//        Pane view = new Pane();


//        TilePane tp= new TilePane();


        Button saveImg = new Button("save PNG");
        Button saveProfile = new Button("save Profile");

        Button openImg = new Button("open Image");


        Button bLoad = new Button("Load Profile");
        TextField tfBoxSize = new TextField("30");

        bLoad.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                tp.getChildren().clear();
                list.clear();
                tp.setMinWidth(view.getMinWidth());
                final FileChooser chooser = new FileChooser();
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));

                File fileToRead = chooser.showOpenDialog(null);
                if (fileToRead != null) {
                    String line;
                    if (fileToRead.exists())
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(fileToRead));
                            int boxSize = Integer.parseInt(reader.readLine());
                            tfBoxSize.setText(boxSize+"");
                            while ((line = reader.readLine()) != null) {
                                if (line.equals("null")) {
                                    refreshLoad(-1, boxSize);
                                } else {
                                    refreshLoad(Double.parseDouble(line), boxSize);
                                }

                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                }


            }
        }));


        Button bBoxSize = new Button("set");

        bBoxSize.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                int boxSize = 25;
                try {
                    boxSize = Integer.parseInt(tfBoxSize.getText());
                } catch (Exception ex) {
                    tfBoxSize.setText("25");
                }

                refreshTiles(boxSize);
            }
        }));
        openImg.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                final FileChooser chooser = new FileChooser();
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                try {
                    File fileToRead = chooser.showOpenDialog(null);
                    if (fileToRead != null) {
                        BufferedImage img = ImageIO.read(fileToRead);
                        currentFileName = fileToRead.getName().substring(0, fileToRead.getName().lastIndexOf("."));
                        iv.setImage(SwingFXUtils.toFXImage(img, null));
                        view.setMinWidth(img.getWidth());
                        view.setMinHeight(img.getHeight());
                        view.setMaxWidth(img.getWidth());
                        view.setMaxHeight(img.getHeight());
                        refreshTiles(25);
                    }
                } catch (IOException ex) {
                }
            }
        }));


        saveImg.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                try {

                    SnapshotParameters sp = new SnapshotParameters();

                    WritableImage img = view.snapshot(new SnapshotParameters(), null);
                    FileChooser chooser = new FileChooser();
                    chooser.setInitialFileName(currentFileName + "_HeatMap.png");
                    // chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    BufferedImage img2 = SwingFXUtils.fromFXImage(img, null);
                    try {
                        File fileToSave = chooser.showSaveDialog(null);
                        if (fileToSave != null) {
                            ImageIO.write(img2, "png", fileToSave);
                        }
                    } catch (IOException ex) {
                        label.setText(ex.getMessage());
                    }
                } catch (Exception ex) {
                    label.setText(ex.getMessage());
                }
            }
        }));
        saveProfile.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                final FileChooser chooser = new FileChooser();
                chooser.setInitialFileName(currentFileName + "_Profile.txt");
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                try {
                    File fileToSave = chooser.showSaveDialog(null);
                    if (fileToSave != null) {
                        FileWriter writer = new FileWriter(fileToSave);
                        writer.write(box + "\n");
                        for (int j = 0; j < tp.getChildren().size(); j++) {
                            Pane p = (Pane) tp.getChildren().get(j);
                            writer.write(p.getId() + "\n");
                        }
                        writer.close();
                    }
                } catch (IOException ex) {
                }
            }
        }));

        toolbar.getChildren().addAll(saveImg, saveProfile, tfBoxSize, bBoxSize, openImg, bLoad,tfSSID, slider, label);
        slider.setMin(0);
        slider.setMax(1);
        slider.setValue(0.5);
        tp.setStyle("-fx-opacity:" + 0.5);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                tp.setStyle("-fx-opacity:" + newValue.doubleValue());
            }
        });

        iv.setImage(new Image("img/hus.jpg"));
        view.getChildren().addAll(iv, tp);

        Image img = iv.getImage();
        view.setMinWidth(img.getWidth());
        view.setMinHeight(img.getHeight());
        view.setMaxWidth(img.getWidth());
        view.setMaxHeight(img.getHeight());
        ScrollPane sp = new ScrollPane();

        sp.setContent(view);
        main.getChildren().addAll(toolbar, sp);

        Scene scene = new Scene(main, width, height);
        stage.setScene(scene);
        Package pack = this.getClass().getPackage();
        String packageName = pack.getName();
        stage.setTitle(packageName);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {

                try {
                    FileWriter writer = null;

                    writer = new FileWriter("output.txt");
                    writer.write(box + "\n");
                    for (int j = 0; j < tp.getChildren().size(); j++) {
                        Pane p = (Pane) tp.getChildren().get(j);
                        writer.write(p.getId() + "\n");
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Platform.exit();
                System.exit(0);
            }
        });
        refreshTiles(25);

    }

    private void refreshTiles(int boxSize) {
        if (boxSize < 5) {
            return;
        }
        box = boxSize;
//        System.out.println((view.getWidth()*view.getHeight())/(boxSize*boxSize));
        tp.getChildren().clear();
        list.clear();
        tp.setMinWidth(view.getMinWidth());
        for (int i = 0; i < (view.getMinWidth() * view.getMinHeight()) / (boxSize * boxSize); i++) {
            Pane p = new Pane();
            p.setMinSize(boxSize, boxSize);
            p.setStyle(borderStyle);
            p.setOnMouseClicked((new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    double prec = getWifiLinux();
//                    prec=100;
                    //prec= new Random(System.nanoTime()).nextInt(101);
                    p.setId(prec + "");
//                    prec = (int) (prec * 5.10);
//
//                    int red = 255;
//                    int green = 0;
//
//                    int inc=102;
//                    for (int j = inc; j <= prec; j+=inc) {
//                        if (green < 255) {
//                            green+=inc;
//                        } else {
//                            red-=inc;
//                        }
//                    }
//                    if(green>255)
//                        green=255;
//                    if(red<0)
//                        red=0;
                    int green=0,red=0;

                    if(prec>=80){
                        green=255;
                        red=0;
                    }else if(prec>=60){
                        green=255;
                        red=127;
                    }else if(prec>=40){
                        green=255;
                        red=255;
                    }else if(prec>=20){
                        green=127;
                        red=255;
                    }else if(prec>=0){
                        green=0;
                        red=255;
                    }



                    System.out.println("Green:"+green+" Red:"+red+" Prec:"+prec);
                    ///System.out.println("-fx-background-color:rgba(" + red + "," + green + ", 0, 0.5)");
                    p.setStyle("-fx-background-color:rgba(" + red + "," + green + ", 0, 1);" + borderStyle);


                }


            }));
            p.setOnMouseEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {

                    //    btnsa.setStyle("-fx-background-color:#dae7f3; ");
                    //if(!p.getId().contains("")){
                    label.setText(p.getId() + "%");
                    //}


                }
            });
            tp.getChildren().add(p);
        }
    }

    private void refreshLoad(double prec, int boxSize) {
        Pane p = new Pane();
        p.setMinSize(boxSize, boxSize);
        if (prec != -1) {
            p.setId(prec + "");
            prec = (int) (prec * 5.10);
            int red = 255;
            int green = 0;
            for (int j = 0; j <= prec; j++) {
                if (green != 255) {
                    green++;
                } else {
                    red--;
                }
            }
            p.setStyle("-fx-background-color:rgba(" + red + "," + green + ", 0, 1);" + borderStyle);
        } else {
            p.setStyle(borderStyle);
        }

        p.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                double prec = getWifiLinux();
                p.setId(prec + "");
                prec = (int) (prec * 5.10);

                int red = 255;
                int green = 0;

                for (int j = 0; j <= prec; j++) {
                    if (green != 255) {
                        green++;
                    } else {
                        red--;
                    }
                }
                p.setStyle("-fx-background-color:rgba(" + red + "," + green + ", 0, 1);" + borderStyle);
            }
        }));
        p.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                label.setText(p.getId() + "%");
            }
        });
        tp.getChildren().add(p);

    }

    private int getWifi() {
        int prec = 0;
        try {
            String command = "netsh wlan show interfaces";
            // Executing the command
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            // Getting the results
            powerShellProcess.getOutputStream().close();
            String line;
//        System.out.println("Standard Output:");
            BufferedReader stdout = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getInputStream()));
            String s = "";
            while ((line = stdout.readLine()) != null) {
//            System.out.println(line);
                s += line + "\n";
            }
//        Regex r= new Regex("\\d{1,3}%/g");
            Pattern pattern = Pattern.compile("\\d{1,3}%");
            Matcher matcher = pattern.matcher(s);
            stdout.close();
//        System.out.println(s);

            while (matcher.find()) {
                String text = matcher.group();
                prec = Integer.parseInt(text.substring(0, text.length() - 1));
                break;
            }

            System.out.println(prec);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prec;

    }
    private int getWifiLinux() {
        int prec = 0;
        try {
            String[] command = {"/bin/sh", "-c", "nmcli dev wifi list | grep "+tfSSID.getText()};
            // Executing the command
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            // Getting the results
            powerShellProcess.getOutputStream().close();
            String line;
//        System.out.println("Standard Output:");
            BufferedReader stdout = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getInputStream()));
            String s = "";

            while ((line = stdout.readLine()) != null) {
//            System.out.println(line);
                s += line + "\n";
            }
            System.out.println(s);
//        Regex r= new Regex("\\d{1,3}%/g");
            Pattern pattern = Pattern.compile("(?<=s  )(\\d{1,3})");
            Matcher matcher = pattern.matcher(s);
            stdout.close();
//        System.out.println(s);

            while (matcher.find()) {
                String text = matcher.group();
                prec = Integer.parseInt(text);
                break;
            }

            System.out.println(prec);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prec;

    }
}
