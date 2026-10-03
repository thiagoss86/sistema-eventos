package com.eventos.sistema.sistema_eventos.participante.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch/participantes")
@RequiredArgsConstructor
public class ParticipanteBatchController {

    private final JobOperator jobOperator;
    private final Job importarParticipanteJob;

    @PostMapping("/importar")
    public ResponseEntity<String> importar() throws Exception {

        JobParameters jobParameter = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(
                importarParticipanteJob,
                jobParameter);

        return ResponseEntity.accepted()
                .body("Importação de participantes iniciada com sucesso.");
    }
}
