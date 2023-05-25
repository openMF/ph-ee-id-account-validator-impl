package org.mifos.pheeidaccountvalidatorimpl.mojaloopconnector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.common.mojaloop.dto.Party;
import org.mifos.connector.common.mojaloop.dto.PartyIdInfo;
import org.mifos.connector.common.mojaloop.dto.TransactionType;
import org.mifos.connector.common.mojaloop.type.IdentifierType;
import org.mifos.pheeidaccountvalidatorimpl.service.AccountValidationService;
import org.mifos.pheeidaccountvalidatorimpl.zeebe.ZeebeProcessStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.common.mojaloop.type.InitiatorType.CONSUMER;
import static org.mifos.connector.common.mojaloop.type.Scenario.TRANSFER;
import static org.mifos.connector.common.mojaloop.type.TransactionRole.PAYEE;
import static org.mifos.pheeidaccountvalidatorimpl.zeebe.ZeebeVariables.*;

@Service(value = "mojaloop")
public class MojaloopAccountValidation extends AccountValidationService {
    @Autowired
    private ZeebeProcessStarter zeebeProcessStarter;
    private ObjectMapper objectMapper;
    @Override
    public Boolean validateAccount(String financialAddress, String fsp, String paymentModality, String payeeIdentity, String callbackURL) {
        Map<String, Object> extraVariables = new HashMap<>();
        extraVariables.put("payeeIdentity", payeeIdentity);
        TransactionType transactionType = new TransactionType();
        transactionType.setInitiator(PAYEE);
        transactionType.setInitiatorType(CONSUMER);
        transactionType.setScenario(TRANSFER);
        extraVariables.put("initiator", transactionType.getInitiator().name());
        extraVariables.put("initiatorType", transactionType.getInitiatorType().name());
        extraVariables.put("scenario", transactionType.getScenario().name());

        extraVariables.put(IS_RTP_REQUEST, true);
        extraVariables.put(IS_AUTHORISATION_REQUIRED, false);
        extraVariables.put(AUTH_TYPE, "NONE");
        extraVariables.put("payeeIdentity", payeeIdentity);
        extraVariables.put("callbackURL",callbackURL);
        TransactionChannelRequestDTO transactionChannelRequestDTO = new TransactionChannelRequestDTO();
        Party party = new Party();
        PartyIdInfo partyIdInfo = new PartyIdInfo(IdentifierType.MSISDN, financialAddress);
        party.setPartyIdInfo(partyIdInfo);
        transactionChannelRequestDTO.setPayer(party);
        try {
            String transactionId = zeebeProcessStarter.startZeebeWorkflow("mojaloop-account-validation",
                    objectMapper.writeValueAsString(transactionChannelRequestDTO),
                    extraVariables);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
