CREATE TABLE `article` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `description` varchar(200) DEFAULT NULL COMMENT '文章介绍',
  `content` text NOT NULL COMMENT '文章主要内容',
  `cover` varchar(300) DEFAULT NULL COMMENT '文章封面',
  `view` int DEFAULT '0' COMMENT '文章浏览量',
  `comment` int DEFAULT '0' COMMENT '评论数量',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数',
  `create_time` bigint DEFAULT NULL COMMENT '文章创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `article_cate` (
  `id` int NOT NULL AUTO_INCREMENT,
  `article_id` int NOT NULL COMMENT '文章ID',
  `cate_id` int NOT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `article_cate_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章和分类的中间表';
CREATE TABLE `article_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status` tinyint DEFAULT '1' COMMENT '文章状态',
  `password` varchar(100) DEFAULT '' COMMENT '是否文章加密',
  `is_encrypt` tinyint DEFAULT '0' COMMENT '是否加密',
  `is_draft` tinyint DEFAULT '0' COMMENT '是否为草稿',
  `is_del` tinyint DEFAULT '0' COMMENT '是否删除',
  `article_id` int NOT NULL COMMENT '对应的文章id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `article_config_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章配置表';
CREATE TABLE `article_tag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `article_id` int NOT NULL COMMENT '文章 ID',
  `tag_id` int NOT NULL COMMENT '标签 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `article_tag_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `assistant` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '助手名称',
  `key` varchar(500) NOT NULL COMMENT '密钥',
  `url` varchar(500) NOT NULL COMMENT 'API 地址',
  `model` varchar(255) NOT NULL COMMENT '模型',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否被启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `assistant_pk` (`name`),
  UNIQUE KEY `assistant_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='助手管理';
CREATE TABLE `cate` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '分类名称',
  `url` varchar(255) DEFAULT '/' COMMENT '分类链接',
  `mark` varchar(100) NOT NULL COMMENT '分类标识',
  `level` int DEFAULT NULL COMMENT '分类级别',
  `type` varchar(10) DEFAULT 'cate' COMMENT '导航还是分类',
  `is_hide` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  `order` int NOT NULL DEFAULT '0' COMMENT '分类顺序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE,
  UNIQUE KEY `cate_pk` (`mark`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '评论者名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '评论者头像',
  `content` text NOT NULL COMMENT '评论内容',
  `email` varchar(100) DEFAULT NULL COMMENT '评论者邮箱',
  `url` varchar(500) DEFAULT NULL COMMENT '评论者网站',
  `article_id` int NOT NULL COMMENT '所属文章ID',
  `comment_id` int DEFAULT '0' COMMENT '二级评论',
  `status` int DEFAULT '0' COMMENT '是否审核',
  `create_time` varchar(255) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `env_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` json NOT NULL COMMENT '配置项',
  `notes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '配置备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `env_config_pk_2` (`id`),
  UNIQUE KEY `env_config_pk` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `footprint` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` varchar(1500) DEFAULT NULL COMMENT '内容',
  `address` varchar(255) NOT NULL COMMENT '地址',
  `position` varchar(255) NOT NULL COMMENT '坐标纬度',
  `images` json DEFAULT NULL COMMENT '图片',
  `create_time` bigint NOT NULL COMMENT '时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `footprint_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `link` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '网站标题',
  `description` varchar(255) NOT NULL COMMENT '网站描述',
  `email` varchar(100) DEFAULT NULL COMMENT '网站邮箱',
  `image` varchar(255) NOT NULL COMMENT '网站logo',
  `url` varchar(500) DEFAULT NULL COMMENT '网站链接',
  `rss` varchar(500) DEFAULT NULL,
  `order` int NOT NULL DEFAULT '0' COMMENT '友联顺序',
  `type_id` int NOT NULL COMMENT '网站所绑定的类型',
  `status` int NOT NULL DEFAULT '0' COMMENT '是否审核',
  `create_time` bigint NOT NULL COMMENT '网站创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `link_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '类型名称',
  `is_admin` int NOT NULL DEFAULT '0' COMMENT '用户是否可选择',
  `order` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='网站类型';
CREATE TABLE `milestone` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '标题',
  `description` varchar(2000) DEFAULT NULL COMMENT '描述',
  `image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `tags` json DEFAULT NULL COMMENT '标签',
  `event_date` bigint NOT NULL COMMENT '事件时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `milestone_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人生里程碑';
CREATE TABLE `page_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '配置项名称',
  `value` json NOT NULL COMMENT '配置项值',
  `notes` varchar(255) DEFAULT NULL COMMENT '配置备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `page_config_pk_2` (`id`),
  UNIQUE KEY `page_config_pk_3` (`id`),
  UNIQUE KEY `page_config_pk` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `mood` varchar(16) DEFAULT NULL COMMENT '心情',
  `location` varchar(255) DEFAULT NULL COMMENT '位置',
  `create_time` bigint NOT NULL COMMENT '时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `record_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `record_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `url` varchar(500) DEFAULT NULL,
  `record_id` int NOT NULL,
  `comment_id` int DEFAULT '0',
  `status` int DEFAULT '0',
  `create_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `swiper` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `url` varchar(500) DEFAULT NULL,
  `order` int NOT NULL DEFAULT '0' COMMENT '排序，越小越靠前',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `tag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(50) NOT NULL COMMENT '密码',
  `name` varchar(50) NOT NULL COMMENT '用户名称',
  `email` varchar(100) DEFAULT NULL COMMENT '用户邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `info` varchar(255) DEFAULT NULL COMMENT '用户介绍',
  `create_time` varchar(255) NOT NULL COMMENT '用户创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_pk` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE `user_token` (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` int NOT NULL COMMENT '用户 ID',
  `token` text NOT NULL COMMENT '用户token',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_token_pk_2` (`id`),
  UNIQUE KEY `user_token_pk_3` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户 token';
CREATE TABLE `wall` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT '神秘人' COMMENT '留言人名称',
  `content` varchar(255) NOT NULL COMMENT '留言内容',
  `color` varchar(100) DEFAULT '#ffe3944d' COMMENT '留言墙颜色',
  `email` varchar(100) DEFAULT NULL COMMENT '留言者邮箱',
  `cate_id` int NOT NULL,
  `status` int DEFAULT '0' COMMENT '是否审核',
  `is_choice` int NOT NULL DEFAULT '0' COMMENT '是否为精选留言',
  `create_time` bigint NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `wall_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='留言墙';
CREATE TABLE `wall_cate` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `mark` varchar(100) NOT NULL COMMENT '分类标识',
  `order` int NOT NULL COMMENT '分类顺序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `wall_cate_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='留言分类';
CREATE TABLE `web_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '配置项名称',
  `value` json NOT NULL COMMENT '配置项值',
  `notes` varchar(255) DEFAULT NULL COMMENT '配置备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `web_page1_pk_2` (`id`),
  UNIQUE KEY `web_page1_pk_3` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='网站配置';
