package com.berzellius.integrations.astraestate.batch;

import com.berzellius.integrations.astraestate.businesslogic.processes.IncomingCallBusinessProcess;
import com.berzellius.integrations.astraestate.businesslogic.processes.LeadsFromSiteService;
import com.berzellius.integrations.astraestate.dmodel.LeadAdded;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by berz on 25.06.2017.
 */
@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
@PropertySource("classpath:batch.properties")
public class LeadsAddedProccessingBatchConfiguration {
    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    IncomingCallBusinessProcess incomingCallBusinessProcess;

    @Autowired
    LeadsFromSiteService leadsFromSiteService;

    @PersistenceContext
    EntityManager entityManager;

    //@StepScope
    @Bean
    public ItemReader<LeadAdded> leadAddedItemReader(){
        JpaPagingItemReader<LeadAdded> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        reader.setQueryString("select l from LeadAdded l where state = :st");
        HashMap<String, Object> params = new LinkedHashMap<>();
        params.put("st", LeadAdded.State.NEW);
        reader.setParameterValues(params);

        return reader;
    }

    @Bean
    public ItemProcessor<LeadAdded, LeadAdded> leadAddedItemProcessor(){
        return new ItemProcessor<LeadAdded, LeadAdded>() {
            @Override
            public LeadAdded process(LeadAdded leadAdded) throws Exception {
                try{
                    leadsFromSiteService.processCreatedLead(leadAdded, null);
                    //incomingCallBusinessProcess.processAddedContact(leadAdded);
                }
                catch(RuntimeException e){
                    System.out.println("exception while processing LeadAdded");
                    e.printStackTrace();
                    throw e;
                }

                return leadAdded;
            }
        };
    }

    @Bean
    public Step leadAddedProcessorStep(
            StepBuilderFactory stepBuilderFactory,
            ItemReader<LeadAdded> leadAddedItemReader,
            ItemProcessor<LeadAdded, LeadAdded> leadAddedItemProcessor

    ){
        return stepBuilderFactory.get("leadAddedProcessorStep")
                .<LeadAdded, LeadAdded>chunk(1)
                .reader(leadAddedItemReader)
                .processor(leadAddedItemProcessor)
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(20)
                .build();
    }

    @Bean
    public Job newLeadAddedProcessJob(
            Step leadAddedProcessorStep
    ){
        RunIdIncrementer runIdIncrementer = new RunIdIncrementer();

        return jobBuilderFactory.get("newLeadAddedProcessJob")
                .incrementer(runIdIncrementer)
                .flow(leadAddedProcessorStep)
                .end()
                .build();
    }
}
