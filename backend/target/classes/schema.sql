CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  role VARCHAR(20) NOT NULL COMMENT '角色：student, admin',
  status VARCHAR(20) NOT NULL COMMENT '状态：active, inactive',
  credit_score INT DEFAULT 100 COMMENT '信用分',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS rooms (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '自习室名称',
  code VARCHAR(50) NOT NULL COMMENT '自习室编码',
  building VARCHAR(100) NOT NULL COMMENT '所在楼栋',
  floor INT NOT NULL COMMENT '楼层',
  capacity INT NOT NULL COMMENT '容量',
  status VARCHAR(20) NOT NULL COMMENT '状态：open, closed, maintenance',
  open_time TIME DEFAULT '08:00:00' COMMENT '开放时间',
  close_time TIME DEFAULT '22:00:00' COMMENT '关闭时间',
  description TEXT DEFAULT NULL COMMENT '描述',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室表';

CREATE TABLE IF NOT EXISTS seats (
  id BIGINT NOT NULL AUTO_INCREMENT,
  room_id BIGINT NOT NULL COMMENT '自习室ID',
  seat_number VARCHAR(20) NOT NULL COMMENT '座位号',
  type VARCHAR(20) DEFAULT 'regular' COMMENT '座位类型：regular, disabled, vip',
  status VARCHAR(20) NOT NULL COMMENT '状态：available, occupied, maintenance',
  position VARCHAR(100) DEFAULT NULL COMMENT '位置描述',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_room_seat (room_id, seat_number),
  FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  room_id BIGINT NOT NULL COMMENT '自习室ID',
  seat_id BIGINT NOT NULL COMMENT '座位ID',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  status VARCHAR(20) NOT NULL COMMENT '状态：pending, active, completed, cancelled, expired, violated',
  reservation_no VARCHAR(50) NOT NULL COMMENT '预约编号',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation_no (reservation_no),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,
  FOREIGN KEY (seat_id) REFERENCES seats (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

CREATE TABLE IF NOT EXISTS check_ins (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL COMMENT '预约ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  check_in_time DATETIME DEFAULT NULL COMMENT '签到时间',
  check_out_time DATETIME DEFAULT NULL COMMENT '签出时间',
  check_in_method VARCHAR(20) DEFAULT NULL COMMENT '签到方式：qrcode, manual',
  status VARCHAR(20) NOT NULL COMMENT '状态：checked_in, checked_out, left, returned',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation (reservation_id),
  FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到表';

CREATE TABLE IF NOT EXISTS violations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  reservation_id BIGINT DEFAULT NULL COMMENT '预约ID',
  type VARCHAR(50) NOT NULL COMMENT '违规类型',
  description TEXT DEFAULT NULL COMMENT '违规描述',
  deduct_credit INT NOT NULL COMMENT '扣除信用分',
  status VARCHAR(20) NOT NULL COMMENT '状态：processed, pending',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违规表';

CREATE TABLE IF NOT EXISTS announcements (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content TEXT NOT NULL COMMENT '内容',
  type VARCHAR(20) DEFAULT 'normal' COMMENT '类型：normal, emergency',
  publish_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  publisher VARCHAR(50) NOT NULL COMMENT '发布人',
  is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

CREATE TABLE IF NOT EXISTS system_settings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  `key` VARCHAR(100) NOT NULL COMMENT '设置键',
  value TEXT DEFAULT NULL COMMENT '设置值',
  description VARCHAR(200) DEFAULT NULL COMMENT '描述',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_key (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

CREATE TABLE IF NOT EXISTS system_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  level VARCHAR(20) NOT NULL COMMENT '日志级别',
  message TEXT NOT NULL COMMENT '日志消息',
  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
  user_id BIGINT DEFAULT NULL COMMENT '用户ID',
  ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  user_agent TEXT DEFAULT NULL COMMENT '用户代理',
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

INSERT INTO system_settings (`key`, value, description) VALUES
('reservation.max_days', '7', '最大预约天数'),
('reservation.max_hours_per_day', '8', '每天最大预约小时数'),
('reservation.min_advance_time', '30', '最小提前预约时间（分钟）'),
('checkin.late_threshold', '15', '签到迟到阈值（分钟）'),
('checkin.leave_timeout', '30', '暂离超时阈值（分钟）'),
('violation.no_show_penalty', '10', '未到违规扣除信用分'),
('violation.late_penalty', '5', '迟到违规扣除信用分'),
('violation.early_leave_penalty', '5', '早退违规扣除信用分'),
('violation.late_checkout_penalty', '5', '晚退违规扣除信用分'),
('credit.minimum', '0', '最低信用分'),
('credit.maximum', '100', '最高信用分'),
('credit.recovery_rate', '1', '每天信用分恢复速率');

INSERT INTO users (username, password, real_name, email, role, status) VALUES
('admin', 'admin123', '系统管理员', 'admin@studyroom.com', 'admin', 'active');
