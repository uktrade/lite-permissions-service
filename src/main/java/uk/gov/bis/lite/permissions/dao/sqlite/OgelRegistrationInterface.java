package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

public interface OgelRegistrationInterface {

  @SqlQuery("SELECT * FROM LOCAL_OGEL_REGISTRATION WHERE ID = :id")
  @Mapper(OgelRegistrationMapper.class)
  OgelRegistration findById(@Bind("id") int id);

  @SqlUpdate("INSERT INTO LOCAL_OGEL_REGISTRATION (USER_ID, OGEL_TYPE, LITE_ID, CUSTOMER_ID, SITE_ID, JSON, STATUS) " +
      "VALUES (:userId, :ogelType, :liteId, :customerId, :siteId, :json, :status)")
  void insert(@Bind("userId") String userId,
              @Bind("ogelType") String ogelType,
              @Bind("liteId") String liteId,
              @Bind("customerId") String customerId,
              @Bind("siteId") String siteId,
              @Bind("json") String json,
              @Bind("status") String status);


  @SqlQuery("SELECT * FROM LOCAL_OGEL_REGISTRATION WHERE STATUS = 'CREATED'")
  @Mapper(OgelRegistrationMapper.class)
  List<OgelRegistration> getCreated();

}
