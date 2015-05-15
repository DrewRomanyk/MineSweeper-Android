package app.drewromanyk.com.minesweeper.models;

/**
 * Created by Drew on 4/18/2015.
 */
public class YesNoDialogInfo {
    String title;
    String description;

    public YesNoDialogInfo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
