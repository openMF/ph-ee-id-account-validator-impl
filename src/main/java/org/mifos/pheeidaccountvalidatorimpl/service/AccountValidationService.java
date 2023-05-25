package org.mifos.pheeidaccountvalidatorimpl.service;

public abstract class AccountValidationService {
    public abstract Boolean validateAccount(String financialAddress, String dspId, String paymentModality, String payeeIdentity, String callbackURL);

}
