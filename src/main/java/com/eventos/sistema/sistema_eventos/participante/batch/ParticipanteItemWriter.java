package com.eventos.sistema.sistema_eventos.participante.batch;

import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import com.eventos.sistema.sistema_eventos.participante.repository.ParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipanteItemWriter implements ItemWriter<Participante> {

    private final ParticipanteRepository participanteRepository;

    @Override
    public void write(@NonNull Chunk<? extends Participante> chunk) throws Exception {

        participanteRepository.saveAll(chunk.getItems());
    }
}
