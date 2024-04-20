package com.service.indianfrog.domain.ranking.service;

import com.service.indianfrog.domain.ranking.dto.Ranking.*;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class RankingService {

    private final UserRepository userRepository;

    public RankingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public GetRankingInfo getRanking(String username) {

        List<User> userList = userRepository.findAll();

        userList.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Integer.compare(o2.getPoints(), o1.getPoints());
            }
        });

        List<GetRanking> rankings = IntStream.range(0, userList.size())
                .limit(100)
                .mapToObj(index -> new GetRanking(
                        index + 1,
                        userList.get(index).getNickname(),
                        userList.get(index).getPoints()))
                .toList();

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int myRanking = IntStream.range(0, userList.size()).filter(i -> userList.get(i).getEmail().equals(username)).findFirst().orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_EMAIL.getMessage())) + 1;

        return new GetRankingInfo(rankings, user.getNickname(), myRanking, user.getPoints());

    }



}
