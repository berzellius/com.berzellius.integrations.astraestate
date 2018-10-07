package com.berzellius.integrations.astraestate.batch;

import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.astraestate.businesslogic.processes.LeadsFromSiteService;
import com.berzellius.integrations.astraestate.dmodel.LeadFromSite;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by berz on 14.10.2015.
 */
@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
@PropertySource("classpath:batch.properties")
public class LeadsFromSitesDataProcessing {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    LeadsFromSiteService leadsFromSiteService;

    @Bean
    public ItemReader<LeadFromSite> cleanLeadItemReader(){
        JpaPagingItemReader<LeadFromSite> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        reader.setQueryString("select l from LeadFromSite l where state = :st");
        HashMap<String, Object> params = new LinkedHashMap<>();
        params.put("st", LeadFromSite.State.NEW);
        reader.setParameterValues(params);

        return reader;
    }

    @Bean
    public ItemProcessor<LeadFromSite, LeadFromSite> leadFromSiteItemProcessor(){
        return new ItemProcessor<LeadFromSite, LeadFromSite>() {
            @Override
            public LeadFromSite process(LeadFromSite item) throws Exception {
                leadsFromSiteService.leadFromSiteDataProcessing(item);
                return item;
            }
        };
    }

    @Bean
    public Step leadsFromSiteProcessStep(
            StepBuilderFactory stepBuilderFactory,
            ItemReader<LeadFromSite> cleanLeadItemReader,
            ItemProcessor<LeadFromSite, LeadFromSite> leadFromSiteItemProcessor
    ){
        return stepBuilderFactory.get("leadsFromSiteProcessStep")
                .<LeadFromSite, LeadFromSite>chunk(1)
                .reader(cleanLeadItemReader)
                .processor(leadFromSiteItemProcessor)
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(2000)
                .taskExecutor(taskExecutor())
                .throttleLimit(1)
                .build();
    }

    @Bean
    public Job leadsFromSiteJob(
            Step leadsFromSiteProcessStep
    ){
        RunIdIncrementer runIdIncrementer = new RunIdIncrementer();

        return jobBuilderFactory.get("leadsFromSiteJob")
                .incrementer(runIdIncrementer)
                .flow(leadsFromSiteProcessStep)
                .end()
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(1);
        return taskExecutor;
    }
}
