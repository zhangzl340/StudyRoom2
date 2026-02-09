package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.College;
import com.studyroom.utils.Result;

public interface CollegeService extends IService<College> {

    Result<?> getCollegeList();

    Result<?> getCollegeDetail(Long id);

    Result<?> createCollege(College college);

    Result<?> updateCollege(Long id, College college);

    Result<?> deleteCollege(Long id);
}
