package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;
import java.util.Optional;

@Singleton
public class SubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionService.class);

  private OgelSubmissionDao submissionDao;
  private CustomerService customerService;

  private static final String USER_ROLE_UPDATE_STATUS_COMPLETE = "COMPLETE";
  private static final String USER_ROLE_UPDATE_STATUS_ERROR = "Error";

  @Inject
  public SubmissionService(OgelSubmissionDao submissionDao, CustomerService customerService) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
  }

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionDao.findRecentBySubmissionRef(subRef) != null;
  }

  /**
   * Attempts to create Customer, Site and do Role Update (if needed)
   */
  public boolean prepareSubmission(String submissionRef) {
    LOGGER.info("prepareSubmission [" + submissionRef + "]");
    boolean allCreated = true;
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if (sub != null) {
      // Create Customer if needed
      if (sub.needsCustomer()) {
        if (!doCreateCustomer(sub)) {
          allCreated = false;
        }
      }
      // Create Site if needed
      if (sub.needsSite() && allCreated) {
        if (!doCreateSite(sub)) {
          allCreated = false;
        }
      }
      // Update User Role if needed
      if (sub.isRoleUpdate() && allCreated) {
        if (!doUserRoleUpdate(sub)) {
          allCreated = false;
        }
      }
    }
    return allCreated;
  }

  /**
   * If OgelSubmission has not completed processing, set MODE to 'SCHEDULED'
   */
  public void updateModeIfNotCompleted(String submissionRef) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if (!sub.hasCompleted()) {
      LOGGER.info("Updating MODE to SCHEDULED for: [" + submissionRef + "]");
      sub.changeToScheduledMode();
      sub.updateStatus();
      submissionDao.update(sub);
    }
  }

  private boolean doCreateCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.createCustomer(sub);
    boolean created = sarRef.isPresent();
    if (created) {
      sub.setCustomerRef(sarRef.get());
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("Customer created. Updated record: " + sarRef.get());
    }
    return created;
  }

  private boolean doCreateSite(OgelSubmission sub) {
    Optional<String> siteRef = customerService.createSite(sub);
    boolean created = siteRef.isPresent();
    if (created) {
      sub.setSiteRef(siteRef.get());
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("Site created. Updated record: " + siteRef.get());
    }
    return created;
  }

  private boolean doUserRoleUpdate(OgelSubmission sub) {
    Optional<String> status = customerService.updateUserRole(sub);
    boolean created = status.isPresent() && status.get().equals(USER_ROLE_UPDATE_STATUS_COMPLETE);
    if (created) {
      sub.setRoleUpdated(true);
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("User role updated. Updated OgelSubmission: " + sub.getUserId() + "/" + sub.getOgelType());
    }
    return created;
  }

}