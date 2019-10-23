package com.yferhaoui.walksafe;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.TreeMap;

import com.yferhaoui.walksafe.data.Output;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.skins.SlimSkin;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class WalkSafe extends Application {

//	public final static void main(final String[] args) {
//		WalkSafe.launch(WalkSafe.class, args);
//	}

	// WalkSafe
	private final Map<Output.DATA, Float> datasSaved = new TreeMap<Output.DATA, Float>();
	private final BorderPane pane = new BorderPane();
	private final Gateway gateway;

	public WalkSafe() throws IOException, TooManyListenersException {
		this.gateway = new Gateway();
	}

	@Override
	public final void init() {

		final Gauge altitude = GaugeBuilder.create()//
				.decimals(2)//
				.minValue(0)//
				.maxValue(Double.valueOf(8848))//
				.unit("mètre")//
				.build();

		final Gauge pression = GaugeBuilder.create()//
				.decimals(2)//
				.minValue(180)//
				.maxValue(1013)//
				.unit("hpa")//
				.build();

		final Gauge oxygen = GaugeBuilder.create()//
				.decimals(2)//
				.minValue(0)//
				.maxValue(100)//
				.unit("%")//
				.build();

		final Gauge temperature = GaugeBuilder.create()//
				.decimals(2)//
				.minValue(-40)//
				.maxValue(60)//
				.unit("°C")//
				.build();

		final Gauge humidite = GaugeBuilder.create()//
				.decimals(2)//
				.minValue(0)//
				.maxValue(100)//
				.unit("%")//
				.build();

		altitude.setSkin(new SlimSkin(altitude));
		pression.setSkin(new SlimSkin(pression));
		oxygen.setSkin(new SlimSkin(oxygen));
		temperature.setSkin(new SlimSkin(temperature));
		humidite.setSkin(new SlimSkin(humidite));

		final VBox altitudeBox = WalkSafe.getTopicBox("Altitude", Color.rgb(229, 115, 115), altitude);
		final VBox pressionBox = WalkSafe.getTopicBox("Pression", Color.rgb(255, 183, 77), pression);
		final VBox oxygenBox = WalkSafe.getTopicBox("Oxygen", Color.rgb(149, 117, 205), oxygen);
		final VBox temperatureBox = WalkSafe.getTopicBox("Température", Color.rgb(77, 208, 225), temperature);
		final VBox humiditeBox = WalkSafe.getTopicBox("Humidite", Color.rgb(129, 199, 132), humidite);

		pane.setPadding(new Insets(20));
		pane.setBackground(new Background(new BackgroundFill(Color.rgb(39, 44, 50), CornerRadii.EMPTY, Insets.EMPTY)));

		final HBox firstPane = new HBox();
		firstPane.setAlignment(Pos.BASELINE_CENTER);
		firstPane.getChildren().add(altitudeBox);
		firstPane.getChildren().add(pressionBox);
		firstPane.getChildren().add(oxygenBox);

		final BorderPane centerPane = new BorderPane();
		final Label alertMessage = new Label("Loading ...");
		alertMessage.setTextFill(Color.WHITE);
		alertMessage.setFont(new Font(50));
		alertMessage.setPadding(new Insets(0, 10, 0, 0));

		final Button refresh = new Button("Refresh");
		refresh.setOnAction(new EventHandler<ActionEvent>() {

			public final void handle(final ActionEvent event) {
				if (WalkSafe.this.gateway.getState().equals(Thread.State.WAITING)) {
					synchronized (WalkSafe.this.gateway) {
						WalkSafe.this.gateway.notifyAll();
					}
				}
			}
		});
		refresh.setFont(new Font(25));

		final String IDLE_BUTTON_STYLE = "-fx-background-color:#1f5800;" + //
				"    -fx-background-radius:30;" + //
				"	 -fx-text-fill: white;" + //
				"    -fx-border-color:#1f5800;" + //
				"    -fx-border-radius:30;" + //
				"    -fx-border-width: 0;" + //
				"    -fx-background-insets: 0;";

		final String HOVERED_BUTTON_STYLE = "-fx-background-color:#6c6c6c;" + //
				"    -fx-background-radius:30;" + //
				"	 -fx-text-fill: #b6b5b5;" + //
				"    -fx-border-color:#6c6c6c;" + //
				"    -fx-border-radius:30;" + //
				"    -fx-border-width: 0;" + //
				"    -fx-background-insets: 0;";

		refresh.setStyle(IDLE_BUTTON_STYLE);
		refresh.setOnMouseEntered(e -> refresh.setStyle(HOVERED_BUTTON_STYLE));
		refresh.setOnMouseExited(e -> refresh.setStyle(IDLE_BUTTON_STYLE));

		final VBox secondPane = new VBox(10);
		secondPane.setAlignment(Pos.BASELINE_CENTER);
		secondPane.getChildren().add(alertMessage);
		secondPane.setPadding(new Insets(10, 10, 10, 10));

		final HBox thirdPane = new HBox();
		thirdPane.setAlignment(Pos.BASELINE_CENTER);
		thirdPane.getChildren().add(temperatureBox);
		thirdPane.getChildren().add(humiditeBox);

		centerPane.setTop(secondPane);
		centerPane.setCenter(thirdPane);

		final Label bottom = new Label("Daniel Jean - Kadir Kaya - Romain De Concini - Yani Ferhaoui");
		bottom.setTextFill(Color.WHITE);
		bottom.setPadding(new Insets(10, 0, 0, 0));
		bottom.setFont(Font.font("Verdana", FontPosture.ITALIC, 10));

		pane.setTop(firstPane);
		pane.setCenter(centerPane);
		pane.setBottom(bottom);
		BorderPane.setAlignment(bottom, Pos.CENTER);

		final Timeline Updater = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
			public void handle(final ActionEvent event) {
				final Map<Output.DATA, Float> datas = WalkSafe.this.gateway.getOutput().getDatas();
				final Float temp = datas.get(Output.DATA.TEMPERATURE);
				final Float press = datas.get(Output.DATA.PRESSION);
				final Float hum = datas.get(Output.DATA.HUMIDITE);
				final Float oxy = datas.get(Output.DATA.OXYGEN);
				final Float alt = datas.get(Output.DATA.ALTITUDE);

				temperature.setValue(temp != null ? temp : temperature.getMinValue());
				pression.setValue(press != null ? press : pression.getMinValue());
				humidite.setValue(hum != null ? hum : humidite.getMinValue());
				oxygen.setValue(oxy != null ? oxy : oxygen.getMinValue());
				altitude.setValue(alt != null ? alt : altitude.getMinValue());

				secondPane.setVisible(!WalkSafe.this.gateway.deviceIsAvailable());
				firstPane.setVisible(WalkSafe.this.gateway.deviceIsAvailable());
				thirdPane.setVisible(WalkSafe.this.gateway.deviceIsAvailable());

				alertMessage.setFont(new Font(WalkSafe.this.gateway.deviceIsAvailable() ? 1 : 50));
				refresh.setFont(new Font(WalkSafe.this.gateway.deviceIsAvailable() ? 1 : 25));

				if (WalkSafe.this.gateway.getOutput().firstDone() && secondPane.getChildren().size() == 1) {
					alertMessage.setText("Please connect the device !");
					secondPane.getChildren().add(refresh);
				}

				WalkSafe.this.treatDatas();

			}
		}));
		Updater.setCycleCount(Timeline.INDEFINITE);
		Updater.play();

	}

	private final void treatDatas() {
		for (final Entry<Output.DATA, Float> e : this.gateway.getOutput().getDatas().entrySet()) {

			if (this.datasSaved.get(e.getKey()) == null) { // Start
				this.datasSaved.put(e.getKey(), e.getValue());

			} else if (this.datasSaved.get(e.getKey()) * 1.05 <= e.getValue()) {
				WalkSafe.runPopup("Warning ! ", "Rising " + e.getKey() + " : ",
						"Stay alert about " + e.getKey().toString().toLowerCase() + " : " + e.getValue());
				this.datasSaved.put(e.getKey(), e.getValue());

			} else if (this.datasSaved.get(e.getKey()) * 0.95 >= e.getValue()) {
				WalkSafe.runPopup("Warning ! ", "Falling " + e.getKey() + " : ",
						"Stay alert about " + e.getKey().toString().toLowerCase() + " : " + e.getValue());
				this.datasSaved.put(e.getKey(), e.getValue());
			}
		}
	}

	private final static void runPopup(final String title, final String headText, final String content) {
		new Thread() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						final Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle(title);
						alert.setHeaderText(headText);
						alert.setContentText(content);
						alert.showAndWait();
					}
				});
			}
		}.start();
	}

	@Override
	public final void start(final Stage stage) {
		final Scene scene = new Scene(this.pane);
		stage.setTitle("WalkSafe");
		stage.setScene(scene);
		stage.show();
		this.gateway.start();

	}

	@Override
	public final void stop() {
		System.exit(0);
	}

	// Static
	private final static VBox getTopicBox(final String title, final Color color, final Gauge gauge) {
		final Rectangle bar = new Rectangle(200, 3);
		bar.setArcWidth(6);
		bar.setArcHeight(6);
		bar.setFill(color);

		final Label label = new Label(title);
		label.setTextFill(color);
		label.setFont(new Font(20));
		label.setAlignment(Pos.CENTER);
		label.setPadding(new Insets(0, 0, 10, 0));

		gauge.setBarColor(color);
		gauge.setBarBackgroundColor(Color.rgb(39, 44, 50));
		gauge.setAnimated(true);
		gauge.setValueColor(Color.WHITE);
		gauge.setUnitColor(Color.WHITE);

		final VBox vBox = new VBox(bar, label, gauge);
		vBox.setSpacing(3);
		vBox.setAlignment(Pos.CENTER);
		vBox.setPadding(new Insets(20, 20, 20, 20));
		return vBox;
	}

	public final static void manageException(final Exception e) {

		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(e.getClass().getSimpleName());
		alert.setHeaderText(e.getMessage());
		alert.setContentText("Would you like to stop the program?");

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			System.exit(0);
		}

	}

	public final static void manageExceptionOutThread(final Exception e) throws InterruptedException {

		final String threadName = "Jacques";
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		final boolean alreadyOpen = threadSet.stream()//
				.filter(o -> o.getName().equals(threadName))//
				.findFirst()//
				.isPresent();

		if (!alreadyOpen) {

			final Thread t = new Thread(threadName) {
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							final Alert alert = new Alert(AlertType.CONFIRMATION);
							alert.setTitle(e.getClass().getSimpleName());
							alert.setHeaderText(e.getMessage());
							alert.setContentText("Would you like to stop the program?");

							final Optional<ButtonType> result = alert.showAndWait();
							if (result.get() == ButtonType.OK) {
								System.exit(0);
							}
						}
					});
				}
			};
			t.start();
			t.join();
		}
	}

}