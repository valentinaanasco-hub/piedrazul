package co.unicauca.piedrazul.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Diálogo modal de éxito fiel al prototipo. Muestra un ícono verde con
 * checkmark y un mensaje personalizado.
 *
 * @author Valentina Añasco
 */
public class SuccessDialog {

    private static final String COLOR_GREEN = "#2ecc71";
    private static final String COLOR_GREEN_BG = "#eafaf1";
    private static final String COLOR_TEXT_DARK = "#1e272e";
    private static final String COLOR_TEXT_GRAY = "#636e72";
    private static final String COLOR_BLUE = "#2c6cf1";

    /**
     * Muestra el diálogo modal de éxito.
     *
     * @param ownerStage Ventana padre sobre la que se bloquea el foco.
     * @param title Texto principal en negrita.
     * @param subtitle Texto secundario debajo del título.
     * @param onClose Acción a ejecutar al cerrar el diálogo.
     */
    public static void show(Stage ownerStage, String title, String subtitle, Runnable onClose) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // --- Ícono circular verde con checkmark ---
        Canvas canvas = new Canvas(72, 72);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Círculo de fondo verde claro
        gc.setFill(Color.web(COLOR_GREEN_BG));
        gc.fillOval(0, 0, 72, 72);

        // Borde verde
        gc.setStroke(Color.web(COLOR_GREEN));
        gc.setLineWidth(2.5);
        gc.strokeOval(1.5, 1.5, 69, 69);

        // Checkmark
        gc.setStroke(Color.web(COLOR_GREEN));
        gc.setLineWidth(3.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        double[] cx = {22, 32, 52};
        double[] cy = {38, 50, 24};
        gc.strokePolyline(cx, cy, cx.length);

        // --- Título ---
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.web(COLOR_TEXT_DARK));
        lblTitle.setWrapText(true);
        lblTitle.setAlignment(Pos.CENTER);

        // --- Subtítulo ---
        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.setFont(Font.font("Segoe UI", 13));
        lblSubtitle.setTextFill(Color.web(COLOR_TEXT_GRAY));
        lblSubtitle.setWrapText(true);
        lblSubtitle.setAlignment(Pos.CENTER);

        // --- Botón continuar ---
        Button btnContinue = new Button("Continuar");
        btnContinue.setMaxWidth(Double.MAX_VALUE);
        btnContinue.setPadding(new Insets(10));
        btnContinue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnContinue.setTextFill(Color.WHITE);
        btnContinue.setStyle(
                "-fx-background-color: " + COLOR_BLUE + ";"
                + "-fx-background-radius: 8px;"
                + "-fx-cursor: hand;"
        );
        btnContinue.setOnAction(e -> {
            dialog.close();
            if (onClose != null) {
                onClose.run();
            }
        });

        // --- Layout ---
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32, 36, 28, 36));
        root.setMaxWidth(300);
        root.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 16px;"
                + "-fx-border-radius: 16px;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 8);"
        );
        root.getChildren().addAll(canvas, lblTitle, lblSubtitle, btnContinue);

        Scene scene = new Scene(root, 320, 290);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }
}
