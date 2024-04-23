package com.service.indianfrog.domain.ranking.service;

import com.service.indianfrog.domain.ranking.dto.Ranking.GetRanking;
import com.service.indianfrog.domain.ranking.dto.Ranking.GetRankingInfo;
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

        List<Object[]> results = userRepository.findUsersWithRank();

        List<GetRanking> rankings = results.stream()
                .map(result -> new GetRanking(
                        (String) result[0], // imageUrl
                        ((Long) result[1]).intValue(), // ranking
                        (String) result[2], // nickname
                        (Integer) result[3]  // points
                ))
                .toList();

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int myRanking = getUserRanking(username);

        return new GetRankingInfo(rankings, user.getImageUrl(), user.getNickname(), myRanking, user.getPoints());

    }

    public int getUserRanking(String username) {

        List<User> userList = userRepository.findAll();

        userList.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Integer.compare(o2.getPoints(), o1.getPoints());
            }
        });

        return IntStream.range(0, userList.size()).filter(i -> userList.get(i).getEmail().equals(username)).findFirst().orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_EMAIL.getMessage())) + 1;
    }



}
