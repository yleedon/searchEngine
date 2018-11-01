package View;


import Model.Parse;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class View {
    public TextField fld_text;
    public Button btn_testParse;
    public Button btn_runTests;



    public void testParse() {
        if ((fld_text.getText().equals("")))
            return;

        Parse parser = new Parse(fld_text.getText());
        try {
            parser.parse();
            parser.printIndex();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(parser.toString());
        a.showAndWait();

    }


    //for the test button
    public void runTests(){
        String[][] tests = {
                {"10,123","10.123K"}, {"123 Thousand","123K"}, {"1010.56","1.01056K"},
                {"10,123,000","10.123M"}, {"55 Million","55M"}, {"1010.56","1.01056K"},
                {"10,123,000,000","10.123B"}, {"55 Billion","55B"}, {"7 Trillion","7000B"},
                {"6%","6%"}, {"10.6 percent","10.6%"}, {"10.6 percentage","10.6%"},
                {"1.7320 Dollars","1.7320 Dollars"},{"22 3/4 Dollars","22 3/4 Dollars"},{"22 3/4","22 3/4"},
                {"$450,000","450,000 Dollars"},{"1,000,000 Dollars","1 M Dollars"},{"$450,000,000","450 M Dollars"},
                {"$100 million","100 M Dollars"},{"20.6m Dollars","20.6 M Dollars"},{"$100 billion","100000 M Dollars"},
                {"100bn Dollars","100000 M Dollars"},{"100 billion U.S. dollars","100000 M Dollars"},{"320 million U.S. dollars","320 M Dollars"},
                {"1 trillion U.S. dollars","1000000 M Dollars"},{"14 MAY","05-14"},
                {"26 May","05-26"},{"33 MAY","33 MAY"}
//                ,{"AAA","BBB"},{"AAA","BBB"},
//                {"AAA","BBB"},{"AAA","BBB"},{"AAA","BBB"},
//                {"AAA","BBB"},{"AAA","BBB"},{"AAA","BBB"},
//                {"AAA","BBB"},{"AAA","BBB"},{"AAA","BBB"},
//                {"AAA","BBB"},{"AAA","BBB"},{"AAA","BBB"},
//                {"AAA","BBB"},{"AAA","BBB"},{"AAA","BBB"},
        };


        try {
            String yaniv;
            String ans = "Results:\n";
            Parse parser = new Parse("");
            for(int i = 0; i<tests.length;i++) {
                parser.setTxt(tests[i][0]);
                parser.parse();
                yaniv = removBraces(parser.toString().substring(0,parser.toString().length()-1));
                if(yaniv.equals(tests[i][1])) {

                    ans += "PASSED - Test(" + i + "): [" + tests[i][0] + "] = [" + tests[i][1]+"]\n";
                }
                else {
                    ans +=  "FAILED!!! - Test(" + i + "): [" + tests[i][0] + "] != [" + tests[i][1]+"] --> " + yaniv+"\n";
                }
            }
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText(ans);
            a.showAndWait();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private String removBraces(String s){
        s = s.replace("{","");
        s = s.replace("}","");
        return s;
    }

}
