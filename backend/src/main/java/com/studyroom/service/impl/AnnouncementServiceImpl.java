package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Announcement;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.AnnouncementMapper;
import com.studyroom.service.AnnouncementService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public Result<?> createAnnouncement(Announcement announcement) {
        if (save(announcement)) {
            return Result.success("公告创建成功");
        } else {
            throw new BusinessException("公告创建失败");
        }
    }

    @Override
    public Result<?> updateAnnouncement(Long id, Announcement announcement) {
        Announcement existingAnnouncement = getById(id);
        if (existingAnnouncement == null) {
            throw new BusinessException("公告不存在");
        }

        announcement.setId(id);
        if (updateById(announcement)) {
            return Result.success("公告更新成功");
        } else {
            throw new BusinessException("公告更新失败");
        }
    }

    @Override
    public Result<?> deleteAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        if (removeById(id)) {
            return Result.success("公告删除成功");
        } else {
            throw new BusinessException("公告删除失败");
        }
    }

    @Override
    public Result<?> getAnnouncementList(String type, Integer page, Integer size) {
        // 这里应该实现分页查询，暂时跳过
        if (type != null && !type.isEmpty()) {
            return Result.success(announcementMapper.selectByType(type));
        }
        return Result.success(list());
    }

    @Override
    public Result<?> getAnnouncementDetail(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return Result.success(announcement);
    }

    @Override
    public Result<?> getActiveAnnouncements() {
        return Result.success(announcementMapper.selectActiveAnnouncements());
    }

    @Override
    public Result<?> updateAnnouncementStatus(Long id, Boolean isActive) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        announcement.setIsActive(isActive);
        if (updateById(announcement)) {
            return Result.success("公告状态更新成功");
        } else {
            throw new BusinessException("公告状态更新失败");
        }
    }

    @Override
    public Result<?> getRecentAnnouncements(int limit) {
        return Result.success(announcementMapper.selectRecentAnnouncements(limit));
    }
}
