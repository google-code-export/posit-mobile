/* 
SQLyog Enterprise v4.01
Host - localhost : Database - posit
**************************************************************
Server version 5.0.67
*/

/*
Table structure for devices
*/

CREATE TABLE `devices` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `project_id` int(11) NOT NULL,
  `name` varchar(250) NOT NULL,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

/*
Table data for posit.devices
*/

INSERT INTO `devices` VALUES 
(1,1,'MyPhone','123456789012345');

/*
Table structure for fields
*/

CREATE TABLE `fields` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(250) NOT NULL,
  `datatype` varchar(250) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

/*
Table data for posit.fields
*/

INSERT INTO `fields` VALUES 
(1,'Text','text'),
(2,'Number','text'),
(3,'Email','text'),
(4,'Image','text');

/*
Table structure for groups
*/

CREATE TABLE `groups` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(50) NOT NULL,
  `created_at` datetime default NULL,
  `modified_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*
Table data for posit.groups
*/

INSERT INTO `groups` VALUES 
(1,'Admin','2009-03-25 11:26:46','2009-03-25 11:26:46'),
(2,'Member','2009-03-25 11:26:54','2009-03-25 11:26:54');

/*
Table structure for news
*/

CREATE TABLE `news` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `user_id` int(11) default NULL,
  `title` varchar(250) NOT NULL,
  `teaser` text,
  `body` text,
  `created_at` datetime NOT NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*
Table data for posit.news
*/

INSERT INTO `news` VALUES 
(1,1,'POSIT 1.0 Released','Cras metus elit, lacinia a, bibendum a, porttitor quis, lectus. Integer posuere ante id sapien. Praesent feugiat sagittis lorem. Phasellus quis nulla.','Ut posuere tellus in pede. Nunc malesuada nulla sed urna. Aliquam ipsum dolor, congue quis, sollicitudin sed, accumsan sit amet, eros. Ut ultrices lorem interdum justo. Integer feugiat. Fusce nisl felis, tempus vel, pharetra sit amet, gravida in, lacus. Phasellus sit amet eros vitae odio rutrum commodo. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In mauris quam, semper vel, mattis et, euismod in, velit. Nam lectus lorem, ullamcorper nec, condimentum a, elementum eget, pede. Cras ultricies lectus in elit. ','0000-00-00 00:00:00',NULL),
(2,2,'Bugs Fixed','Ut posuere tellus in pede. Nunc malesuada nulla sed urna. Aliquam ipsum dolor, congue quis, sollicitudin sed, accumsan sit amet, eros. Ut ultrices lorem interdum justo. Integer feugiat. Fusce nisl felis, tempus vel, pharetra sit amet, gravida in, lacus.','Nam lectus lorem, ullamcorper nec, condimentum a, elementum eget, pede. Cras ultricies lectus in elit. Donec vitae odio in arcu mollis tristique. Maecenas sed neque vitae ligula pharetra facilisis. Quisque egestas odio ac nibh. ','2009-03-26 00:00:00',NULL);

/*
Table structure for project_field
*/

CREATE TABLE `project_field` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `project_id` int(11) NOT NULL,
  `field_id` int(11) NOT NULL,
  `name` varchar(50) default NULL,
  `desc` varchar(500) default NULL,
  `order` int(11) default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=22 DEFAULT CHARSET=latin1;

/*
Table data for posit.project_field
*/

INSERT INTO `project_field` VALUES 
(1,1,1,'Name','Name of the person',0),
(3,1,3,'Email','Email address of the victim',2),
(4,3,1,'Name','Name of the person',0),
(5,3,1,'Address','Address of the victim',1),
(6,3,2,'Phone','Phone number',2),
(9,1,1,'College','Name of College you are studying',1);

/*
Table structure for projects
*/

CREATE TABLE `projects` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `name` varchar(250) NOT NULL,
  `desc` text NOT NULL,
  `app_key` varchar(250) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

/*
Table data for posit.projects
*/

INSERT INTO `projects` VALUES 
(1,1,'Mission Rescue','Fusce nisl felis, tempus vel, pharetra sit amet, gravida in, lacus. Phasellus sit amet eros vitae odio rutrum commodo. Aliquam erat volutpat. Praesent feugiat sagittis lorem.','9e05ca972175b35a319dd5a4e7b85cab','2009-03-26 00:00:00',NULL),
(3,1,'Earthquake Disaster Management','Project to save people during earthquake and disasters.','5076520690e870e253541ff352e54c4a',NULL,NULL);

/*
Table structure for users
*/

CREATE TABLE `users` (
  `id` int(10) NOT NULL auto_increment,
  `group_id` int(11) default NULL,
  `name` varchar(40) NOT NULL,
  `password` varchar(40) NOT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(40) NOT NULL,
  `last_name` varchar(40) NOT NULL,
  `active` tinyint(4) default NULL,
  `last_ip` varchar(40) default NULL,
  `last_login` datetime default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `username` (`name`),
  UNIQUE KEY `email` (`email`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*
Table data for posit.users
*/

INSERT INTO `users` VALUES 
(1,1,'posit','3d3d041d37a98539789c71ba4d72fa4b','posit@posit.com','Posit','User',1,'','2009-03-26 01:27:00',NULL,NULL),
(2,2,'jwala','8c6facdbe6fd1f741ce910c72ad331e5','jwalanta@gmail.com','Jwalanta','Shrestha',1,'','2009-03-26 01:28:00',NULL,NULL);

