package com.eventos.sistema.sistema_eventos.participante.batch;

import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import com.eventos.sistema.sistema_eventos.participante.repository.ParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipanteItemProcessor  implements ItemProcessor<ParticipanteCsv, Participante> {

    private final ParticipanteRepository participanteRepository;

    @Override
    public @Nullable Participante process(ParticipanteCsv item) throws Exception {

        String nome = item.nome().trim();
        String email = item.email().trim();
        String telefone = item.telefone().trim();

        if(participanteRepository.existsByEmailIgnoreCase(email)){
            return null;
        }

        return Participante.builder()
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .build();
    }
}
