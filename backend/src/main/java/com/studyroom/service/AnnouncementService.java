package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.Announcement;
import com.studyroom.utils.Result;

public interface AnnouncementService extends IService<Announcement> {

    Result<?> createAnnouncement(Announcement announcement);

    Result<?> updateAnnouncement(Long id, Announcement announcement);

    Result<?> deleteAnnouncement(Long id);

    Result<?> getAnnouncementList(String type, Integer page, Integer size);

    Result<?> getAnnouncementDetail(Long id);

    Result<?> getActiveAnnouncements();

    Result<?> updateAnnouncementStatus(Long id, Boolean isActive);

    Result<?> getRecentAnnouncements(int limit);
}
