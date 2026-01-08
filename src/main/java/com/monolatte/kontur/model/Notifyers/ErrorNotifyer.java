package com.monolatte.kontur.model.Notifyers;

import javafx.scene.control.Alert;

public class ErrorNotifyer extends Notifyer {
    private final Alert.AlertType _type = Alert.AlertType.ERROR;

    public ErrorNotifyer(String title, String header, String message) {
        super(title, header, message);
    }

    public Alert.AlertType getType() {
        return this._type;
    }

    public void apprise() {
        var alert = new Alert(this._type);

        alert.setTitle(super.getTitle());
        alert.setHeaderText(super.getHeader());
        alert.setContentText(super.getMessage());
        alert.showAndWait();
    }
}
