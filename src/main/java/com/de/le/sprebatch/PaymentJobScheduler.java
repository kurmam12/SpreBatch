package com.de.le.sprebatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component

public class PaymentJobScheduler {

    private final JobOperator jobOperator;
    private final Job importPaymentJob;

    public PaymentJobScheduler(JobOperator jobOperator, Job importPaymentJob) {
        this.jobOperator = jobOperator;
        this.importPaymentJob = importPaymentJob;
    }

    @Scheduled(cron = "${payment.job.cron:0 */5 * * * *}")
    public void runPaymentJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(importPaymentJob, params);
            System.out.println("Job finished with status: " + execution.getStatus());

        } catch (JobInstanceAlreadyCompleteException |
                 JobExecutionAlreadyRunningException |
                 JobRestartException |
                 InvalidJobParametersException e) {
            System.err.println("Job execution failed: " + e.getMessage());
        }
    }
}