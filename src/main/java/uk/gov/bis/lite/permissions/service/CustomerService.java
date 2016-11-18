package uk.gov.bis.lite.permissions.service;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.customer.api.param.AddressParam;
import uk.gov.bis.lite.customer.api.param.CustomerParam;
import uk.gov.bis.lite.customer.api.param.SiteParam;
import uk.gov.bis.lite.customer.api.param.UserRoleParam;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.permissions.api.param.RegisterAddressParam;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@Singleton
class CustomerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

  private static final String DEFAULT_SITE_NAME = "Main Site";

  private FailService failService;
  private String customerServiceUrl;
  private Client httpClient;

  @Inject
  public CustomerService(Client httpClient, FailService failService,
                         @Named("customerServiceUrl") String customerServiceUrl) {
    this.httpClient = httpClient;
    this.failService = failService;
    this.customerServiceUrl = customerServiceUrl;
  }

  /**
   * Get Customer using  companyNumber OR Use CustomerService to create Customer
   * Returns sarRef if successful
   * Notifies FailService if there is an error during create process
   */
  Optional<String> getOrCreateCustomer(OgelSubmission sub) {
    // We first attempt to get Customer using the companyNumber
    String companyNumber = getCustomerParam(sub).getCompaniesHouseNumber();
    if (!StringUtils.isBlank(companyNumber)) {
      Optional<String> customerId = getCustomerIdByCompanyNumber(companyNumber);
      if (customerId.isPresent()) {
        return customerId;
      }
    }
    // No Customer exists for companyNumber, so we attempt to create a new one
    return createCustomer(sub);
  }

  /**
   * Uses CustomerService to create Site
   * Returns siteRef if successful, notifies FailService if there is an error
   */
  Optional<String> createSite(OgelSubmission sub) {

    String createSitePath = "/customer-sites/{customerId}";
    String path = createSitePath.replace("{customerId}", sub.getCustomerRef());

    WebTarget target = httpClient.target(customerServiceUrl).queryParam("userId", sub.getUserId()).path(path);
    try {
      Response response = target.request().post(Entity.json(getSiteParam(sub)));
      if (isOk(response)) {
        return Optional.of(response.readEntity(SiteView.class).getSiteId());
      } else if (isForbidden(response)) {
        failService.fail(sub, CallbackView.FailReason.PERMISSION_DENIED, FailService.Origin.SITE);
      } else {
        failService.fail(sub, response, FailService.Origin.SITE);
      }
    } catch (ProcessingException e) {
      failService.fail(sub, e, FailService.Origin.SITE);
    }
    return Optional.empty();
  }

  /**
   * Uses CustomerService to update a UserRole
   * Returns TRUE if successful, notifies FailService if there is an error
   */
  boolean updateUserRole(OgelSubmission sub) {

    String userRolePath = "/user-roles/user/{userId}/site/{siteRef}";
    String path = userRolePath.replace("{userId}", sub.getUserId());
    path = path.replace("{siteRef}", sub.getSiteRef());

    WebTarget target = httpClient.target(customerServiceUrl).path(path);
    Response response = target.request().post(Entity.json(getUserRoleParam(sub)));
    if (isOk(response)) {
      return true;
    } else {
      failService.fail(sub, response, FailService.Origin.USER_ROLE);
    }
    return false;
  }

  /**
   * Uses CustomerService to create Customer
   * Returns sarRef if successful, notifies FailService if there is an error
   */
  private Optional<String> createCustomer(OgelSubmission sub) {

    WebTarget target = httpClient.target(customerServiceUrl).path("/create-customer");
    try {
      Response response = target.request().post(Entity.json(getCustomerParam(sub)));
      if (isOk(response)) {
        return Optional.of(response.readEntity(CustomerView.class).getCustomerId());
      } else {
        failService.fail(sub, response, FailService.Origin.CUSTOMER);
      }
    } catch (ProcessingException e) {
      failService.fail(sub, e, FailService.Origin.CUSTOMER);
    }
    return Optional.empty();
  }

  /**
   * Uses CustomerService to get CustomerId from the companyNumber
   */
  private Optional<String> getCustomerIdByCompanyNumber(String companyNumber) {
    String customerSearchByCompanyNumberPath = "/search-customers/registered-number/{chNumber}";
    WebTarget target = httpClient.target(customerServiceUrl)
        .path(customerSearchByCompanyNumberPath.replace("{chNumber}", companyNumber));
    try {
      Response response = target.request().get();
      if (isOk(response)) {
        CustomerView customer = response.readEntity(CustomerView.class);
        return Optional.of(customer.getCustomerId());
      }
    } catch (ProcessingException e) {
      LOGGER.warn("Exception getCustomerIdByCompanyNumber: " + Throwables.getStackTraceAsString(e));
    }
    return Optional.empty();
  }

  private CustomerParam getCustomerParam(OgelSubmission sub) {
    RegisterParam regParam = sub.getRegisterParamFromJson();
    RegisterParam.RegisterCustomerParam regCustomerParam = regParam.getNewCustomer();
    RegisterAddressParam regAddressParam = regCustomerParam.getRegisteredAddress();

    CustomerParam customerParam = new CustomerParam();
    customerParam.setUserId(sub.getUserId());
    customerParam.setCustomerName(regCustomerParam.getCustomerName());
    customerParam.setAddressParam(getAddressParam(regAddressParam));
    customerParam.setCompaniesHouseNumber(regCustomerParam.getChNumber());
    customerParam.setCompaniesHouseValidated(regCustomerParam.isChNumberValidated());
    customerParam.setCustomerType(regCustomerParam.getCustomerType());
    customerParam.setEoriNumber(regCustomerParam.getEoriNumber());
    customerParam.setEoriValidated(regCustomerParam.isEoriNumberValidated());
    customerParam.setWebsite(regCustomerParam.getWebsite());
    return customerParam;
  }

  private AddressParam getAddressParam(RegisterAddressParam param) {
    AddressParam addressParam = new AddressParam();
    addressParam.setLine1(param.getLine1());
    addressParam.setLine2(param.getLine2());
    addressParam.setTown(param.getTown());
    addressParam.setCounty(param.getCounty());
    addressParam.setPostcode(param.getPostcode());
    addressParam.setCountry(param.getCountry());
    return addressParam;
  }

  private SiteParam getSiteParam(OgelSubmission sub) {
    RegisterParam param = sub.getRegisterParamFromJson();
    RegisterParam.RegisterSiteParam regSiteParam = param.getNewSite();
    String siteName = regSiteParam.getSiteName() != null ? regSiteParam.getSiteName() : DEFAULT_SITE_NAME;
    RegisterAddressParam regAddressParam = regSiteParam.isUseCustomerAddress() ? param.getNewCustomer().getRegisteredAddress() : regSiteParam.getAddress();

    SiteParam siteParam = new SiteParam();
    siteParam.setSiteName(siteName);
    siteParam.setAddressParam(getAddressParam(regAddressParam));
    return siteParam;
  }

  /**
   * Creates a UserRoleParam with an ADMIN roleType
   */
  private UserRoleParam getUserRoleParam(OgelSubmission sub) {
    RegisterParam regParam = sub.getRegisterParamFromJson();
    RegisterParam.RegisterAdminApprovalParam regAdminApprovalParam = regParam.getAdminApproval();

    UserRoleParam userRoleParam = new UserRoleParam();
    userRoleParam.setRoleType(UserRoleParam.RoleType.ADMIN);
    userRoleParam.setAdminUserId(regAdminApprovalParam.getAdminUserId());
    return userRoleParam;
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }

  private boolean isForbidden(Response response) {
    return response != null && (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode());
  }

}
