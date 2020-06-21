package com.jhu.chenyuzhang.experimentgame;

import java.util.ArrayList;
import java.util.Arrays;

public class Trial  {
    private String[] outcomes;  // each option: win / lose / no outcome
    private String orient;  // spatial orientation - 0: horizontal; 1: vertical;
    private String type;  // task types - 1: 2Opt2Att, 2: 2Opt4Att, 3: 4Opt2Att, 4: 4Opt4Att
    private String dominance;
    private ArrayList<String> attributes;  // this is a list of attributes for each trial ordered as in the excel / csv file

    public Trial(){
    }

    public Trial(String[] columns){
        this.outcomes = Arrays.copyOfRange(columns, 0, 4);
        this.orient = columns[4];
        this.type = columns[5];
        this.dominance = columns[6];
        String[] attributes = Arrays.copyOfRange(columns, 7, columns.length);
        this.attributes = new ArrayList<>(Arrays.asList(attributes));
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

    public String[] getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }
}
