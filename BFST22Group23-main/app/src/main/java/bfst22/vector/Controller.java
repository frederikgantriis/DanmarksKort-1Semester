package bfst22.vector;

import java.io.File;
import java.util.ArrayList;

import bfst22.vector.model.Address;
import bfst22.vector.model.Model;
import bfst22.vector.model.Theme;
import bfst22.vector.model.WayType;
import bfst22.vector.model.ShortesPath.Edge;
import bfst22.vector.model.ShortesPath.TravelType;
import bfst22.vector.model.drawable.Circle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;


public class Controller {
    private Point2D lastMouse;
    private ContextMenu contextMenu;
    
    private View view;
    
    @FXML
    private ComboBox<String> comboBox1;
    @FXML
    private ComboBox<String> comboBox2;
    @FXML
    private MapCanvas canvas;
    
    @FXML
    private ScrollPane scrollPaneShowRoute;

    @FXML
    private StackPane stackPane;

    @FXML
    private Label fpsLabel;
    
    @FXML
    private Label zoomBarLabel;
    
    @FXML
    private Label nearestRoadnameLabel;
    
    @FXML
    private VBox vBoxRouteLabels;

    private Model model;

    // @FXML
    // private Label comboBox1Label;

    @FXML
    private Button removeRoute;
    @FXML
    private Button removeRouteStart;
    @FXML
    private Button removeRouteEnd;

    // @FXML
    // private Label endPointLabel;

