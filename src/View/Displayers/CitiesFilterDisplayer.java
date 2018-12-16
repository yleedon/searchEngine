package View.Displayers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.*;

public class CitiesFilterDisplayer extends GridPane {
    private Map<String, CheckBox> checkBoxMap;

    /**
     * Constructor
     *
     * @param cities - the collection of cities to display, if null the displayer will be empty
     */
    public CitiesFilterDisplayer(Collection<String> cities) {
        setCities(cities);
    }

    public CitiesFilterDisplayer(Collection<String> cities, HashSet<String> selectedCities){
        setCities(cities);
        if(selectedCities != null) {
            for (String city : selectedCities) {
                checkBoxMap.get(city).setSelected(true);
            }
        }
    }

    public void setSelectionToAll(boolean select){
        for(String city:checkBoxMap.keySet()){
            checkBoxMap.get(city).setSelected(select);
        }
    }

    /**
     * get the selected cities to filter with
     *
     * @return a Collection of cities(as a String) that the user selected
     */
    public HashSet<String> getSelectedCities() {
        HashSet<String> ans = new HashSet<>();
        for (String city : checkBoxMap.keySet()) {
            if (checkBoxMap.get(city).isSelected()) {
                ans.add(city);
            }
        }
        return ans;
    }

    /**
     * set the displayer's fields by the given collection of cities
     *
     * @param cities - the given collection of cities(as a String)
     */
    private void setCities(Collection<String> cities) {
        if (cities == null)
            return;
        init();
        for (String city : cities) {
            checkBoxMap.put(city, new CheckBox());
        }

        GridPane entry;
        Collection<GridPane> entries = new ArrayList<>();
        for (String city : checkBoxMap.keySet()) {
            entry = new GridPane();
//            entry.getColumnConstraints().add(new ColumnConstraints(50));
//            entry.getColumnConstraints().add(new ColumnConstraints(150));
            entry.add(checkBoxMap.get(city), 0, 0);
            entry.add(new Label(city), 1, 0);

            ((ArrayList<GridPane>) entries).add(entry);
        }

        ListView list = new ListView();
        list.setPrefWidth(500);
        list.getItems().addAll(entries);

        add(list, 0, 1, 2, 1);
    }

    /**
     * initialize all fields and the displayer
     */
    private void init() {
        checkBoxMap = new TreeMap<>();
        getRowConstraints().add(new RowConstraints(40));
        getRowConstraints().add(new RowConstraints(400));

        Label lbl_title = new Label("\t City");
        lbl_title.setStyle("-fx-text-fill: BLUE;-fx-font-weight:bold;");
        add(lbl_title, 1, 0);
    }
}
