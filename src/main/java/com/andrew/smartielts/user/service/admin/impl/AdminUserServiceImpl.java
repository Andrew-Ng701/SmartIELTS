package com.andrew.smartielts.user.service.admin.impl;

import com.andrew.smartielts.auth.domain.pojo.User;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.user.domain.query.admin.AdminDeletedUserPageQuery;
import com.andrew.smartielts.user.domain.query.admin.AdminUserPageQuery;
import com.andrew.smartielts.user.domain.vo.UserAdminDetailVO;
import com.andrew.smartielts.user.domain.vo.UserAdminVO;
import com.andrew.smartielts.user.mapper.UserMapper;
import com.andrew.smartielts.user.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<UserAdminVO> pageActiveUsers(AdminUserPageQuery query) {
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = userMapper.countActive(query);
        List<User> users = userMapper.pageActive(query, offset, pageSize);
        List<UserAdminVO> list = users.stream().map(this::toVO).toList();

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    @Override
    public PageResult<UserAdminVO> pageDeletedUsers(AdminDeletedUserPageQuery query) {
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        int offset = (pageNum - 1) * pageSize;
        String sortDirection = query.getSortDirection() == null ? "DESC" : query.getSortDirection().name();

        Long total = userMapper.countDeleted();
        List<User> users = userMapper.pageDeleted(sortDirection, offset, pageSize);
        List<UserAdminVO> list = users.stream().map(this::toVO).toList();

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    @Override
    public UserAdminDetailVO getUserDetail(Long userId) {
        User user = userMapper.findAnyById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        UserAdminDetailVO vo = new UserAdminDetailVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setIsDeleted(user.getIsDeleted());
        vo.setDeletedTime(user.getDeletedTime());
        vo.setCreatedTime(user.getCreatedTime());
        return vo;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        userMapper.softDeleteById(userId);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        User user = userMapper.findAnyById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getIsDeleted() == null || user.getIsDeleted() == 0) {
            throw new RuntimeException("User is not deleted");
        }
        userMapper.restoreById(userId);
    }

    @Override
    public Long totalUsers() {
        return userMapper.countAllUsers();
    }

    @Override
    public Long activeUsers() {
        return userMapper.countActiveUsers();
    }

    @Override
    public Long deletedUsers() {
        return userMapper.countDeletedUsers();
    }

    private UserAdminVO toVO(User user) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setIsDeleted(user.getIsDeleted());
        vo.setDeletedTime(user.getDeletedTime());
        vo.setCreatedTime(user.getCreatedTime());
        return vo;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
