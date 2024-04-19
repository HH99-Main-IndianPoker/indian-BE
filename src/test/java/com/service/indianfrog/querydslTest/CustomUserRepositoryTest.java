package com.service.indianfrog.querydslTest;

import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CustomUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private User user;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .email("test@test.com")
                .nickname("nickname")
                .password("a!123456")
                .build();
        em.persist(user);
        em.flush();
        em.clear();
    }

    @Test
    public void testFindByEmail() {
        assertThat(userRepository.findByEmail("test@test.com"))
                .isNotEmpty()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo("test@test.com");
                });
    }

    @Test
    public void testExistsByEmail() {
        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("asdasd@test.com")).isFalse();
    }

    @Test
    public void testExistsByNickname() {
        assertThat(userRepository.existsByNickname("nickname")).isTrue();
        assertThat(userRepository.existsByNickname("asdasdas")).isFalse();
    }

    @Test
    public void testFindByNickname() {
        assertThat(userRepository.findByNickname("nickname"))
                .isNotNull()
                .matches(u -> u.getNickname().equals("nickname"), "닉네임으로 찾기 실패...");
    }
}
