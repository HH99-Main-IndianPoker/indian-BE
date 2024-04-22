package com.service.indianfrog.domain.mypage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.service.indianfrog.domain.mypage.dto.MyPageInfo;
import com.service.indianfrog.domain.mypage.dto.MyProfile;
import com.service.indianfrog.domain.mypage.dto.PointChange;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import com.service.indianfrog.global.s3.S3Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final AmazonS3 s3Client;
    private final MeterRegistry registry;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public MyPageService(UserRepository userRepository, S3Service s3Service, AmazonS3 s3Client, MeterRegistry registry) {
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.s3Client = s3Client;
        this.registry = registry;
    }

    @Transactional(readOnly = true)
    public MyPageInfo getMyPage(String username) {
        Timer.Sample getMyPageTimer = Timer.start(registry);

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int ranking = getUserRanking(username) + 1;

        getMyPageTimer.stop(registry.timer("myPage.getPage.time"));
        return new MyPageInfo(user.getNickname(), username, ranking, user.getPoints(), user.getImageUrl());
    }

    @Transactional
    public PointChange pointRecharge(String username, int point) {
        Timer.Sample rechargeTimer = Timer.start(registry);

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        user.updatePoint(point);

        rechargeTimer.stop(registry.timer("myPage.recharge.time"));
        return new PointChange(user.getPoints());
    }

    @Transactional
    public MyProfile updateProfileImg(String username, MultipartFile userImg) throws IOException {
        Timer.Sample updateProfileTimer = Timer.start(registry);

        String originFileName = userImg.getOriginalFilename(); // img 원본 이름
        String s3FileName = UUID.randomUUID() + originFileName;
        String s3UrlText = s3Client.getUrl(bucket, s3FileName).toString();
        s3Service.delete(s3FileName);
        s3Service.upload(userImg, s3FileName);

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        user.imgUpdate(originFileName, s3UrlText);

        updateProfileTimer.stop(registry.timer("myPage.update.time"));
        return new MyProfile(s3UrlText);
    }

    private int getUserRanking(String username) {
        Timer.Sample getRankingTimer = Timer.start(registry);

        List<User> userList = userRepository.findAll();

        userList.sort((o1, o2) -> Integer.compare(o2.getPoints(), o1.getPoints()));

        getRankingTimer.stop(registry.timer("myPage.getRanking.time"));
        return IntStream.range(0, userList.size()).filter(i -> userList.get(i).getEmail().equals(username)).findFirst().orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_EMAIL.getMessage()));
    }
}
