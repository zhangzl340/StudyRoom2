package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Carousel;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.CarouselMapper;
import com.studyroom.service.CarouselService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel> implements CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public Result<?> createCarousel(Carousel carousel) {
        if (save(carousel)) {
            return Result.success("轮播图创建成功");
        } else {
            throw new BusinessException("轮播图创建失败");
        }
    }

    @Override
    public Result<?> updateCarousel(Long id, Carousel carousel) {
        Carousel existingCarousel = getById(id);
        if (existingCarousel == null) {
            throw new BusinessException("轮播图不存在");
        }

        carousel.setId(id);
        if (updateById(carousel)) {
            return Result.success("轮播图更新成功");
        } else {
            throw new BusinessException("轮播图更新失败");
        }
    }

    @Override
    public Result<?> deleteCarousel(Long id) {
        Carousel carousel = getById(id);
        if (carousel == null) {
            throw new BusinessException("轮播图不存在");
        }

        if (removeById(id)) {
            return Result.success("轮播图删除成功");
        } else {
            throw new BusinessException("轮播图删除失败");
        }
    }

    @Override
    public Result<?> getCarouselList(Integer page, Integer size) {
        // 这里应该实现分页查询，暂时跳过
        return Result.success(list());
    }

    @Override
    public Result<?> getCarouselDetail(Long id) {
        Carousel carousel = getById(id);
        if (carousel == null) {
            throw new BusinessException("轮播图不存在");
        }
        return Result.success(carousel);
    }

    @Override
    public Result<?> getActiveCarousels() {
        return Result.success(carouselMapper.selectActiveCarousels());
    }

    @Override
    public Result<?> updateCarouselStatus(Long id, String status) {
        Carousel carousel = getById(id);
        if (carousel == null) {
            throw new BusinessException("轮播图不存在");
        }

        carousel.setStatus(status);
        if (updateById(carousel)) {
            return Result.success("轮播图状态更新成功");
        } else {
            throw new BusinessException("轮播图状态更新失败");
        }
    }
}
