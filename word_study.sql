-- MySQL dump 10.13  Distrib 8.4.4, for Win64 (x86_64)
--
-- Host: localhost    Database: word_study
-- ------------------------------------------------------
-- Server version	8.4.4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `word_study`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `word_study` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `word_study`;

--
-- Table structure for table `t_administrator`
--

DROP TABLE IF EXISTS `t_administrator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_administrator` (
  `userId` varchar(50) NOT NULL COMMENT '用户ID（主键，管理员与普通用户共用）',
  `loginPassword` varchar(50) NOT NULL COMMENT '登录密码',
  `accountStatus` varchar(20) NOT NULL COMMENT '账号状态（正常、冻结）',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`userId`),
  KEY `idx_loginPassword` (`loginPassword`),
  KEY `idx_accountStatus` (`accountStatus`),
  KEY `idx_createTime` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_administrator`
--

LOCK TABLES `t_administrator` WRITE;
/*!40000 ALTER TABLE `t_administrator` DISABLE KEYS */;
INSERT INTO `t_administrator` VALUES ('admin001','admin_pwd_001','正常','2024-12-01 09:00:00'),('admin002','admin_pwd_002','正常','2024-12-10 14:30:00'),('admin003','admin_pwd_003','冻结','2025-01-15 10:20:00');
/*!40000 ALTER TABLE `t_administrator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_collection`
--

DROP TABLE IF EXISTS `t_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_collection` (
  `collectionId` varchar(50) NOT NULL COMMENT '收藏ID（主键）',
  `userId` varchar(50) NOT NULL COMMENT '用户ID（关联 OrdinaryUser 表的 userId）',
  `wordId` varchar(50) NOT NULL COMMENT '单词ID（关联 Word 表的 wordId）',
  `collectionTime` datetime NOT NULL COMMENT '收藏时间',
  PRIMARY KEY (`collectionId`),
  KEY `idx_userId` (`userId`),
  KEY `idx_wordId` (`wordId`),
  KEY `idx_collectionTime` (`collectionTime`),
  CONSTRAINT `t_collection_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `t_ordinary_user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `t_collection_ibfk_2` FOREIGN KEY (`wordId`) REFERENCES `t_word` (`wordId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_collection`
--

LOCK TABLES `t_collection` WRITE;
/*!40000 ALTER TABLE `t_collection` DISABLE KEYS */;
INSERT INTO `t_collection` VALUES ('0002','10001','WD0003','2025-02-05 14:20:00'),('0003','10002','WD0005','2025-02-07 09:45:00'),('0004','10003','WD0002','2025-02-08 16:30:00'),('0005','10001','WD0002','2025-02-15 11:00:00'),('0006','10001','WD0005','2025-02-16 09:30:00'),('0007','10004','WD0001','2025-02-18 14:20:00'),('0008','10004','WD0010','2025-02-19 10:15:00'),('25e5a46b-1c7c-494d-8','10001','WD0004','2026-05-18 15:45:43'),('2b492503-83fc-4367-8','10001','WD0006','2026-05-18 15:46:26'),('48f37979-9ab2-4089-8','10001','WD0004','2026-05-12 17:00:03'),('6d001af1-a7a4-4e54-8','10001','WD0004','2026-05-18 15:45:44'),('735c0cbc-1fe9-4322-8','10001','WD0007','2026-05-18 16:02:17'),('bb07651d-f383-44ef-9','10001','WD0002','2026-05-18 15:16:36'),('d7296c54-2a5b-47d9-a','10001','WD0004','2026-05-18 15:45:45'),('de45e430-2ac1-4c36-9','10001','WD0013','2026-05-18 20:07:13'),('e87e8b2e-1f41-4de1-9','10001','WD0007','2026-05-18 15:52:01');
/*!40000 ALTER TABLE `t_collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ordinary_user`
--

DROP TABLE IF EXISTS `t_ordinary_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ordinary_user` (
  `userId` varchar(50) NOT NULL COMMENT '用户ID（主键，关联 User 表的 userId）',
  `loginPassword` varchar(50) NOT NULL COMMENT '登录密码',
  `userName` varchar(50) NOT NULL COMMENT '用户昵称',
  `phoneNumber` varchar(11) NOT NULL COMMENT '联系电话',
  `email` varchar(100) NOT NULL COMMENT '邮箱地址',
  `accountStatus` varchar(20) NOT NULL COMMENT '账号状态（正常、冻结）',
  `registerTime` datetime NOT NULL COMMENT '注册时间',
  `lastLoginTime` datetime NOT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`userId`),
  KEY `idx_phoneNumber` (`phoneNumber`),
  KEY `idx_email` (`email`),
  KEY `idx_accountStatus` (`accountStatus`),
  KEY `idx_registerTime` (`registerTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ordinary_user`
--

LOCK TABLES `t_ordinary_user` WRITE;
/*!40000 ALTER TABLE `t_ordinary_user` DISABLE KEYS */;
INSERT INTO `t_ordinary_user` VALUES ('10001','pwd_123456','陈小明','13800138001','chenxm@example.com','正常','2025-01-01 10:00:00','2025-02-20 08:30:00'),('10002','pwd_abcdef','李芳芳','13800138002','lifang@example.com','正常','2025-01-05 11:15:00','2025-02-19 21:45:00'),('10003','pwd_xyz789','王大力','13800138003','wangdl@example.com','冻结','2025-01-10 09:30:00','2025-02-10 12:00:00'),('10004','pwd_123','赵小刚','13800138004','zhaoxg@example.com','正常','2025-02-01 10:00:00','2025-02-20 18:00:00'),('10005','pwd_456','孙丽丽','13800138005','sunll@example.com','正常','2025-02-10 09:30:00','2025-02-19 20:00:00');
/*!40000 ALTER TABLE `t_ordinary_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_word`
--

DROP TABLE IF EXISTS `t_word`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_word` (
  `wordId` varchar(50) NOT NULL COMMENT '单词ID（主键）',
  `wordBookId` varchar(50) NOT NULL COMMENT '所属词书ID（关联 WordBook 表的 wordBookId）',
  `englishSpelling` varchar(50) NOT NULL COMMENT '英文拼写',
  `chineseDefinition` text NOT NULL COMMENT '中文释义',
  `exampleSentence` text COMMENT '例句',
  `phoneticSymbol` varchar(50) NOT NULL COMMENT '音标',
  `WordPronunciation` varchar(255) NOT NULL COMMENT '音频链接（字段名与类属性一致，首字母大写）',
  `wordImage` varchar(255) DEFAULT NULL COMMENT '示意图',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`wordId`),
  KEY `idx_wordBookId` (`wordBookId`),
  KEY `idx_englishSpelling` (`englishSpelling`),
  KEY `idx_chineseDefinition` (`chineseDefinition`(100)),
  KEY `idx_phoneticSymbol` (`phoneticSymbol`),
  KEY `idx_WordPronunciation` (`WordPronunciation`),
  KEY `idx_wordImage` (`wordImage`),
  KEY `idx_createTime` (`createTime`),
  CONSTRAINT `t_word_ibfk_1` FOREIGN KEY (`wordBookId`) REFERENCES `t_word_book` (`wordBookId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单词表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_word`
--

LOCK TABLES `t_word` WRITE;
/*!40000 ALTER TABLE `t_word` DISABLE KEYS */;
INSERT INTO `t_word` VALUES ('WD0001','WB001','abandon','抛弃，放弃','He abandoned his car in the snow.','/əˈbændən/','http://dict.youdao.com/dictvoice?type=1&audio=abandon','/images/words/abandon.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0002','WB001','ability','能力，才能','She has the ability to learn fast.','/əˈbɪləti/','http://dict.youdao.com/dictvoice?type=1&audio=ability','/images/words/ability.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0003','WB002','consequence','后果，结果','You must face the consequence.','/ˈkɑːnsɪkwens/','http://dict.youdao.com/dictvoice?type=1&audio=consequence','/images/words/consequence.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0004','WB004','analyze','分析','We need to analyze the data.','/ˈænəlaɪz/','http://dict.youdao.com/dictvoice?type=1&audio=analyze','/images/words/analyze.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0005','WB002','diligent','勤奋的','He is a diligent student.','/ˈdɪlɪdʒənt/','http://dict.youdao.com/dictvoice?type=1&audio=diligent','/images/words/diligent.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0006','WB001','broad','宽阔的','The river is very broad.','/brɔːd/','http://dict.youdao.com/dictvoice?type=1&audio=broad','/images/words//broad.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0007','WB001','calculate','计算','Calculate the result.','/ˈkælkjuleɪt/','http://dict.youdao.com/dictvoice?type=1&audio=calculate','/images/words/calculate.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0008','WB001','dangerous','危险的','It is dangerous to swim here.','/ˈdeɪndʒərəs/','http://dict.youdao.com/dictvoice?type=1&audio=dangerous','/images/words/dangerous.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0009','WB001','effective','有效的','This method is effective.','/ɪˈfektɪv/','http://dict.youdao.com/dictvoice?type=1&audio=effective','/images/words/effective.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0010','WB004','constitute','构成','Twelve months constitute a year.','/ˈkɑːnstɪtuːt/','http://dict.youdao.com/dictvoice?type=1&audio=constitute','/images/words/constitute.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0011','WB004','derive','源自','The word derives from Latin.','/dɪˈraɪv/','http://dict.youdao.com/dictvoice?type=1&audio=derive','/images/words/derive.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0012','WB004','evaluate','评估','We need to evaluate the risk.','/ɪˈvæljueɪt/','http://dict.youdao.com/dictvoice?type=1&audio=evaluate','/images/words/evaluate.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0013','WB005','abandon','抛弃','He abandoned the project.','/əˈbændən/','http://dict.youdao.com/dictvoice?type=1&audio=abandon','/images/words/abandon.png','2026-05-10 21:12:40','2026-05-10 21:12:40'),('WD0014','WB005','brilliant','辉煌的','A brilliant idea.','/ˈbrɪliənt/','http://dict.youdao.com/dictvoice?type=1&audio=brilliant','/images/words/brilliant.png','2026-05-10 21:12:40','2026-05-10 21:12:40');
/*!40000 ALTER TABLE `t_word` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_word_insert` AFTER INSERT ON `t_word` FOR EACH ROW BEGIN
    UPDATE t_word_book
    SET wordCount = wordCount + 1,
        updateTime = NOW()
    WHERE wordBookId = NEW.wordBookId;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_word_delete` AFTER DELETE ON `t_word` FOR EACH ROW BEGIN
    UPDATE t_word_book
    SET wordCount = wordCount - 1,
        updateTime = NOW()
    WHERE wordBookId = OLD.wordBookId;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `t_word_book`
--

DROP TABLE IF EXISTS `t_word_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_word_book` (
  `wordBookId` varchar(50) NOT NULL COMMENT '词书ID（主键）',
  `wordBookName` varchar(100) NOT NULL COMMENT '词书名称',
  `difficultyLevel` varchar(20) NOT NULL COMMENT '词书难度等级（简单、中等、困难）',
  `wordBookDescription` text COMMENT '词书描述',
  `wordCount` int NOT NULL COMMENT '单词数量',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` datetime NOT NULL COMMENT '更新时间',
  `wordBookStatus` varchar(20) NOT NULL COMMENT '词书状态（未上线、已上线）',
  PRIMARY KEY (`wordBookId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='词书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_word_book`
--

LOCK TABLES `t_word_book` WRITE;
/*!40000 ALTER TABLE `t_word_book` DISABLE KEYS */;
INSERT INTO `t_word_book` VALUES ('WB001','四级高频词','中级','大学英语四级考试常考词汇',1200,'2026-05-10 21:12:40','2026-05-10 21:12:40','已上线'),('WB002','雅思真经','高级','雅思真题核心词汇',1800,'2026-05-10 21:12:40','2026-05-10 21:12:40','已上线'),('WB003','儿童启蒙','初级','3-8岁幼儿英语基础单词',300,'2026-05-10 21:12:40','2026-05-10 21:12:40','未上线'),('WB004','考研核心','高级','考研英语大纲5500词精选',2200,'2026-05-10 21:12:40','2026-05-10 21:12:40','已上线'),('WB005','托福高分词汇','高级','托福备考核心词汇',800,'2026-05-10 21:12:40','2026-05-10 21:12:40','未上线');
/*!40000 ALTER TABLE `t_word_book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_word_learning_record`
--

DROP TABLE IF EXISTS `t_word_learning_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_word_learning_record` (
  `recordId` varchar(50) NOT NULL COMMENT '记录ID（主键）',
  `userId` varchar(50) NOT NULL COMMENT '用户ID（关联 OrdinaryUser 表的 userId）',
  `learnedWordBookId` varchar(50) NOT NULL COMMENT '学习词书ID（关联 WordBook 表的 wordBookId）',
  `wordId` varchar(50) NOT NULL COMMENT '单词ID（关联 Word 表的 worldId）',
  `learningDate` date NOT NULL COMMENT '学习日期（仅日期类型，与类属性一致）',
  `recordCreateTime` datetime NOT NULL COMMENT '记录创建时间',
  PRIMARY KEY (`recordId`),
  KEY `idx_userId` (`userId`),
  KEY `idx_learnedWordBookId` (`learnedWordBookId`),
  KEY `idx_wordId` (`wordId`),
  KEY `idx_learningDate` (`learningDate`),
  KEY `idx_recordCreateTime` (`recordCreateTime`),
  CONSTRAINT `t_word_learning_record_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `t_ordinary_user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `t_word_learning_record_ibfk_2` FOREIGN KEY (`learnedWordBookId`) REFERENCES `t_word_book` (`wordBookId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `t_word_learning_record_ibfk_3` FOREIGN KEY (`wordId`) REFERENCES `t_word` (`wordId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单词学习记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_word_learning_record`
--

LOCK TABLES `t_word_learning_record` WRITE;
/*!40000 ALTER TABLE `t_word_learning_record` DISABLE KEYS */;
INSERT INTO `t_word_learning_record` VALUES ('27d9a826-eabe-4b5b-a','10001','WB001','WD0001','2026-05-18','2026-05-18 12:23:52'),('859761b7-f213-41bd-8','10001','WB001','WD0001','2026-05-18','2026-05-18 15:46:23'),('REC001','10001','WB001','WD0001','2025-02-10','2026-05-10 21:12:40'),('REC002','10001','WB001','WD0002','2025-02-10','2026-05-10 21:12:40'),('REC003','10002','WB002','WD0003','2025-02-12','2026-05-10 21:12:40'),('REC004','10002','WB002','WD0005','2025-02-13','2026-05-10 21:12:40'),('REC005','10003','WB004','WD0004','2025-02-09','2026-05-10 21:12:40'),('REC006','10001','WB002','WD0003','2025-02-11','2026-05-10 21:12:40'),('REC007','10001','WB001','WD0006','2025-02-15','2026-05-10 21:12:40'),('REC008','10001','WB001','WD0007','2025-02-15','2026-05-10 21:12:40'),('REC009','10001','WB001','WD0008','2025-02-15','2026-05-10 21:12:40'),('REC010','10001','WB001','WD0009','2025-02-16','2026-05-10 21:12:40'),('REC011','10001','WB001','WD0001','2025-02-15','2026-05-10 21:12:40'),('REC012','10001','WB001','WD0002','2025-02-16','2026-05-10 21:12:40'),('REC013','10001','WB002','WD0005','2025-02-15','2026-05-10 21:12:40'),('REC014','10001','WB004','WD0004','2025-02-17','2026-05-10 21:12:40'),('REC015','10001','WB004','WD0010','2025-02-18','2026-05-10 21:12:40'),('REC016','10001','WB004','WD0011','2025-02-18','2026-05-10 21:12:40'),('REC017','10002','WB002','WD0001','2025-02-14','2026-05-10 21:12:40'),('REC018','10002','WB002','WD0002','2025-02-14','2026-05-10 21:12:40'),('REC019','10002','WB004','WD0004','2025-02-15','2026-05-10 21:12:40'),('REC020','10002','WB004','WD0011','2025-02-15','2026-05-10 21:12:40'),('REC021','10003','WB001','WD0003','2025-02-10','2026-05-10 21:12:40'),('REC022','10003','WB002','WD0004','2025-02-11','2026-05-10 21:12:40'),('REC023','10004','WB001','WD0001','2025-02-18','2026-05-10 21:12:40'),('REC024','10004','WB001','WD0002','2025-02-18','2026-05-10 21:12:40'),('REC025','10004','WB002','WD0003','2025-02-18','2026-05-10 21:12:40'),('REC026','10004','WB004','WD0012','2025-02-19','2026-05-10 21:12:40'),('REC027','10005','WB001','WD0001','2025-02-20','2026-05-10 21:12:40'),('REC028','10005','WB001','WD0002','2025-02-20','2026-05-10 21:12:40'),('REC029','10005','WB001','WD0006','2025-02-20','2026-05-10 21:12:40'),('REC030','10005','WB002','WD0003','2025-02-20','2026-05-10 21:12:40'),('REC031','10005','WB002','WD0005','2025-02-20','2026-05-10 21:12:40'),('REC032','10001','WB001','WD0001','2024-12-31','2026-05-10 21:12:40'),('REC033','10001','WB001','WD0002','2025-01-01','2026-05-10 21:12:40');
/*!40000 ALTER TABLE `t_word_learning_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_admin_daily_stats`
--

DROP TABLE IF EXISTS `v_admin_daily_stats`;
/*!50001 DROP VIEW IF EXISTS `v_admin_daily_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_admin_daily_stats` AS SELECT 
 1 AS `learningDate`,
 1 AS `activeUserCount`,
 1 AS `totalUniqueWordsLearned`,
 1 AS `avgWordsPerUser`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_collection_detail`
--

DROP TABLE IF EXISTS `v_user_collection_detail`;
/*!50001 DROP VIEW IF EXISTS `v_user_collection_detail`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_collection_detail` AS SELECT 
 1 AS `collectionId`,
 1 AS `userId`,
 1 AS `wordId`,
 1 AS `collectionTime`,
 1 AS `englishSpelling`,
 1 AS `chineseDefinition`,
 1 AS `phoneticSymbol`,
 1 AS `exampleSentence`,
 1 AS `WordPronunciation`,
 1 AS `wordImage`,
 1 AS `wordBookId`,
 1 AS `wordBookName`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_daily_learning`
--

DROP TABLE IF EXISTS `v_user_daily_learning`;
/*!50001 DROP VIEW IF EXISTS `v_user_daily_learning`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_daily_learning` AS SELECT 
 1 AS `userId`,
 1 AS `learningDate`,
 1 AS `dailyWordCount`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_overall_stats`
--

DROP TABLE IF EXISTS `v_user_overall_stats`;
/*!50001 DROP VIEW IF EXISTS `v_user_overall_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_overall_stats` AS SELECT 
 1 AS `userId`,
 1 AS `learningDays`,
 1 AS `totalLearnedWords`,
 1 AS `avgDailyWords`,
 1 AS `totalLearningEvents`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_wordbook_progress`
--

DROP TABLE IF EXISTS `v_user_wordbook_progress`;
/*!50001 DROP VIEW IF EXISTS `v_user_wordbook_progress`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_wordbook_progress` AS SELECT 
 1 AS `userId`,
 1 AS `wordBookId`,
 1 AS `wordBookName`,
 1 AS `difficultyLevel`,
 1 AS `totalWordCount`,
 1 AS `learnedWordCount`,
 1 AS `progressPercent`,
 1 AS `firstLearningDate`,
 1 AS `lastUpdateTime`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_wordbook_stats`
--

DROP TABLE IF EXISTS `v_user_wordbook_stats`;
/*!50001 DROP VIEW IF EXISTS `v_user_wordbook_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_wordbook_stats` AS SELECT 
 1 AS `userId`,
 1 AS `wordBookId`,
 1 AS `learnedWordCount`,
 1 AS `firstLearningDate`,
 1 AS `lastLearningDate`*/;
SET character_set_client = @saved_cs_client;

--
-- Current Database: `word_study`
--

USE `word_study`;

--
-- Final view structure for view `v_admin_daily_stats`
--

/*!50001 DROP VIEW IF EXISTS `v_admin_daily_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_admin_daily_stats` AS select `t_word_learning_record`.`learningDate` AS `learningDate`,count(distinct `t_word_learning_record`.`userId`) AS `activeUserCount`,count(distinct `t_word_learning_record`.`wordId`) AS `totalUniqueWordsLearned`,round(((count(distinct `t_word_learning_record`.`wordId`) * 1.0) / count(distinct `t_word_learning_record`.`userId`)),2) AS `avgWordsPerUser` from `t_word_learning_record` group by `t_word_learning_record`.`learningDate` order by `t_word_learning_record`.`learningDate` desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_collection_detail`
--

/*!50001 DROP VIEW IF EXISTS `v_user_collection_detail`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_collection_detail` AS select `c`.`collectionId` AS `collectionId`,`c`.`userId` AS `userId`,`c`.`wordId` AS `wordId`,`c`.`collectionTime` AS `collectionTime`,`w`.`englishSpelling` AS `englishSpelling`,`w`.`chineseDefinition` AS `chineseDefinition`,`w`.`phoneticSymbol` AS `phoneticSymbol`,`w`.`exampleSentence` AS `exampleSentence`,`w`.`WordPronunciation` AS `WordPronunciation`,`w`.`wordImage` AS `wordImage`,`wb`.`wordBookId` AS `wordBookId`,`wb`.`wordBookName` AS `wordBookName` from ((`t_collection` `c` join `t_word` `w` on((`w`.`wordId` = `c`.`wordId`))) join `t_word_book` `wb` on((`wb`.`wordBookId` = `w`.`wordBookId`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_daily_learning`
--

/*!50001 DROP VIEW IF EXISTS `v_user_daily_learning`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_daily_learning` AS select `t_word_learning_record`.`userId` AS `userId`,`t_word_learning_record`.`learningDate` AS `learningDate`,count(distinct `t_word_learning_record`.`wordId`) AS `dailyWordCount` from `t_word_learning_record` group by `t_word_learning_record`.`userId`,`t_word_learning_record`.`learningDate` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_overall_stats`
--

/*!50001 DROP VIEW IF EXISTS `v_user_overall_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_overall_stats` AS select `t_word_learning_record`.`userId` AS `userId`,count(distinct `t_word_learning_record`.`learningDate`) AS `learningDays`,count(distinct `t_word_learning_record`.`wordId`) AS `totalLearnedWords`,round((count(distinct `t_word_learning_record`.`wordId`) / count(distinct `t_word_learning_record`.`learningDate`)),2) AS `avgDailyWords`,count(0) AS `totalLearningEvents` from `t_word_learning_record` group by `t_word_learning_record`.`userId` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_wordbook_progress`
--

/*!50001 DROP VIEW IF EXISTS `v_user_wordbook_progress`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_wordbook_progress` AS select `lr`.`userId` AS `userId`,`lr`.`learnedWordBookId` AS `wordBookId`,`wb`.`wordBookName` AS `wordBookName`,`wb`.`difficultyLevel` AS `difficultyLevel`,`wb`.`wordCount` AS `totalWordCount`,count(distinct `lr`.`wordId`) AS `learnedWordCount`,round(((count(distinct `lr`.`wordId`) * 100.0) / `wb`.`wordCount`),2) AS `progressPercent`,min(`lr`.`learningDate`) AS `firstLearningDate`,max(`lr`.`recordCreateTime`) AS `lastUpdateTime` from (`t_word_learning_record` `lr` join `t_word_book` `wb` on((`wb`.`wordBookId` = `lr`.`learnedWordBookId`))) where (`wb`.`wordBookStatus` = '已上线') group by `lr`.`userId`,`lr`.`learnedWordBookId`,`wb`.`wordBookName`,`wb`.`difficultyLevel`,`wb`.`wordCount` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_wordbook_stats`
--

/*!50001 DROP VIEW IF EXISTS `v_user_wordbook_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_wordbook_stats` AS select `t_word_learning_record`.`userId` AS `userId`,`t_word_learning_record`.`learnedWordBookId` AS `wordBookId`,count(distinct `t_word_learning_record`.`wordId`) AS `learnedWordCount`,min(`t_word_learning_record`.`learningDate`) AS `firstLearningDate`,max(`t_word_learning_record`.`learningDate`) AS `lastLearningDate` from `t_word_learning_record` group by `t_word_learning_record`.`userId`,`t_word_learning_record`.`learnedWordBookId` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-19  9:35:11
