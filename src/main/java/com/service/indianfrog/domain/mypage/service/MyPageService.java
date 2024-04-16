package com.service.indianfrog.domain.mypage.service;

import com.service.indianfrog.domain.mypage.dto.MyPageInfo;
import com.service.indianfrog.domain.mypage.dto.PointChange;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MyPageInfo getMyPage(String username) {

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int ranking = getUserRanking(username) + 1;

        return new MyPageInfo(user.getNickname(), username, ranking, user.getPoints());
    }

    private int getUserRanking(String username) {

        List<User> userList = userRepository.findAll();

        userList.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Integer.compare(o2.getPoints(), o1.getPoints());
            }
        });

        return IntStream.range(0, userList.size()).filter(i -> userList.get(i).getEmail().equals(username)).findFirst().orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_EMAIL.getMessage()));

    }

    @Transactional
    public PointChange pointRecharge(String username, int point) {

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        user.updatePoint(point);

        return new PointChange(user.getPoints());
    }
}
