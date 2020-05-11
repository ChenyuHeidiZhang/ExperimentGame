package com.jhu.chenyuzhang.experimentgame;

import java.util.ArrayList;
import java.util.Arrays;

public class Trial  {
    private String type; 	// task types - 1: 2Opt2Att, 2: 2Opt4Att, 3: 4Opt2Att, 4: 4Opt4Att
    private ArrayList<String> attributes;   // this is a list of attributes for each trial ordered as in the excel / csv file

    private String col3, col4;
    private String col5;
    private String col6;
    private String col7;
    private String col8;
    private String col9;
    private String col10;
    private String col11;
    private String col12;
    private String col13;
    private String col14;
    private String col15;
    private String col16;
    private String col17;
    private String col18;

    public Trial(){
    }

    public Trial(String[] columns){
        this.type = columns[0];
        this.attributes = new ArrayList<String>(Arrays.asList(columns));
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }

}
