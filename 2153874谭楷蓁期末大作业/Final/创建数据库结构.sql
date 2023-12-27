/*
SQLyog  v12.2.6 (64 bit)
MySQL - 8.0.35 : Database - middle
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`middle` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `middle`;

/*Table structure for table `favorites` */

DROP TABLE IF EXISTS `favorites`;

CREATE TABLE `favorites` (
  `favoriteID` int NOT NULL AUTO_INCREMENT,
  `userID` int DEFAULT NULL,
  `questionID` int DEFAULT NULL,
  PRIMARY KEY (`favoriteID`),
  KEY `userID` (`userID`),
  KEY `questionID` (`questionID`),
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`userID`) REFERENCES `users` (`userID`),
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`questionID`) REFERENCES `questions` (`questionID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `question_choice` */

DROP TABLE IF EXISTS `question_choice`;

CREATE TABLE `question_choice` (
  `ChoiceID` int NOT NULL AUTO_INCREMENT,
  `QuestionID` int DEFAULT NULL,
  `ChoiceLabel` char(1) DEFAULT NULL,
  `ChoiceContent` text,
  PRIMARY KEY (`ChoiceID`),
  KEY `QuestionID` (`QuestionID`),
  CONSTRAINT `question_choice_ibfk_1` FOREIGN KEY (`QuestionID`) REFERENCES `questions` (`questionID`)
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `questionmedia` */

DROP TABLE IF EXISTS `questionmedia`;

CREATE TABLE `questionmedia` (
  `MediaID` int NOT NULL AUTO_INCREMENT,
  `QuestionID` int DEFAULT NULL,
  `MediaType` varchar(50) DEFAULT NULL,
  `MediaPath` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MediaID`),
  KEY `QuestionID` (`QuestionID`),
  CONSTRAINT `questionmedia_ibfk_1` FOREIGN KEY (`QuestionID`) REFERENCES `questions` (`questionID`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `questions` */

DROP TABLE IF EXISTS `questions`;

CREATE TABLE `questions` (
  `questionID` int NOT NULL AUTO_INCREMENT,
  `subject` varchar(50) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `difficulty` varchar(50) DEFAULT NULL,
  `content` text,
  `answer` text,
  `UserID` int DEFAULT NULL,
  PRIMARY KEY (`questionID`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `users` (`userID`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `user_questions` */

DROP TABLE IF EXISTS `user_questions`;

CREATE TABLE `user_questions` (
  `userquestionID` int NOT NULL AUTO_INCREMENT,
  `userID` int DEFAULT NULL,
  `questionID` int DEFAULT NULL,
  PRIMARY KEY (`userquestionID`),
  KEY `userID` (`userID`),
  KEY `questionID` (`questionID`),
  CONSTRAINT `user_questions_ibfk_1` FOREIGN KEY (`userID`) REFERENCES `users` (`userID`),
  CONSTRAINT `user_questions_ibfk_2` FOREIGN KEY (`questionID`) REFERENCES `questions` (`questionID`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `userID` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `information` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
