package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Announcement;

import java.util.List;

public interface AnnouncementMapper extends BaseMapper<Announcement> {

    List<Announcement> selectActiveAnnouncements();

    List<Announcement> selectByType(String type);

    List<Announcement> selectRecentAnnouncements(int limit);
}
