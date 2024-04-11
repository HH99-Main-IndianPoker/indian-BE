package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.UserChoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "게임 레디 서비스 레이어")
public class ReadyGameService {

    public GameState areTheyAllReady(UserChoices userChoices) {
        if (userChoices.getUserOneChoice().equals(UserChoice.READY) && userChoices.getUserTwoChoice().equals(UserChoice.READY)){
            return GameState.ALL_READY;
        }

        if (userChoices.getUserOneChoice().equals(UserChoice.READY) || userChoices.getUserTwoChoice().equals(UserChoice.READY)){
            return GameState.READY;
        }

        return GameState.NO_ONE_READY;
    }
}