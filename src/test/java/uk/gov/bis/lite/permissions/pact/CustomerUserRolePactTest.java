package uk.gov.bis.lite.permissions.pact;


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import javax.ws.rs.client.ClientBuilder;

/**
 * CustomerUserRolePactTest
 */
public class CustomerUserRolePactTest extends CustomerBasePactTest {

  private CustomerService customerService;

  private final static String PROVIDER = "lite-customer-service";

  private static final String ADMIN_USER_ID = "ADMIN_USER_ID";
  private static final String USER_ID_SUCCESS = "USER_ID_SUCCESS";
  private static final String SITE_REF_SUCCESS = "SITE_REF_SUCCESS";
  private static final String USER_ID_FAIL = "USER_ID_FAIL";
  private static final String SITE_REF_FAIL = "SITE_REF_FAIL";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment updateUserRoleSuccess(PactDslWithProvider builder) {

    return builder
        .given("update user role success")
        .uponReceiving("update user role success")
          .path("/user-roles/user/" + USER_ID_SUCCESS + "/site/" + SITE_REF_SUCCESS)
          .headers(headers())
          .method("POST")
          .body(userRoleParamPactDsl())
            .willRespondWith()
              .status(200)
        .toFragment();
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment updateUserRoleFail(PactDslWithProvider builder) {

    return builder
        .given("update user role fail")
        .uponReceiving("update user role fail")
          .path("/user-roles/user/" + USER_ID_FAIL + "/site/" + SITE_REF_FAIL)
          .headers(headers())
          .method("POST")
          .body(userRoleParamPactDsl())
            .willRespondWith()
              .status(400)
        .toFragment();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "updateUserRoleSuccess")
  public void testUpdateUserRoleSuccessServicePact() throws Exception {
    OgelSubmission sub = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_EXISTING)));
    sub.setAdminUserId(ADMIN_USER_ID);
    sub.setSiteRef(SITE_REF_SUCCESS);
    sub.setUserId(USER_ID_SUCCESS);
    assertThat(customerService.updateUserRole(sub)).isTrue();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "updateUserRoleFail")
  public void testUpdateUserRoleFailServicePact() throws Exception {
    OgelSubmission sub = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_EXISTING)));
    sub.setAdminUserId(ADMIN_USER_ID);
    sub.setSiteRef(SITE_REF_FAIL);
    sub.setUserId(USER_ID_FAIL);
    assertThat(customerService.updateUserRole(sub)).isFalse();
  }

  private PactDslJsonBody userRoleParamPactDsl() {
    // Requires 'ADMIN' value because API UserRoleParam param contains enum RoleType [ADMIN, SUBMITTER, PREPARER]
    return new PactDslJsonBody()
        .stringType("adminUserId")
        .stringType("roleType", "ADMIN")
        .asBody();
  }

}
