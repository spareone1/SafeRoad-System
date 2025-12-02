SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `User` (
  id BIGINT NOT NULL AUTO_INCREMENT,
  userid VARCHAR(255) NOT NULL,
  pw VARCHAR(255) NOT NULL,
  name VARCHAR(255) NULL,
  email VARCHAR(255) NULL,
  emailpermission TINYINT(1) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  userstatus ENUM('deleted','normal_user','admin_user') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_userid (userid),
  UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ObstacleGroup` (
  id BIGINT NOT NULL AUTO_INCREMENT,
  location VARCHAR(255) NOT NULL,
  state ENUM('processing','completed','non_processed') NOT NULL DEFAULT 'non_processed',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `Obstacle` (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  obstaclegroup_id BIGINT NOT NULL,
  obstacleName VARCHAR(255) NULL,
  lat FLOAT NOT NULL,
  lon FLOAT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  imageloc VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_obstacle_user (user_id),
  KEY idx_obstacle_group (obstaclegroup_id),
  CONSTRAINT fk_obstacle_user
    FOREIGN KEY (user_id) REFERENCES `User`(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_obstacle_group
    FOREIGN KEY (obstaclegroup_id) REFERENCES `ObstacleGroup`(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `Socialuserinfo` (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  provider VARCHAR(255) NOT NULL,
  puserid VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_social_user (user_id),
  CONSTRAINT fk_social_user
    FOREIGN KEY (user_id) REFERENCES `User`(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE KEY uk_social_provider_puserid (provider, puserid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;