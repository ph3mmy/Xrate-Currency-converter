package com.jcedar.xratecurrencyconverter.model;

/**
 * Created by OLUWAPHEMMY on 10/17/2016.
 */
public class CurrencyModel {
    private String currencyName, symbol, baseRate, baseCurrencySymbol, currencyCode;
    private Double  invRate;
    private int flag;

    public CurrencyModel (){

    }

    public CurrencyModel (String currencyName, String symbol, String baseRate, String baseCurrencySymbol, String currencyCode, Double invRate, int flag){
        this.flag = flag;
        this.symbol = symbol;
        this.baseCurrencySymbol = baseCurrencySymbol;
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.baseRate = baseRate;
        this.invRate = invRate;
    }


    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(String baseRate) {
        this.baseRate = baseRate;
    }

    public Double getInvRate() {
        return invRate;
    }

    public void setInvRate(Double invRate) {
        this.invRate = invRate;
    }

}
