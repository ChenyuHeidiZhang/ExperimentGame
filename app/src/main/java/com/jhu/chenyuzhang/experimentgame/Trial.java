package com.jhu.chenyuzhang.experimentgame;

import java.util.ArrayList;
import java.util.Arrays;

public class Trial  {
    private String orient;  // spatial orientation - 0: horizontal; 1: vertical;
    private String type;  // task types - 1: 2Opt2Att, 2: 2Opt4Att, 3: 4Opt2Att, 4: 4Opt4Att
    private String dominance;
    private ArrayList<String> attributes;   // this is a list of attributes for each trial ordered as in the excel / csv file

    public Trial(){
    }

    public Trial(String[] columns){
        this.orient = columns[0];
        this.type = columns[1];
        this.dominance = columns[2];
        String[] attributes = Arrays.copyOfRange(columns, 3, columns.length);
        this.attributes = new ArrayList<String>(Arrays.asList(attributes));
    }

    public String getOrient() {
        return this.orient;
    }

    public void setOrient(String orient) {
        this.orient = orient;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDominance() {
        return dominance;
    }

    public void setDominance(String dominance) {
        this.dominance = dominance;
    }

    public ArrayList<String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }
}
