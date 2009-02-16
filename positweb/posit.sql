/*
SQLyog Enterprise - MySQL GUI v7.02 
MySQL - 5.0.67 : Database - stdtrack
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/`stdtrack` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;

USE `stdtrack`;

/*Table structure for table `mast_district` */

CREATE TABLE `mast_district` (
  `dist_code` varchar(30) NOT NULL default '',
  `dist_name` varchar(20) default NULL,
  PRIMARY KEY  (`dist_code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `mast_district` */

insert  into `mast_district`(`dist_code`,`dist_name`) values ('41','MANANG');

/*Table structure for table `mast_school_type` */

CREATE TABLE `mast_school_type` (
  `sch_num` varchar(30) default NULL,
  `sch_year` varchar(4) default NULL,
  `ecd` int(11) default NULL,
  `class1` int(11) default NULL,
  `class2` int(11) default NULL,
  `class3` int(11) default NULL,
  `class4` int(11) default NULL,
  `class5` int(11) default NULL,
  `class6` int(11) default NULL,
  `class7` int(11) default NULL,
  `class8` int(11) default NULL,
  `class9` int(11) default NULL,
  `class10` int(11) default NULL,
  `class11` int(11) default NULL,
  `class12` int(11) default NULL,
  `flash` int(11) default NULL,
  `entry_timestamp` int(11) default NULL,
  KEY `sch_num` (`sch_num`),
  KEY `sch_year` (`sch_year`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Section A4 - Type of School and grades offered';

/*Data for the table `mast_school_type` */

insert  into `mast_school_type`(`sch_num`,`sch_year`,`ecd`,`class1`,`class2`,`class3`,`class4`,`class5`,`class6`,`class7`,`class8`,`class9`,`class10`,`class11`,`class12`,`flash`,`entry_timestamp`) values ('410010001','2064',1,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410010002','2064',6,6,6,6,0,0,0,0,0,0,0,0,0,2,2003),('410020001','2064',1,1,1,1,1,0,0,0,0,0,0,0,0,2,2003),('410020002','2064',0,1,1,1,1,0,0,0,0,0,0,0,0,2,2003),('410020004','2064',1,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410020005','2064',1,1,1,1,1,1,1,1,1,0,0,0,0,2,2003),('410020007','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410020003','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410020006','2064',0,1,1,1,1,1,1,1,1,1,1,3,3,2,2003),('410030001','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410030002','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410030003','2064',1,2,2,2,2,2,0,0,0,0,0,0,0,2,2003),('410030004','2064',1,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410030005','2064',1,1,1,1,1,1,1,1,0,0,0,0,0,2,2003),('410030006','2064',1,1,1,1,1,1,1,1,1,1,1,0,0,2,2003),('410040001','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410050001','2064',1,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410060001','2064',0,1,1,1,1,1,1,1,0,0,0,0,0,2,2003),('410070002','2064',1,1,1,1,1,1,1,1,1,1,1,0,0,2,2003),('410070001','2064',0,1,1,1,1,1,1,1,0,0,0,0,0,2,2003),('410080001','2064',0,1,1,1,1,1,1,1,0,0,0,0,0,2,2003),('410090001','2064',0,1,1,1,1,1,1,1,0,0,0,0,0,2,2003),('410090002','2064',0,1,1,1,0,0,0,0,0,0,0,0,0,2,2003),('410100001','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410100002','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410110001','2064',1,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410110002','2064',1,2,2,2,2,2,1,1,0,0,0,0,0,2,2003),('410110003','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410120001','2064',0,1,1,1,1,1,0,0,0,0,0,0,0,2,2003),('410130001','2064',1,2,2,2,2,2,1,1,0,0,0,0,0,2,2003),('410130002','2064',0,1,1,1,1,1,1,1,1,1,1,4,4,2,2003),('410010001','2065',1,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410100001','2065',0,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410070001','2065',0,1,1,1,1,1,1,1,0,0,0,0,0,1,2003),('410110001','2065',1,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410030001','2065',0,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410030003','2065',1,2,2,2,2,2,0,0,0,0,0,0,0,1,2003),('410020007','2065',0,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410020005','2065',1,1,1,1,1,1,1,1,1,0,0,0,0,1,2003),('410020006','2065',0,1,1,1,1,1,1,1,1,1,1,3,3,1,2003),('410020004','2065',1,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410020002','2065',0,1,1,1,1,1,0,0,0,0,0,0,0,1,2003),('410020001','2065',1,1,1,1,1,0,0,0,0,0,0,0,0,1,2003);

/*Table structure for table `mast_schoollist` */

CREATE TABLE `mast_schoollist` (
  `dist_code` varchar(50) default NULL,
  `vdc_code` varchar(100) default NULL,
  `sch_code` varchar(100) default NULL,
  `sch_year` varchar(4) default NULL,
  `nm_sch` varchar(100) default NULL,
  `wardno` varchar(100) default NULL,
  `location` varchar(100) default NULL,
  `post_office` varchar(50) default NULL,
  `telno` varchar(50) default NULL,
  `email` varchar(50) default NULL,
  `region` varchar(50) default NULL,
  `sch_num` varchar(100) default NULL,
  `flash` int(11) default NULL,
  `tmis` int(11) default NULL,
  `entry_timestamp` int(11) default NULL,
  KEY `sch_num` (`sch_num`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='MyISAM free: 10240 kB';

/*Data for the table `mast_schoollist` */

insert  into `mast_schoollist`(`dist_code`,`vdc_code`,`sch_code`,`sch_year`,`nm_sch`,`wardno`,`location`,`post_office`,`telno`,`email`,`region`,`sch_num`,`flash`,`tmis`,`entry_timestamp`) values ('41','001','0001','2064','Bhraka Pra V','3','Bhraka',NULL,NULL,NULL,'1','410010001',2,NULL,2003),('41','001','0002','2064','Lofeling Ebs','1','Farkyu',NULL,NULL,NULL,'1','410010002',2,NULL,2003),('41','002','0001','2064','Balmandir Pra V','5','Chame',NULL,'066440234',NULL,'1','410020001',2,NULL,2003),('41','002','0002','2064','Gaurishankar Pra V','1','Timang',NULL,NULL,NULL,'1','410020002',2,NULL,2003),('41','002','0004','2064','Bishwashanti Pra V','5','Koto',NULL,NULL,NULL,'1','410020004',2,NULL,2003),('41','002','0005','2064','Deendayalu Ni Ma V','4','Thanchok',NULL,'066440215',NULL,'1','410020005',2,NULL,2003),('41','002','0007','2064','Laligurans Pra V','9','Talekhu',NULL,NULL,NULL,'1','410020007',2,NULL,2003),('41','002','0003','2064','Tatopani Pra V','3','Latamanang',NULL,NULL,NULL,'1','410020003',2,NULL,2003),('41','002','0006','2064','Lokpriya Uchha Ma V','5','Chame',NULL,'066440134',NULL,'1','410020006',2,NULL,2003),('41','003','0001','2064','Marsyangdi Pra V','5','Khotro',NULL,NULL,NULL,'1','410030001',2,NULL,2003),('41','003','0002','2064','Amar Pra V','1','Gherang',NULL,NULL,NULL,'1','410030002',2,NULL,2003),('41','003','0003','2064','Dharapani Pra V','5','Dharapani',NULL,NULL,NULL,'1','410030003',2,NULL,2003),('41','003','0004','2064','Saraswoti Pra V','8','wodar',NULL,'993665014',NULL,'1','410030004',2,NULL,2003),('41','003','0005','2064','Mansuri Ni Ma V','4','Nachai',NULL,NULL,NULL,'1','410030005',2,NULL,2003),('41','003','0006','2064','Bhanu Ma V','1','Tal',NULL,NULL,NULL,'1','410030006',2,NULL,2003),('41','004','0001','2064','Fu Pra V','2','Fu',NULL,NULL,NULL,'1','410040001',2,NULL,2003),('41','005','0001','2064','Ghyaru Pra V','2','Ghyaru',NULL,'019442002',NULL,'1','410050001',2,NULL,2003),('41','006','0001','2064','Tilicho Ni Ma V','2','Khangsar',NULL,'019442008',NULL,'1','410060001',2,NULL,2003),('41','007','0002','2064','Annapurna Ma V','8','Manang Gaun',NULL,'019442009',NULL,'1','410070002',2,NULL,2003),('41','007','0001','2064','Humde Ni Ma V','9','Humde',NULL,'019442000',NULL,'1','410070001',2,NULL,2003),('41','008','0001','2064','Nar Ni Ma V','5','Nar',NULL,'019442004',NULL,'1','410080001',2,NULL,2003),('41','009','0001','2064','Ngawal Ni Ma V','1','Ngawal',NULL,'019442005',NULL,'1','410090001',2,NULL,2003),('41','009','0002','2064','Buddha Pra V','9','Ngawal Besi',NULL,NULL,NULL,'1','410090002',2,NULL,2003),('41','010','0001','2064','Pisang Pra V','8','Pisang',NULL,NULL,NULL,'1','410100001',2,NULL,2003),('41','010','0002','2064','Janaklyan Pra V','1','Bhratang',NULL,NULL,NULL,'1','410100002',2,NULL,2003),('41','011','0001','2064','Pashupati Pra V','8','Bagarchhap',NULL,NULL,NULL,'1','410110001',2,NULL,2003),('41','011','0002','2064','Jana Bikash Ni Ma V','4','Tachai',NULL,NULL,NULL,'1','410110002',2,NULL,2003),('41','011','0003','2064','Gyanodaya Pra V','9','Danakyu',NULL,NULL,NULL,'1','410110003',2,NULL,2003),('41','012','0001','2064','Tanki Pra V','3','Tanki Manang',NULL,'993665036',NULL,'1','410120001',2,NULL,2003),('41','013','0001','2064','Himalaya Mahendra Jyoti Ni Ma V','3','Tilche',NULL,'993665008',NULL,'1','410130001',2,NULL,2003),('41','013','0002','2064','Prakash Jyoti Uchha Ma V','6','Thonche',NULL,'066449320',NULL,'1','410130002',2,NULL,2003),('41','010','0001','2065','Pisang Pra V','8','Pisang',NULL,NULL,NULL,'1','410100001',1,NULL,2003),('41','007','0001','2065','Humde Ni Ma V','9','Humde',NULL,'019442000',NULL,'1','410070001',1,NULL,2003),('41','011','0001','2065','Pashupati Pra V','8','Bagarchhap',NULL,NULL,NULL,'1','410110001',1,NULL,2003),('41','003','0001','2065','Marsyangdi Pra V','5','Khotro',NULL,NULL,NULL,'1','410030001',1,NULL,2003),('41','003','0003','2065','Dharapani Pra V','5','Dharapani',NULL,NULL,NULL,'1','410030003',1,NULL,2003),('41','002','0007','2065','Laligurans Pra V','9','Talekhu',NULL,'066440147',NULL,'1','410020007',1,NULL,2003),('41','002','0005','2065','Deendayalu Ni Ma V','4','Thanchok',NULL,'066440215',NULL,'1','410020005',1,NULL,2003),('41','002','0006','2065','Lokpriya Uchha Ma V','5','Chame',NULL,'066440134',NULL,'1','410020006',1,NULL,2003),('41','002','0004','2065','Bishwashanti Pra V','5','Koto',NULL,NULL,NULL,'1','410020004',1,NULL,2003),('41','002','0002','2065','Gaurishankar Pra V','1','Timang',NULL,NULL,NULL,'1','410020002',1,NULL,2003),('41','002','0001','2065','Balmandir Pra V','5','Chame',NULL,'066440234',NULL,'1','410020001',1,NULL,2003);

/*Table structure for table `mast_vdc` */

CREATE TABLE `mast_vdc` (
  `dist_code` varchar(5) NOT NULL default '',
  `vdc_code` varchar(5) NOT NULL default '',
  `vdc_name_e` varchar(250) default NULL,
  `uniquevdc` varchar(5) default NULL,
  KEY `dist_code` (`dist_code`),
  KEY `vdc_code` (`vdc_code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Master VDC list';

/*Data for the table `mast_vdc` */

insert  into `mast_vdc`(`dist_code`,`vdc_code`,`vdc_name_e`,`uniquevdc`) values ('41','001','Bhraka',NULL),('41','002','Chame',NULL),('41','003','Dharapani',NULL),('41','004','Fu',NULL),('41','005','Ghyaru',NULL),('41','006','Khangsar',NULL),('41','007','Manang Gaun',NULL),('41','008','Nar',NULL),('41','009','Ngawal',NULL),('41','010','Pisang',NULL),('41','011','Tachai Bagarchhap',NULL),('41','012','Tanki Manang',NULL),('41','013','Thonche',NULL);

/*Table structure for table `students_static` */

CREATE TABLE `students_static` (
  `stu_num` varchar(20) character set latin1 NOT NULL,
  `first_name` varchar(50) character set latin1 default NULL,
  `last_name` varchar(50) character set latin1 default NULL,
  `sex` int(11) default NULL,
  `year_of_birth` int(11) default NULL,
  `caste_ethnicity` varchar(50) character set latin1 default NULL,
  `mother_language` varchar(50) character set latin1 default NULL,
  `ecd_ppc_status` int(11) default NULL,
  `birth_reg` varchar(20) collate utf8_unicode_ci default NULL,
  `father_name` varchar(100) collate utf8_unicode_ci default NULL,
  `father_occ` int(11) default NULL,
  `father_edu` int(11) default NULL,
  `mother_name` varchar(100) collate utf8_unicode_ci default NULL,
  `mother_occ` int(11) default NULL,
  `mother_edu` int(11) default NULL,
  PRIMARY KEY  (`stu_num`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*Data for the table `students_static` */

insert  into `students_static`(`stu_num`,`first_name`,`last_name`,`sex`,`year_of_birth`,`caste_ethnicity`,`mother_language`,`ecd_ppc_status`,`birth_reg`,`father_name`,`father_occ`,`father_edu`,`mother_name`,`mother_occ`,`mother_edu`) values ('4165000001','Jwalanta','Shrestha',1,2042,'Newar','Nepali',1,'1234','Jiwan Shrestha',1,2,'Kalpana Shrestha',3,4),('4165000002','Ram','Sharma',1,2050,'Brahmin','Nepali',1,'567',NULL,NULL,NULL,NULL,NULL,NULL),('4165000003','Hari','Subedi',1,2050,'Brahmin','Nepali',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('4165000004','Pasang','Sherpa',2,2055,'Sherpa','Nepali',2,NULL,'Lakpa Sherpa',2,1,'Pema Sherpa',NULL,NULL),('4165000005',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `students_track` */

CREATE TABLE `students_track` (
  `stu_num` varchar(20) NOT NULL,
  `sch_num` varchar(20) NOT NULL,
  `sch_num_prev` varchar(20) default NULL,
  `sch_year` int(11) NOT NULL,
  `roll_no` int(11) default NULL,
  `class` int(11) default NULL,
  `class_prev` int(11) default NULL,
  `age` int(11) default NULL,
  `adm_year` int(11) default NULL,
  `dist_school` int(11) default NULL,
  `disability` int(11) default NULL,
  `sch_status` int(11) default NULL,
  `household_no` varchar(100) default NULL,
  `outside_home_status` int(11) default NULL,
  `outside_home_hrs` int(11) default NULL,
  `economic_status` int(11) default NULL,
  `upgraded` int(11) default NULL,
  `repetition` int(11) default NULL,
  `dropout` int(11) default NULL,
  `dropout_reason` int(11) default NULL,
  `is_current` int(11) default NULL,
  PRIMARY KEY  (`stu_num`,`sch_year`),
  KEY `sch_num` (`sch_num`),
  KEY `stu_num` (`stu_num`),
  KEY `sch_year` (`sch_year`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `students_track` */

insert  into `students_track`(`stu_num`,`sch_num`,`sch_num_prev`,`sch_year`,`roll_no`,`class`,`class_prev`,`age`,`adm_year`,`dist_school`,`disability`,`sch_status`,`household_no`,`outside_home_status`,`outside_home_hrs`,`economic_status`,`upgraded`,`repetition`,`dropout`,`dropout_reason`,`is_current`) values ('4165000001','410010001','410010001',2064,NULL,2,1,23,NULL,10,NULL,1,'11/2 Nawasudhar Galli',1,2,3,NULL,NULL,NULL,NULL,NULL),('4165000002','410010001','410010001',2064,10,1,NULL,19,NULL,5,NULL,2,'123 Maijubahal',1,1,NULL,1,NULL,NULL,NULL,NULL),('4165000003','410010001','410010001',2064,11,1,NULL,15,NULL,10,NULL,2,'1234567',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('4165000004','410010001','410010001',2064,12,1,NULL,10,NULL,20,NULL,2,NULL,2,NULL,2,NULL,NULL,NULL,NULL,NULL),('4165000002','410010001','410010001',2065,10,2,1,19,NULL,5,NULL,2,'123 Maijubahal',1,1,NULL,NULL,NULL,NULL,NULL,1);

/*Table structure for table `student_track_latest` */

DROP TABLE IF EXISTS `student_track_latest`;

/*!50001 CREATE TABLE `student_track_latest` (
  `stu_num` varchar(20) character set latin1 NOT NULL,
  `sch_num` varchar(20) character set latin1 NOT NULL,
  `sch_year` int(11) NOT NULL,
  `roll_no` int(11) default NULL,
  `class` int(11) default NULL,
  `adm_year` int(11) default NULL,
  `dist_school` int(11) default NULL,
  `disability` int(11) default NULL,
  `sch_status` int(11) default NULL,
  `outside_home_status` int(11) default NULL,
  `outside_home_hrs` int(11) default NULL,
  `upgraded` int(11) default NULL,
  `repetition` int(11) default NULL,
  `dropout` int(11) default NULL,
  `dropout_reason` int(11) default NULL,
  `is_current` int(11) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci */;

/*View structure for view student_track_latest */

/*!50001 DROP TABLE IF EXISTS `student_track_latest` */;
/*!50001 CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `student_track_latest` AS (select `students_track`.`stu_num` AS `stu_num`,`students_track`.`sch_num` AS `sch_num`,`students_track`.`sch_year` AS `sch_year`,`students_track`.`roll_no` AS `roll_no`,`students_track`.`class` AS `class`,`students_track`.`adm_year` AS `adm_year`,`students_track`.`dist_school` AS `dist_school`,`students_track`.`disability` AS `disability`,`students_track`.`sch_status` AS `sch_status`,`students_track`.`outside_home_status` AS `outside_home_status`,`students_track`.`outside_home_hrs` AS `outside_home_hrs`,`students_track`.`upgraded` AS `upgraded`,`students_track`.`repetition` AS `repetition`,`students_track`.`dropout` AS `dropout`,`students_track`.`dropout_reason` AS `dropout_reason`,`students_track`.`is_current` AS `is_current` from `students_track` where (`students_track`.`stu_num`,`students_track`.`sch_year`) in (select `students_track`.`stu_num` AS `stu_num`,max(`students_track`.`sch_year`) AS `max(sch_year)` from `students_track` group by `students_track`.`stu_num`)) */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;