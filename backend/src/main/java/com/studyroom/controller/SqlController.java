package com.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.entity.*;
import com.studyroom.mapper.*;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/sql")
@Tag(name = "数据库测试接口")
public class SqlController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ViolationMapper violationMapper;

    // ==================== 用户测试 ====================

    @GetMapping("/users/list")
    public Result<?> getUserList() {
        List<User> users = userMapper.selectList(null);
        return Result.success(users);
    }

    @GetMapping("/users/page")
    public Result<?> getUserPage(@RequestParam(defaultValue = "1") Integer pageNum, 
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<User> page = new Page<>(pageNum, pageSize);
        userMapper.selectPage(page, null);
        return Result.success(page);
    }

    @PostMapping("/users/create")
    public Result<?> createUser(@RequestBody User user) {
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setStatus("active");
        user.setCreditScore(100);
        int result = userMapper.insert(user);
        return Result.success(result > 0 ? user : null);
    }

    @PutMapping("/users/update")
    public Result<?> updateUser(@RequestBody User user) {
        user.setUpdatedAt(new Date());
        int result = userMapper.updateById(user);
        return Result.success(result > 0 ? userMapper.selectById(user.getId()) : null);
    }

    @DeleteMapping("/users/delete/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        int result = userMapper.deleteById(id);
        return Result.success(result > 0);
    }

    @GetMapping("/users/get/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        return Result.success(user);
    }

    // ==================== 自习室测试 ====================

    @GetMapping("/rooms/list")
    public Result<?> getRoomList() {
        List<Room> rooms = roomMapper.selectList(null);
        return Result.success(rooms);
    }

    @GetMapping("/rooms/page")
    public Result<?> getRoomPage(@RequestParam(defaultValue = "1") Integer pageNum, 
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Room> page = new Page<>(pageNum, pageSize);
        roomMapper.selectPage(page, null);
        return Result.success(page);
    }

    @PostMapping("/rooms/create")
    public Result<?> createRoom(@RequestBody Room room) {
        room.setCreatedAt(new Date());
        room.setUpdatedAt(new Date());
        room.setStatus("available");
        int result = roomMapper.insert(room);
        return Result.success(result > 0 ? room : null);
    }

    @PutMapping("/rooms/update")
    public Result<?> updateRoom(@RequestBody Room room) {
        room.setUpdatedAt(new Date());
        int result = roomMapper.updateById(room);
        return Result.success(result > 0 ? roomMapper.selectById(room.getId()) : null);
    }

    @DeleteMapping("/rooms/delete/{id}")
    public Result<?> deleteRoom(@PathVariable Long id) {
        int result = roomMapper.deleteById(id);
        return Result.success(result > 0);
    }

    @GetMapping("/rooms/get/{id}")
    public Result<?> getRoomById(@PathVariable Long id) {
        Room room = roomMapper.selectById(id);
        return Result.success(room);
    }

    // ==================== 座位测试 ====================

    @GetMapping("/seats/list")
    public Result<?> getSeatList() {
        List<Seat> seats = seatMapper.selectList(null);
        return Result.success(seats);
    }

    @GetMapping("/seats/by-room/{roomId}")
    public Result<?> getSeatsByRoomId(@PathVariable Long roomId) {
        List<Seat> seats = seatMapper.selectList(
            new LambdaQueryWrapper<Seat>().eq(Seat::getRoomId, roomId)
        );
        return Result.success(seats);
    }

    @PostMapping("/seats/create")
    public Result<?> createSeat(@RequestBody Seat seat) {
        seat.setCreatedAt(new Date());
        seat.setUpdatedAt(new Date());
        seat.setStatus("available");
        int result = seatMapper.insert(seat);
        return Result.success(result > 0 ? seat : null);
    }

    @PutMapping("/seats/update")
    public Result<?> updateSeat(@RequestBody Seat seat) {
        seat.setUpdatedAt(new Date());
        int result = seatMapper.updateById(seat);
        return Result.success(result > 0 ? seatMapper.selectById(seat.getId()) : null);
    }

    @DeleteMapping("/seats/delete/{id}")
    public Result<?> deleteSeat(@PathVariable Long id) {
        int result = seatMapper.deleteById(id);
        return Result.success(result > 0);
    }

    // ==================== 预约测试 ====================

    @GetMapping("/reservations/list")
    public Result<?> getReservationList() {
        List<Reservation> reservations = reservationMapper.selectList(null);
        return Result.success(reservations);
    }

    @GetMapping("/reservations/by-user/{userId}")
    public Result<?> getReservationsByUserId(@PathVariable Long userId) {
        List<Reservation> reservations = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>().eq(Reservation::getUserId, userId)
        );
        return Result.success(reservations);
    }

    @PostMapping("/reservations/create")
    public Result<?> createReservation(@RequestBody Reservation reservation) {
        reservation.setCreatedAt(new Date());
        reservation.setUpdatedAt(new Date());
        reservation.setStatus("active");
//        reservation.setReservationNo("RES" + System.currentTimeMillis());
        int result = reservationMapper.insert(reservation);
        return Result.success(result > 0 ? reservation : null);
    }

    @PutMapping("/reservations/update")
    public Result<?> updateReservation(@RequestBody Reservation reservation) {
        reservation.setUpdatedAt(new Date());
        int result = reservationMapper.updateById(reservation);
        return Result.success(result > 0 ? reservationMapper.selectById(reservation.getId()) : null);
    }

    @DeleteMapping("/reservations/delete/{id}")
    public Result<?> deleteReservation(@PathVariable Long id) {
        int result = reservationMapper.deleteById(id);
        return Result.success(result > 0);
    }

    // ==================== 签到测试 ====================

    @GetMapping("/checkins/list")
    public Result<?> getCheckInList() {
        List<CheckIn> checkIns = checkInMapper.selectList(null);
        return Result.success(checkIns);
    }

    @GetMapping("/checkins/by-user/{userId}")
    public Result<?> getCheckInsByUserId(@PathVariable Long userId) {
        List<CheckIn> checkIns = checkInMapper.selectList(
            new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, userId)
        );
        return Result.success(checkIns);
    }

    @PostMapping("/checkins/create")
    public Result<?> createCheckIn(@RequestBody CheckIn checkIn) {
        checkIn.setCreatedAt(new Date());
        int result = checkInMapper.insert(checkIn);
        return Result.success(result > 0 ? checkIn : null);
    }

    @PutMapping("/checkins/checkout/{id}")
    public Result<?> checkout(@PathVariable Long id, @RequestBody CheckIn checkIn) {
        CheckIn existing = checkInMapper.selectById(id);
        if (existing != null) {
            existing.setCheckOutTime(new Date());
            existing.setStatus("completed");
//            existing.setActualDuration(checkIn.getActualDuration());
            existing.setUpdatedAt(new Date());
            checkInMapper.updateById(existing);
            return Result.success(existing);
        }
        return Result.error("签到记录不存在");
    }

    // ==================== 违规测试 ====================

    @GetMapping("/violations/list")
    public Result<?> getViolationList() {
        List<Violation> violations = violationMapper.selectList(null);
        return Result.success(violations);
    }

    @GetMapping("/violations/by-user/{userId}")
    public Result<?> getViolationsByUserId(@PathVariable Long userId) {
        List<Violation> violations = violationMapper.selectList(
            new LambdaQueryWrapper<Violation>().eq(Violation::getUserId, userId)
        );
        return Result.success(violations);
    }

    @PostMapping("/violations/create")
    public Result<?> createViolation(@RequestBody Violation violation) {
        violation.setCreatedAt(new Date());
        violation.setStatus("unprocessed");
        int result = violationMapper.insert(violation);
        return Result.success(result > 0 ? violation : null);
    }

    @PutMapping("/violations/update")
    public Result<?> updateViolation(@RequestBody Violation violation) {
        violation.setUpdatedAt(new Date());
        int result = violationMapper.updateById(violation);
        return Result.success(result > 0 ? violationMapper.selectById(violation.getId()) : null);
    }

    // ==================== 综合测试 ====================

    @GetMapping("/dashboard")
    public Result<?> getDashboardStats() {
        // 用户总数
        long userCount = userMapper.selectCount(null);
        
        // 自习室总数
        long roomCount = roomMapper.selectCount(null);
        
        // 座位总数
        long seatCount = seatMapper.selectCount(null);
        
        // 预约总数
        long reservationCount = reservationMapper.selectCount(null);
        
        // 签到总数
        long checkInCount = checkInMapper.selectCount(null);
        
        // 违规总数
        long violationCount = violationMapper.selectCount(null);

        return Result.success(
            new java.util.HashMap<String, Object>() {
                {
                    put("userCount", userCount);
                    put("roomCount", roomCount);
                    put("seatCount", seatCount);
                    put("reservationCount", reservationCount);
                    put("checkInCount", checkInCount);
                    put("violationCount", violationCount);
                }
            }
        );
    }

    @GetMapping("/health")
    public Result<?> healthCheck() {
        try {
            // 测试数据库连接
            userMapper.selectCount(null);
            return Result.success("数据库连接正常");
        } catch (Exception e) {
            return Result.error("数据库连接失败: " + e.getMessage());
        }
    }
}
