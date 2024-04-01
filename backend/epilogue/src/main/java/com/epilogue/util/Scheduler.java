package com.epilogue.util;

import com.epilogue.domain.memorial.Memorial;
import com.epilogue.domain.user.User;
import com.epilogue.domain.user.UserStatus;
import com.epilogue.domain.viewer.Viewer;
import com.epilogue.domain.witness.Witness;
import com.epilogue.repository.memorial.MemorialRepository;
import com.epilogue.repository.user.UserRepository;
import com.epilogue.repository.viewer.ViewerRepository;
import com.epilogue.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Scheduler {
    private final MemorialRepository memorialRepository;
    private final ViewerRepository viewerRepository;
    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;

    private final SmsCertificationUtil smsUtil;
    private final EmailUtil emailUtil;

    @Scheduled(cron = "0 0 0 * * *")    // 매일 00시 정각
//    @Scheduled(cron = "0 * * * * *")    // 매분 (테스트용)
    public void deleteMemorial() {

        // 1년 전 날짜 구하기
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        Date oneYearAgo = cal.getTime();
        log.info("일년전 날짜 : {}", oneYearAgo);

        // 1년 넘은 추모관 삭제
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
//        List<Memorial> memorialsOlderThanDate = memorialRepository.findMemorialsOlderThanDate(sdf.format(oneYearAgo));
//        memorialRepository.deleteAll(memorialsOlderThanDate);

        // 사망했지만 유언을 보내지 않은 User 검색
        List<User> findUsers = userRepository.findByUserStatus(UserStatus.DEADANDNOTSEND);

        for (User findUser : findUsers) {
            // 1. 유언장 전송 메소드 호출 (핸드폰 & 이메일)
            List<Viewer> viewers  = viewerRepository.findAllByWillWillSeq(findUser.getWill().getWillSeq());

            for (Viewer v : viewers) {
                if (v.getViewerMobile() == null) continue;
                smsUtil.sendWillLink(v.getViewerMobile(), findUser.getName(), v.getViewerName());
            }

            for (Viewer v : viewers) {
                if (v.getViewerEmail() == null) continue;
                emailUtil.sendWillLink(v.getViewerEmail(), findUser.getName(), v.getViewerName());
            }

            // 2. 추모관 생성
            memorialRepository.save(Memorial.builder()
                    .user(findUser)
                    .goneDate("2024.01.01")
                    .graveName(findUser.getWill().getGraveName())
                    .graveImg(awsS3Service.getGraveImageFromS3(findUser.getWill().getGraveImageAddress()))
                    .createdDate(new Timestamp(System.currentTimeMillis()))
                    .build());

            findUser.setUserStatus(UserStatus.DEADANDSEND);
        }
    }

    @Scheduled(fixedDelay = 30000) // 30초 마다
    public void test() {
    }

}
