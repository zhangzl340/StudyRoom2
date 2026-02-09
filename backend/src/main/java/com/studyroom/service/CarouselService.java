package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.Carousel;
import com.studyroom.utils.Result;

public interface CarouselService extends IService<Carousel> {

    Result<?> createCarousel(Carousel carousel);

    Result<?> updateCarousel(Long id, Carousel carousel);

    Result<?> deleteCarousel(Long id);

    Result<?> getCarouselList(Integer page, Integer size);

    Result<?> getCarouselDetail(Long id);

    Result<?> getActiveCarousels();

    Result<?> updateCarouselStatus(Long id, String status);
}
