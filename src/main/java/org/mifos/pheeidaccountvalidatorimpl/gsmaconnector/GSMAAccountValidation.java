package org.mifos.pheeidaccountvalidatorimpl.gsmaconnector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.mifos.pheeidaccountvalidatorimpl.service.AccountValidationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(value = "gsma")
public class GSMAAccountValidation extends AccountValidationService {
    @Value("${gsma_connector.contactpoint}")
    public String gsmaConnectorContactPoint;
    @Value("${gsma_connector.endpoint.account-status}")
    public String accountStatusEndpoint;
    @Override
    public Boolean validateAccount(String financialAddress, String tenant, String paymentModality, String payeeIdentity, String callbackURL){
        accountStatusEndpoint = accountStatusEndpoint.replaceAll("identifierType", paymentModality);
        accountStatusEndpoint = accountStatusEndpoint.replaceAll("identifierId", financialAddress);
        RequestSpecification requestSpec = new RequestSpecBuilder().build();
        requestSpec.relaxedHTTPSValidation();

        Response response = RestAssured.given(requestSpec)
                .baseUri(gsmaConnectorContactPoint)
                .header("Platform-TenantId", tenant)
                .when()
                .get(accountStatusEndpoint)
                .andReturn();
        Integer statusCode = response.then().extract().statusCode();
        String responseBody = response.then().extract().body().asString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseMap = null;
        if(statusCode==200) {
            try {
                responseMap = objectMapper.readValue(responseBody, Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String fieldValue = responseMap.get("fieldName").toString();

            if (responseMap.get("accountStatus").equals("savingsAccountStatusType.active")) {
                return true;
            }
        }

        return false;

    }
}

