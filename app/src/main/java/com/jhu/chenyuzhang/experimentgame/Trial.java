package com.jhu.chenyuzhang.experimentgame;

import java.util.ArrayList;
import java.util.Arrays;

public class Trial  {
    private String type; 	// task types - 1: 2Opt2Att, 2: 2Opt4Att, 3: 4Opt2Att, 4: 4Opt4Att
    private ArrayList<String> attributes;   // this is a list of attributes for each trial ordered as in the excel / csv file

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
