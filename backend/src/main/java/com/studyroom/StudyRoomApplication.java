package com.studyroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.studyroom.mapper")
public class StudyRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyRoomApplication.class, args);
        System.out.println("========================================");
        System.out.println("自习室预约系统启动成功！");
        System.out.println("接口文档地址: http://localhost:8080/api/doc.htm");
        System.out.println("========================================");
    }
}