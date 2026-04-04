package com.andrew.smartielts.user.service.user.impl;

import com.andrew.smartielts.auth.domain.pojo.User;
import com.andrew.smartielts.dashboard.domain.vo.UserModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.service.UserDashboardService;
import com.andrew.smartielts.user.domain.dto.UserProfileUpdateDTO;
import com.andrew.smartielts.user.domain.vo.UserProfileVO;
import com.andrew.smartielts.user.domain.vo.UserStatsVO;
import com.andrew.smartielts.user.mapper.UserMapper;
import com.andrew.smartielts.user.service.user.UserService;
import com.andrew.smartielts.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserDashboardService userDashboardService;

    @Override
    public UserProfileVO getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return toProfileVO(user);
    }

    @Override
    @Transactional
    public UserProfileVO updateProfile(UserProfileUpdateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        String email = dto.getEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email cannot be empty");
        }
        email = email.trim().toLowerCase();

        if (!isValidEmail(email)) {
            throw new RuntimeException("Invalid email format");
        }

        Boolean exists = userMapper.existsActiveEmailExcludeId(email, userId);
        if (Boolean.TRUE.equals(exists)) {
            throw new RuntimeException("Email already registered");
        }

        userMapper.updateEmailById(userId, email);

        User updated = userMapper.findActiveById(userId);
        if (updated == null) {
            throw new RuntimeException("User not found");
        }
        return toProfileVO(updated);
    }

    @Override
    public UserOverviewVO getOverview() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return userDashboardService.overview(userId);
    }

    @Override
    public UserStatsVO getStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return buildStats(userId);
    }

    private UserStatsVO buildStats(Long userId) {
        List<UserModuleStatVO> stats = userDashboardService.userStats(userId);

        UserStatsVO vo = new UserStatsVO();
        vo.setUserId(userId);

        for (UserModuleStatVO stat : stats) {
            if (stat == null || stat.getModule() == null) {
                continue;
            }
            switch (stat.getModule()) {
                case "listening" -> {
                    vo.setListeningActiveRecordCount(stat.getActiveCount());
                    vo.setListeningDeletedRecordCount(stat.getDeletedCount());
                }
                case "reading" -> {
                    vo.setReadingActiveRecordCount(stat.getActiveCount());
                    vo.setReadingDeletedRecordCount(stat.getDeletedCount());
                }
                case "writing" -> {
                    vo.setWritingActiveRecordCount(stat.getActiveCount());
                    vo.setWritingDeletedRecordCount(stat.getDeletedCount());
                }
                case "speaking" -> {
                    vo.setSpeakingActiveRecordCount(stat.getActiveCount());
                    vo.setSpeakingDeletedRecordCount(stat.getDeletedCount());
                }
                default -> {
                }
            }
        }

        vo.setTotalActiveRecordCount(
                safeLong(vo.getListeningActiveRecordCount())
                        + safeLong(vo.getReadingActiveRecordCount())
                        + safeLong(vo.getWritingActiveRecordCount())
                        + safeLong(vo.getSpeakingActiveRecordCount())
        );

        vo.setTotalDeletedRecordCount(
                safeLong(vo.getListeningDeletedRecordCount())
                        + safeLong(vo.getReadingDeletedRecordCount())
                        + safeLong(vo.getWritingDeletedRecordCount())
                        + safeLong(vo.getSpeakingDeletedRecordCount())
        );

        return vo;
    }

    private UserProfileVO toProfileVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setIsDeleted(user.getIsDeleted());
        vo.setDeletedTime(user.getDeletedTime());
        vo.setCreatedTime(user.getCreatedTime());
        return vo;
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}