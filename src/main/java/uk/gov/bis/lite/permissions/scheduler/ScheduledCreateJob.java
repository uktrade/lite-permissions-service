package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.bis.lite.permissions.service.OgelService;

public class ScheduledCreateJob implements Job {

  private OgelService ogelService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    ogelService.processScheduledCreate();
  }

  private void init(JobExecutionContext context) {
    ogelService = (OgelService) context.getMergedJobDataMap().get(Scheduler.OGEL_SERVICE_NAME);
  }
}