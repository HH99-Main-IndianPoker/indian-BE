package com.example.socketpractice.domain.game.repository;

import com.example.socketpractice.domain.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
