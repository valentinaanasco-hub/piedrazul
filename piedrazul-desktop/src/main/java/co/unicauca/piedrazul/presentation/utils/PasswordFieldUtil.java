package co.unicauca.piedrazul.presentation.utils;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PasswordFieldUtil {

    public static void configureVisibility(PasswordField pf, TextField tf, Label lblEye) {
        tf.setManaged(false);
        tf.setVisible(false);
        tf.getStyleClass().addAll(pf.getStyleClass()); 

        lblEye.setOnMouseClicked(e -> {
            if (pf.isVisible()) {
                tf.setText(pf.getText());
                tf.setVisible(true);
                tf.setManaged(true);
                pf.setVisible(false);
                pf.setManaged(false);
                lblEye.setText("🙈"); 
            } else {
                pf.setText(tf.getText());
                pf.setVisible(true);
                pf.setManaged(true);
                tf.setVisible(false);
                tf.setManaged(false);
                lblEye.setText("👁"); 
            }
        });
    }
}
