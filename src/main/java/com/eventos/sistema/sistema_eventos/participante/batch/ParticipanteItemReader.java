package com.eventos.sistema.sistema_eventos.participante.batch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
public class ParticipanteItemReader {

    public FlatFileItemReader<ParticipanteCsv> reader() {

        return new FlatFileItemReaderBuilder<ParticipanteCsv>()
                .name("participanteCsvReader")
                .resource(
                        new org.springframework.core.io.ClassPathResource(
                                "batch/participantes.csv"
                        )
                )
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        "nome",
                        "email",
                        "telefone"
                )
                .fieldSetMapper(fieldSet ->
                        new ParticipanteCsv(
                                fieldSet.readString("nome"),
                                fieldSet.readString("email"),
                                fieldSet.readString("telefone")
                        )
                )
                .build();
    }
}