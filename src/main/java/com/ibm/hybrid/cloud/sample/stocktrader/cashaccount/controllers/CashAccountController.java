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
package com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.model.CashAccount;
import com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.repository.CashAccountRepository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController()
@RequestMapping("/cash-account")
public class CashAccountController {
    @Autowired
    CashAccountRepository cashAccountRepository;
    Logger logger = LoggerFactory.getLogger(CashAccountController.class);

    @Hidden
    @GetMapping("/{owner}/currency")
    public ResponseEntity<Double> getConversion(@PathVariable("owner") String owner) {
        CashAccount cashAccount = cashAccountRepository.findByOwner(owner);
        return new ResponseEntity<>(cashAccountRepository.getCurrencyRate(cashAccount.getCurrency()),
                HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gets cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "404", description = "Cash account not found", content = @Content),
    })
    @GetMapping("/{owner}")
    public ResponseEntity<CashAccount> GetCashAccountByOwner(@PathVariable("owner") String owner) {
        CashAccount cashAccount = cashAccountRepository.findByOwner(owner);

        if (cashAccount != null) {
            return new ResponseEntity<>(cashAccount, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Creates cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "409", description = "Conflicting account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal error", content = @Content)
    })
    @PostMapping("/{owner}")
    public ResponseEntity<CashAccount> createCashAccount(@PathVariable("owner") String owner,
            @RequestBody CashAccount cashAccount) {
        try {
            CashAccount account = cashAccountRepository.findByOwner(cashAccount.getOwner());
            if (account != null) {
                return new ResponseEntity<>(account, HttpStatus.CONFLICT);
            }
            CashAccount newCashAccount = new CashAccount(cashAccount.getOwner(), cashAccount.getBalance(),
                    cashAccount.getCurrency());
            cashAccountRepository.addCashAccount(newCashAccount);
            return new ResponseEntity<>(newCashAccount, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updates cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "201", description = "Conflicting account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "404", description = "Cash account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal error", content = @Content)
    })
    @PutMapping("/{owner}")
    public ResponseEntity<CashAccount> updateCashAccount(@PathVariable("owner") String owner,
            @RequestBody CashAccount newCashAccount) {
        CashAccount cashAccount = cashAccountRepository.findByOwner(owner);
        try {
            if (cashAccount != null) {
                if (!owner.equals(newCashAccount.getOwner())
                        && cashAccountRepository.findByOwner(newCashAccount.getOwner()) != null) {
                    return new ResponseEntity<>(cashAccount, HttpStatus.CONFLICT);
                }
                cashAccount.setOwner(owner);
                cashAccount.setBalance(newCashAccount.getBalance());
                cashAccount.setCurrency(newCashAccount.getCurrency());
                cashAccountRepository.updateCashAccount(cashAccount);
                return new ResponseEntity<>(cashAccount, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Debited from cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "400", description = "Debiting from negative cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "404", description = "Cash account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal error", content = @Content)
    })
    @PutMapping("/{owner}/debit")
    public ResponseEntity<CashAccount> debitBalance(@PathVariable("owner") String owner,
            @RequestParam("amount") double amount) {
        CashAccount cashAccount = cashAccountRepository.findByOwner(owner);
        try {
            if (cashAccount != null) {
                double currencyRate = cashAccountRepository.getCurrencyRate(cashAccount.getCurrency());
                if (cashAccount.getBalance() < 0) {
                    return new ResponseEntity<>(cashAccount, HttpStatus.BAD_REQUEST);
                } else if (cashAccount.getBalance() > amount) {
                    cashAccountRepository.subtractBalanceFromAccount(cashAccount, amount * currencyRate);
                    cashAccount = cashAccountRepository.findByOwner(owner);
                    return new ResponseEntity<>(cashAccount, HttpStatus.OK);
                }
                // overdraft fee
                cashAccountRepository.subtractBalanceFromAccount(cashAccount, (amount + 25) * currencyRate);
                cashAccount = cashAccountRepository.findByOwner(owner);
                return new ResponseEntity<>(cashAccount, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credited from cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "404", description = "Cash account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal error", content = @Content)
    })
    @PutMapping("/{owner}/credit")
    public ResponseEntity<CashAccount> creditBalance(@PathVariable("owner") String owner,
            @RequestParam("amount") double amount) {
        CashAccount cashAccount = cashAccountRepository.findByOwner(owner);
        double currencyRate = cashAccountRepository.getCurrencyRate(cashAccount.getCurrency());

        try {
            if (cashAccount != null) {
                cashAccountRepository.addBalanceToAccount(cashAccount, amount * currencyRate);
                cashAccount = cashAccountRepository.findByOwner(owner);
                return new ResponseEntity<>(cashAccount, HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted cash account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CashAccount.class)) }),
            @ApiResponse(responseCode = "404", description = "Cash account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal error", content = @Content)
    })
    @DeleteMapping("/{owner}")
    public ResponseEntity<CashAccount> deleteCashAccount(@PathVariable("owner") String owner) {
        try {
            CashAccount cashAccount = cashAccountRepository.findByOwner(owner);
            int result = cashAccountRepository.deleteByOwner(owner);
            if (result == 0) {
                return new ResponseEntity<>(cashAccount, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(cashAccount, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
