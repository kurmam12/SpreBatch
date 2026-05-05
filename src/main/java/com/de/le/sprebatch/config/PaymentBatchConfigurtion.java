package com.de.le.sprebatch.config;

import com.de.le.sprebatch.entity.Payment;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
//import org.springframework.batch.core.job.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

@Configuration

public class PaymentBatchConfigurtion {

    // ✅ NO constructor, NO field injection — pure bean factory


    @Bean
    public FlatFileItemReader<Payment> reader() {
        return new FlatFileItemReaderBuilder<Payment>()
                .name("paymentItemReader")
                .resource(new ClassPathResource("payments.csv"))
                .delimited()
                .names("id", "paymentId", "paymentType", "paymentDate", "paymentStatus", "amount")
                .linesToSkip(1)
                .fieldSetMapper(fieldSet -> {
                    Payment payment = new Payment();
                    // ✅ Skip id — @GeneratedValue auto-assigns a new ID every run
                    payment.setPaymentId(fieldSet.readString("paymentId"));
                    payment.setPaymentType(fieldSet.readString("paymentType"));
                    payment.setPaymentDate(LocalDate.parse(
                            fieldSet.readString("paymentDate"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    payment.setPaymentStatus(fieldSet.readString("paymentStatus"));
                    payment.setAmount(fieldSet.readInt("amount"));
                    return payment;
                })
                .build();
    }

   /* public FlatFileItemReader<Payment> reader() {
        return new FlatFileItemReaderBuilder<Payment>()
                .name("paymentItemReader")
                .resource(new ClassPathResource("payments.csv"))
                .delimited()
                .names("id","paymentId", "paymentType", "paymentDate", "paymentStatus", "amount")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Payment>() {{
                    setTargetType(Payment.class);
                    setCustomEditors(Map.of(LocalDate.class, new PropertyEditorSupport() {
                        @Override
                        public void setAsText(String text) throws IllegalArgumentException {
                            setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        }
                    }));
                }})
                .build();
    }*/

    @Bean
    public JpaItemWriter<Payment> writer(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Payment>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step paymentStep(JobRepository jobRepository,
                            ItemReader<Payment> reader,
                            JpaItemWriter<Payment> writer,
                            PlatformTransactionManager transactionManager) {
        return new StepBuilder("paymentStep", jobRepository)
                .<Payment, Payment>chunk(5)
                .transactionManager(transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .build();
    }

    @Bean
    public Job importPaymentJob(JobRepository jobRepository, Step paymentStep) {
        return new JobBuilder("importPaymentJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paymentStep)
                .build();
    }
}