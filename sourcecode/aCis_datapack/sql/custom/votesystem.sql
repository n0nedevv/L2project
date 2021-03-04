SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for globalvotes
-- ----------------------------
DROP TABLE IF EXISTS `globalvotes`;
CREATE TABLE `globalvotes`  (
  `voteSite` tinyint(2) NOT NULL,
  `lastRewardVotes` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`voteSite`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of globalvotes
-- ----------------------------
INSERT INTO `globalvotes` VALUES (0, 13);
INSERT INTO `globalvotes` VALUES (1, 68);
INSERT INTO `globalvotes` VALUES (2, 0);
INSERT INTO `globalvotes` VALUES (3, 3);
INSERT INTO `globalvotes` VALUES (4, 2);
INSERT INTO `globalvotes` VALUES (5, 0);
INSERT INTO `globalvotes` VALUES (6, 0);
INSERT INTO `globalvotes` VALUES (7, 2);
INSERT INTO `globalvotes` VALUES (8, 3);
INSERT INTO `globalvotes` VALUES (9, 0);
INSERT INTO `globalvotes` VALUES (10, 75);

-- ----------------------------
-- Table structure for individualvotes
-- ----------------------------
DROP TABLE IF EXISTS `individualvotes`;
CREATE TABLE `individualvotes`  (
  `voterIp` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `voteSite` tinyint(3) NOT NULL,
  `diffTime` bigint(20) NULL DEFAULT NULL,
  `votingTimeSite` bigint(20) NULL DEFAULT NULL,
  `alreadyRewarded` tinyint(3) NULL DEFAULT NULL,
  PRIMARY KEY (`voterIp`, `voteSite`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of individualvotes
-- ----------------------------
INSERT INTO `individualvotes` VALUES ('127.0.0.1', 0, 1588703279833, 1588702011000, 1);
INSERT INTO `individualvotes` VALUES ('127.0.0.1', 3, 1588704503248, 1588703396000, 1);
INSERT INTO `individualvotes` VALUES ('127.0.0.1', 5, 1588692835993, 1588692823000, 1);

SET FOREIGN_KEY_CHECKS = 1;