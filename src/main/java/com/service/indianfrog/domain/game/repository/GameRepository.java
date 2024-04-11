package com.service.indianfrog.domain.game.repository;

import com.service.indianfrog.domain.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {


}
