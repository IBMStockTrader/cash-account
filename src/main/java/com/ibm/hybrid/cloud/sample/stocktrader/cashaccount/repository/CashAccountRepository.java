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
package com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.model.CashAccount;
import com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.model.USDExchangeRate;

@Repository
public class CashAccountRepository {
    @Value("${jdbc.table}")
    private String tableName;
    @Value("${currency.api.url}")
    private String url;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public class CashAccountRowMapper implements RowMapper<CashAccount> {
        @Override
        public CashAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
            CashAccount account = new CashAccount();
            account.setOwner(rs.getString("owner"));
            account.setBalance(rs.getDouble("balance"));
            account.setCurrency(rs.getString("currency"));
            return account;
        }
    }

    public CashAccount findByOwner(String owner) {
        try {
            List<CashAccount> account = jdbcTemplate.query("SELECT * FROM " + tableName + " WHERE owner=?",
                    new BeanPropertyRowMapper<CashAccount>(CashAccount.class), owner);

            if (account.size() != 1) {
                return null;
            }

            return account.get(0);
        } catch (Error e) {
            return null;
        }
    }

    public int addCashAccount(CashAccount cashAccount) {
        return jdbcTemplate.update("INSERT INTO " + tableName + " (owner, balance, currency) VALUES(?, ?, ?)",
                new Object[] {
                        cashAccount.getOwner(), cashAccount.getBalance(), cashAccount.getCurrency()
                });
    }

    public int updateCashAccount(CashAccount cashAccount) {
        return jdbcTemplate.update("UPDATE " + tableName + " SET owner = ?, balance = ?, currency = ? WHERE owner = ?",
                new Object[] {
                        cashAccount.getOwner(), cashAccount.getBalance(), cashAccount.getCurrency(),
                        cashAccount.getOwner()
                });
    }

    public int subtractBalanceFromAccount(CashAccount cashAccount, double debitAmount) {
        return jdbcTemplate.update("UPDATE " + tableName + " SET balance = ? WHERE owner = ?",
                new Object[] {
                        cashAccount.getBalance() - debitAmount, cashAccount.getOwner()
                });
    }

    public int addBalanceToAccount(CashAccount cashAccount, double creditAmount) {
        return jdbcTemplate.update("UPDATE " + tableName + " SET balance = ? WHERE owner = ?",
                new Object[] {
                        cashAccount.getBalance() + creditAmount, cashAccount.getOwner()
                });
    }

    public int deleteByOwner(String owner) {
        return jdbcTemplate.update("DELETE FROM " + tableName + " WHERE owner = ?", owner);
    }

    // grabs from cache if rate already cached, if not, directly grab from api
    @Cacheable(cacheNames = "currencies", key = "#key")
    public Double getCurrencyRate(String key) {
        if (key.equals("USD")) {
            return (double) 1;
        }
        RestTemplate restTemplate = new RestTemplate();
        USDExchangeRate usdExchangeRate = restTemplate.getForObject(
                url + "?from=USD&to={key}", USDExchangeRate.class, key);
        return usdExchangeRate.getCurrencyRate();
    }
}
