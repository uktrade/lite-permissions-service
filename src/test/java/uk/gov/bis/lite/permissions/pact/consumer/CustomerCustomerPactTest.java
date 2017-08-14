package uk.gov.bis.lite.permissions.pact.consumer;


import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import java.util.Optional;

import javax.ws.rs.client.ClientBuilder;

/**
 * CustomerCustomerPactTest
 */
public class CustomerCustomerPactTest extends CustomerBasePactTest {

  private CustomerService customerService;

  private static final String COMPANY_NUMBER_SUCCESS = "COMPANY_NUMBER_SUCCESS";
  private static final String COMPANY_NUMBER_FAIL = "COMPANY_NUMBER_FAIL";
  private static final String CUSTOMER_ID_VALUE = "CUSTOMER_ID_VALUE";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createCustomerSuccess(PactDslWithProvider builder) {

    return builder
        .given("new customer is valid")
        .uponReceiving("request to create a new customer")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(customerParamPactDsl())
          .willRespondWith()
            .headers(headers())
            .status(200)
            .body(customerViewPactDsl())
        .toFragment();
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createCustomerFail(PactDslWithProvider builder) {

    return builder
        .given("new customer is invalid")
        .uponReceiving("request to create a new customer")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .willRespondWith()
            .status(400)
        .toFragment();
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment customerByCompanyNumberSuccess(PactDslWithProvider builder) {

    return builder
        .given("customer successfully retrieved")
        .uponReceiving("request to get customer")
          .path("/search-customers/registered-number/" + COMPANY_NUMBER_SUCCESS)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers())
            .body(customerViewPactDsl())
        .toFragment();
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment customerByCompanyNumberFail(PactDslWithProvider builder) {

    return builder
        .given("customer not found")
        .uponReceiving("request to get customer")
          .path("/search-customers/registered-number/" + COMPANY_NUMBER_FAIL)
          .method("GET")
          .willRespondWith()
            .status(404)
        .toFragment();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "createCustomerSuccess")
  public void testCreateCustomerSuccessServicePact() throws Exception {
    Optional<String> customerRefOpt = customerService.createCustomer(ogelSubmission());
    assertThat(customerRefOpt).isPresent();
    assertThat(customerRefOpt.get()).isEqualTo(CUSTOMER_ID_VALUE);
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "createCustomerFail")
  public void testCreateCustomerFailServicePact() throws Exception {
    OgelSubmission sub = ogelSubmission();
    sub.setUserId("");
    assertThat(customerService.createCustomer(sub)).isNotPresent();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "customerByCompanyNumberSuccess")
  public void testCustomerByCompanyNumberSuccessServicePact() throws Exception {
    Optional<String> customerRefOpt = customerService.getCustomerIdByCompanyNumber(COMPANY_NUMBER_SUCCESS);
    assertThat(customerRefOpt).isPresent();
    assertThat(customerRefOpt.get()).isEqualTo(CUSTOMER_ID_VALUE);
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "customerByCompanyNumberFail")
  public void testCustomerByCompanyNumberFailServicePact() throws Exception {
    assertThat(customerService.getCustomerIdByCompanyNumber(COMPANY_NUMBER_FAIL)).isNotPresent();
  }

  private PactDslJsonBody customerViewPactDsl() {
    return new PactDslJsonBody()
        .stringType("customerId", CUSTOMER_ID_VALUE)
        .asBody();
  }

  private DslPart customerParamPactDsl() {
    return new PactDslJsonBody()
        .stringType("userId", "userId")
        .stringType("customerName", "customerName")
        .stringType("customerType", "customerType")
        .stringType("website", "website")
        .stringType("companiesHouseNumber", "chNumber")
        .booleanType("companiesHouseValidated", false)
        .stringType("eoriNumber", "eoriNumber")
        .booleanType("eoriValidated", false)
        .object("addressParam")
        .stringType("line1", "line1")
        .stringType("line2", "line2")
        .stringType("town", "town")
        .stringType("county", "county")
        .stringType("postcode", "postcode")
        .stringType("country", "country")
        .closeObject();
  }
}