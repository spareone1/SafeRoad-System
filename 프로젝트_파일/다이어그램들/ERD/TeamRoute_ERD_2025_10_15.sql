CREATE TABLE `Obstacle` (
	`id`	BIGINT	NOT NULL,
	`user_id`	BIGINT	NOT NULL,
	`obstacleName`	VARCHAR(255)	NULL,
	`lat`	FLOAT	NOT NULL,
	`lon`	FLOAT	NOT NULL,
	`state`	ENUM	NOT NULL	COMMENT '미완료, 완료, 처리중',
	`created_at`	DATETIME(6)	NOT NULL	COMMENT 'CURRENT_TIMESTAMP',
	`updated_at`	DATETIME(6)	NOT NULL	COMMENT 'CURRENT_TIMESTAMP',
	`location`	VARCHAR(255)	NOT NULL	COMMENT '블랙박스내부 API돌려서나온거받는거임',
	`imageloc`	VARCHAR(255)	NOT NULL
);

CREATE TABLE `User` (
	`id`	BIGINT	NOT NULL,
	`userid`	VARCHAR(255)	NOT NULL,
	`pw`	VARCHAR(255)	NOT NULL,
	`name`	VARCHAR(255)	NULL,
	`email`	VARCHAR(255)	NULL	COMMENT 'UNIQUE',
	`emailpermission`	BOOLEAN	NULL,
	`created_at`	DATETIME	NOT NULL	COMMENT 'CURRENT_TIMESTAMP',
	`updated_at`	DATETIME	NOT NULL	COMMENT 'CURRENT_TIMESTAMP',
	`userstatus`	ENUM	NOT NULL	COMMENT 'deleted, normal_user, admin_user'
);

CREATE TABLE `Socialuserinfo` (
	`id`	BIGINT	NOT NULL,
	`user_id`	BIGINT	NOT NULL,
	`provider`	VARCHAR(255)	NOT NULL,
	`puserid`	VARCHAR(255)	NOT NULL,
	`email`	VARCHAR(255)	NOT NULL
);

ALTER TABLE `Obstacle` ADD CONSTRAINT `PK_OBSTACLE` PRIMARY KEY (
	`id`,
	`user_id`
);

ALTER TABLE `User` ADD CONSTRAINT `PK_USER` PRIMARY KEY (
	`id`
);

ALTER TABLE `Socialuserinfo` ADD CONSTRAINT `PK_SOCIALUSERINFO` PRIMARY KEY (
	`id`,
	`user_id`
);

ALTER TABLE `Obstacle` ADD CONSTRAINT `FK_User_TO_Obstacle_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `User` (
	`id`
);

ALTER TABLE `Socialuserinfo` ADD CONSTRAINT `FK_User_TO_Socialuserinfo_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `User` (
	`id`
);

