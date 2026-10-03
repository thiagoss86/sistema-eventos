package com.eventos.sistema.sistema_eventos.participante.batch;

import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ParticipanteBatchConfig {

    private final ParticipanteItemReader participanteItemReader;
    private final ParticipanteItemProcessor participanteItemProcessor;
    private final ParticipanteItemWriter participanteItemWriter;

    @Bean
    public Step importarParticipanteStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {

        FlatFileItemReader<ParticipanteCsv> reader = participanteItemReader.reader();

        return new StepBuilder(
                "importarParticipantesStep",
                jobRepository
        )
                .<ParticipanteCsv, Participante>chunk(2)
                .reader(reader)
                .processor(participanteItemProcessor)
                .writer(participanteItemWriter)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job importarParticipanteJob(
            JobRepository jobRepository,
            Step importarParticipanteStep) {

        return new JobBuilder(
                "importarParticipantesJob",
                jobRepository)
                .start(importarParticipanteStep)
                .build();
    }
}
