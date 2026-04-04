package com.andrew.smartielts.user.service.user;

import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.user.domain.dto.UserProfileUpdateDTO;
import com.andrew.smartielts.user.domain.vo.UserProfileVO;
import com.andrew.smartielts.user.domain.vo.UserStatsVO;

public interface UserService {

    UserProfileVO getProfile();

    UserProfileVO updateProfile(UserProfileUpdateDTO dto);

    UserOverviewVO getOverview();

    UserStatsVO getStats();
}