/*
       Copyright 2023 Kyndryl, All Rights Reserved
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class USDExchangeRate {
    private String amount;
    private String base;
    private String date;
    // workaround for unknown currency field. It is stored into the map from the
    // return from calling the api
    @JsonProperty("rates")
    private Map<String, Double> rates;
    @JsonIgnore
    private String currency;
    @JsonIgnore
    private Double currencyRate;

    public USDExchangeRate() {
    }

    public USDExchangeRate(String amount, String base, String date, Map<String, Double> rates) {
        this.amount = amount;
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrency() {
        updateCurrencyRate();
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getCurrencyRate() {
        updateCurrencyRate();
        return currencyRate;
    }

    public void setCurrencyRate(Double currencyRate) {
        this.currencyRate = currencyRate;
    }

    // we have to update the values here because the constructor isn't called when
    // we grab the JSON values
    private void updateCurrencyRate() {
        if (this.currency == null) {
            for (String key : this.rates.keySet()) {
                setCurrency(key);
                setCurrencyRate(this.rates.get(key));
                break;
            }
        }
    }
}
