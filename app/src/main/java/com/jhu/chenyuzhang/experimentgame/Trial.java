package com.jhu.chenyuzhang.experimentgame;

public class Trial  {
    private String amount1;
    private String probability1;
    private String amount2;
    private String probability2;

    public Trial(){
    }

    public Trial(String a1, String p1, String a2, String p2){
        amount1 = a1;
        probability1 = p1;
        amount2 = a2;
        probability2 = p2;
    }


    public String getAmount1() {
        return amount1;
    }

    public void setAmount1(String amount1) {
        this.amount1 = amount1;
    }

    public String getProbability1() {
        return probability1;
    }

    public void setProbability1(String probability1) {
        this.probability1 = probability1;
    }

    public String getAmount2() {
        return amount2;
    }

    public void setAmount2(String amount2) {
        this.amount2 = amount2;
    }

    public String getProbability2() {
        return probability2;
    }

    public void setProbability2(String probability2) {
        this.probability2 = probability2;
    }
}
