package app.drewromanyk.com.minesweeper.models;

/**
 * Created by Drew on 4/18/2015.
 * YesNoDialogInfo
 * Class designed to store a dialog's title and description ( message ).
 */
public class YesNoDialogInfo {
    private String title;
    private String description;

    public YesNoDialogInfo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