    public void init(Model model, View view) {
        this.view = view;
        this.model = model;
        canvas.init(model);
        updateZoomBarLabel();
        // Route route (points) buttons
        removeRoute.setText("Remove Route");
        removeRouteStart.setText("Remove Route Start Point");
        removeRouteEnd.setText("Remove Route End Point");
        removeRoute.setFont(Font.font("Helvetica", FontWeight.EXTRA_BOLD, 15));
        removeRouteStart.setFont(Font.font("Helvetica", FontWeight.EXTRA_BOLD, 15));
        removeRouteEnd.setFont(Font.font("Helvetica", FontWeight.EXTRA_BOLD, 15));
        removeRoute.setTextFill(Color.BLACK);
        removeRouteStart.setTextFill(Color.BLACK);
        removeRouteEnd.setTextFill(Color.BLACK);
        removeRoute.setOnAction(e -> {canvas.setRoute(null); canvas.setRouteStart(null); canvas.setRouteEnd(null); updateRouteUI();});
        removeRouteStart.setOnAction(e -> {canvas.setRouteStart(null); updateRouteUI();});
        removeRouteEnd.setOnAction(e -> {canvas.setRouteEnd(null); updateRouteUI();});
        RepaintAnimationTimer repaintAnimationTimer = new RepaintAnimationTimer(canvas);
        repaintAnimationTimer.start();

        FpsTracker fpsTracker = new FpsTracker(fpsLabel);
        fpsTracker.start();
        Scene scene = fpsLabel.getScene();
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F) {
                if (fpsLabel.isVisible()) {
                    fpsTracker.stop();
                } else{
                    fpsTracker.start();
                    fpsLabel.setText("loading...");
                }
                fpsLabel.setVisible(!fpsLabel.isVisible());
            }
        });


        var tst = model.getTST();
        //ComboBox<String> comboBox1 = new ComboBox<>();
        comboBox1.setVisibleRowCount(5);
        comboBox1.setEditable(true);
        // stackPane.getChildren().add(comboBox1);
        // stackPane.setAlignment(comboBox1, Pos.TOP_LEFT);

        comboBox1.setOnAction(e ->{
            String currentText = comboBox1.getValue().toLowerCase();
            if (!currentText.isEmpty()) { 
                comboBox1.getItems().retainAll(comboBox1.getValue());
                var parsedAddress = Address.parse(currentText).toString().toLowerCase();
                if (tst.contains(parsedAddress)) {
                    canvas.model.getLines().get(WayType.MOUSEMARK).clear();
                    Circle circle = new Circle(Color.RED, tst.get(parsedAddress).getLongitude(), tst.get(parsedAddress).getLatitude(), 20);
                    canvas.addAddressPoint(circle, WayType.MOUSEMARK);
                    canvas.setRouteStart(canvas.getNearestAdress(tst.get(parsedAddress).getLongitude(), tst.get(parsedAddress).getLatitude()));
                    Point2D toPan = canvas.modelToScreen(new Point2D(circle.lon, circle.lat));
                    
                    canvas.pan(scene.getWidth() / 2 - toPan.getX(), scene.getHeight() / 2 - toPan.getY());
                    if (canvas.getZoom() > 0.02) {
                        repaintAnimationTimer.startAutoZoom(scene.getWidth() / 2, scene.getHeight() / 2);
                    }
                    
                    canvas.repaint();
                    comboBox1.getItems().retainAll(comboBox1.getValue());
                    comboBox1.setStyle("-fx-text-inner-color: black;");
                } else {
                    comboBox1.setItems(tst.search(currentText));
                    comboBox1.show();
                    canvas.model.getLines().get(WayType.MOUSEMARK).clear();
                    comboBox1.setStyle("-fx-text-inner-color: red;");
                    canvas.repaint();
                }
            }
            updateRouteUI();
        });

        //endPoint = new ComboBox<>();
        comboBox2.setVisibleRowCount(5);
        comboBox2.setEditable(true);
        // stackPane.getChildren().add(comboBox2);
        // stackPane.setAlignment(comboBox2, Pos.TOP_CENTER);
        comboBox2.setOnAction(e ->{
            String currentText = comboBox2.getValue().toLowerCase();
            if (!currentText.isEmpty()) {  
                comboBox2.getItems().retainAll(comboBox2.getValue());
                var parsedAddress = Address.parse(currentText).toString().toLowerCase();
                if (tst.contains(parsedAddress)) {
                    canvas.model.getLines().get(WayType.MOUSEMARK).clear();
                    Circle circle = new Circle(Color.RED, tst.get(parsedAddress).getLongitude(), tst.get(parsedAddress).getLatitude(), 20);
                    canvas.addAddressPoint(circle, WayType.MOUSEMARK);
                    canvas.setRouteEnd(canvas.getNearestAdress(tst.get(parsedAddress).getLongitude(), tst.get(parsedAddress).getLatitude()));
                    Point2D toPan = canvas.modelToScreen(new Point2D(circle.lon, circle.lat));
                    
                    canvas.pan(scene.getWidth() / 2 - toPan.getX(), scene.getHeight() / 2 - toPan.getY());
                    repaintAnimationTimer.startAutoZoom(scene.getWidth() / 2, scene.getHeight() / 2);
                    
                    canvas.repaint();
                    comboBox2.getItems().retainAll(comboBox2.getValue());
                    comboBox2.setStyle("-fx-text-inner-color: black;");
                } else {
                    comboBox2.setItems(tst.search2(currentText));
                    comboBox2.show();
                    canvas.model.getLines().get(WayType.MOUSEMARK).clear();
                    comboBox2.setStyle("-fx-text-inner-color: red;");
                    canvas.repaint();
                }
            }
            updateRouteUI();
        });

        contextMenu = new ContextMenu();
        MenuItem contextMenuItem1 = new MenuItem("Add start point here");
        MenuItem contextMenuItem2 = new MenuItem("Add end point here");

        contextMenuItem1.setOnAction(e -> {
            Edge ne = canvas.getNearestEdge(contextMenu.getAnchorX(), contextMenu.getAnchorY());
            canvas.setRouteStart(ne);
            System.out.println(ne.getRoadName());
            updateRouteUI();
        });
        contextMenuItem2.setOnAction(e -> {
            Edge ne = canvas.getNearestEdge(contextMenu.getAnchorX(), contextMenu.getAnchorY());
            canvas.setRouteEnd(ne);
            System.out.println(ne.getRoadName());
            updateRouteUI();
        });

        contextMenu.getItems().add(contextMenuItem1);
        contextMenu.getItems().add(contextMenuItem2);

        contextMenu.hide();

    }

    @FXML
    private void onScroll(ScrollEvent e) {
        var factor = e.getDeltaY();
        canvas.zoom(Math.pow(1.05, factor), e.getX(), e.getY());
        updateZoomBarLabel();
    }

    private void updateZoomBarLabel(){
        int zoomIntMeters = canvas.getZoomInMeters();
        if (zoomIntMeters > 999) {
            zoomBarLabel.setText(canvas.getZoomInMeters() / 1000 + " kilometers");    
        } else {
            zoomBarLabel.setText(canvas.getZoomInMeters() + " meters");
        }
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY:
                var dx = e.getX() - lastMouse.getX();
                var dy = e.getY() - lastMouse.getY();
                canvas.pan(dx, dy);
                lastMouse = new Point2D(e.getX(), e.getY());
                break;
            default:
                break;
        }
    }

    @FXML
    private void onMouseMoved(MouseEvent e){
        nearestRoadnameLabel.setText(canvas.getNearestEdge(e.getX(), e.getY()).getRoadName());
    }
    
    @FXML
    private void onMousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY:
                //System.out.println(canvas.getNearestPoint(e.getX(), e.getY()).getRoadName());
                lastMouse = new Point2D(e.getX(), e.getY());
                contextMenu.hide();
                break;
            case SECONDARY:
                contextMenu.show(canvas, e.getX(), e.getSceneY());
                break;
            default:
                System.out.println("Unknown mouse button pressed: " + e.getButton());
                break;
            }
    }
    
    public void updateRouteUI(){
        scrollPaneShowRoute.setVisible(canvas.isRoute());
        if (canvas.isRoute()) {
            ArrayList<String> routeList = new ArrayList<>();
            vBoxRouteLabels.getChildren().clear();
            routeList.add("Total distance to destination: " + String.format("%.1f", canvas.getRoute().getDistance()/1000) + " km.");
            routeList.add("Total time to destination: " + canvas.getRoute().getTimeToTraverse()/60 + " minutes.");
            routeList.addAll(model.getRouteDirections(canvas.getRoute()));
            for (String string : routeList) {
                Label label = new Label(string);
                label.setPrefWidth(vBoxRouteLabels.getWidth());
                label.setMaxWidth(vBoxRouteLabels.getWidth());
                label.setMinWidth(vBoxRouteLabels.getWidth());
                label.setWrapText(true);
                vBoxRouteLabels.getChildren().add(label);
            }
        }
        removeRoute.setDisable(!canvas.isRoute());
        removeRouteStart.setDisable(!canvas.isStartRoute());
        removeRouteEnd.setDisable(!canvas.isEndRoute());
    }



    public void onResizeWindow(){
        if (canvas.isDebugging())
            canvas.updateDrawingRectangle();
        //canvas.repaint();
    }

    @FXML
    private void onOpenFileWindow() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose data file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(".osm", ".obj"));
        var file = fileChooser.showOpenDialog(view.getStage());
        if (file != null) {
            try {
                System.out.println(file.getAbsolutePath());
                new View(new Model(file.getAbsolutePath()), view.getStage());
            }
            catch (Exception e) {
                System.out.println("Failed to load new data file");
                System.out.println(e);
            }
        }
    }






    @FXML
    private void setNormalMode() {
        canvas.setDebugMode(false);
        // canvas.repaint();
    }

    @FXML
    private void toggleDebugDrawOnlyDijkstraMode() {
        canvas.toggleDebugDrawOnlyDijkstraMode();
        // canvas.repaint();
    }
    @FXML
    private void toggleDebugMode() {
        canvas.toggleDebugMode();
        // canvas.repaint();
    }

    @FXML
    private void enableDebugMode() {
        canvas.setDebugMode(true);
        // canvas.repaint();
    }

    @FXML
    private void toggleKDTree(){
        canvas.setKdTreeEnabled(true);
    }

    @FXML
    private void changeTheme(ActionEvent event) {
        MenuItem menuItem = (MenuItem)event.getSource();
        canvas.setTheme(Theme.values()[Integer.parseInt(menuItem.getId())]);
    }

    @FXML
    private void setWindowSize6(ActionEvent event) {
        canvas.setDebugWindowRelation(0.8);
    }

    @FXML
    private void setWindowSize5(ActionEvent event) {
        canvas.setDebugWindowRelation(0.6);
    }

    @FXML
    private void setWindowSize4(ActionEvent event) {
        canvas.setDebugWindowRelation(0.5);
    }

    @FXML
    private void setWindowSize3(ActionEvent event) {
        canvas.setDebugWindowRelation(0.4);
    }

    @FXML
    private void setWindowSize2(ActionEvent event) {
        canvas.setDebugWindowRelation(0.3);
    }

    @FXML
    private void setWindowSize1(ActionEvent event) {
        canvas.setDebugWindowRelation(0.2);
    }

    // @FXML
    // private void showEndPoint(ActionEvent event){
    //     if (endPoint.isVisible()) {
    //         endPoint.setVisible(false);
    //         endPointLabel.setVisible(false);
    //     } else {
    //         endPoint.setVisible(true);
    //         // endPointLabel.setVisible(true);
    //     }
    // }

    @FXML
    private void changeTravelTypeToCar(ActionEvent event){
        canvas.changeTravelType(TravelType.CAR);
    }

    @FXML
    private void changeTravelTypeToBicycle(ActionEvent event){
        canvas.changeTravelType(TravelType.BICYCLE);
    }

    @FXML
    private void changeTravelTypeToFoot(ActionEvent event){
        canvas.changeTravelType(TravelType.FOOT);
    }

   /*  @FXML
    private void getNearestPoint(MouseEvent event){
        try {
            Edge nearestNode = canvas.getNearestPoint(event.getX(), event.getY());
            canvas.drawRedNode(nearestNode);
            System.out.println("nearest node:\n lat: " + nearestNode.getLatitude() + "\n lon: " + nearestNode.getLongitude());
        } catch (NullPointerException e) {
            System.out.println("node is out of reach");
        }
    } */
}
