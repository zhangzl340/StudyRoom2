package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Carousel;

import java.util.List;

public interface CarouselMapper extends BaseMapper<Carousel> {

    List<Carousel> selectActiveCarousels();
}
