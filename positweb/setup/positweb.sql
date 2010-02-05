-- phpMyAdmin SQL Dump
-- version 2.9.1.1-Debian-6
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Generation Time: Feb 05, 2010 at 04:08 PM
-- Server version: 5.0.32
-- PHP Version: 5.2.0-8+etch7
-- 
-- Database: `posit_rampositweb`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `audio`
-- 

CREATE TABLE `audio` (
  `id` int(11) NOT NULL,
  `find_id` int(11) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `data_path` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `find_id` (`find_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `device`
-- 

CREATE TABLE `device` (
  `imei` varchar(16) default NULL,
  `name` varchar(32) default NULL,
  `user_id` int(11) NOT NULL,
  `auth_key` varchar(32) NOT NULL,
  `add_time` datetime NOT NULL,
  `status` set('pending','ok') NOT NULL default 'pending',
  PRIMARY KEY  (`auth_key`),
  UNIQUE KEY `imei` (`imei`),
  KEY `user_id` (`user_id`),
  KEY `status` (`status`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `expedition`
-- 

CREATE TABLE `expedition` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(32) default 'Expedition',
  `project_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `description` text,
  `add_time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `modify_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=169 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `find`
-- 

CREATE TABLE `find` (
  `id` mediumint(9) NOT NULL auto_increment,
  `project_id` int(11) NOT NULL,
  `description` varchar(100) NOT NULL,
  `barcode_id` varchar(32) default NULL,
  `name` varchar(32) NOT NULL,
  `add_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `revision` int(11) NOT NULL,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `project_id` (`project_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=16 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `find_history`
-- 

CREATE TABLE `find_history` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `find_guid` varchar(50) NOT NULL,
  `action` varchar(20) NOT NULL,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=23 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `gps_sample`
-- 

CREATE TABLE `gps_sample` (
  `id` int(11) NOT NULL auto_increment,
  `sample_time` datetime NOT NULL,
  `expedition_id` int(11) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `altitude` double NOT NULL,
  `swath` bigint(17) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=15916 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `photo`
-- 

CREATE TABLE `photo` (
  `id` int(11) NOT NULL auto_increment,
  `guid` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `identifier` bigint(17) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `data_full` blob NOT NULL,
  `data_thumb` blob NOT NULL,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `guid` (`guid`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=17 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `project`
-- 

CREATE TABLE `project` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(32) NOT NULL,
  `description` text NOT NULL,
  `create_time` datetime NOT NULL,
  `permission_type` set('open','closed') NOT NULL default 'open',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `sync_history`
-- 

CREATE TABLE `sync_history` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=69 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `user`
-- 

CREATE TABLE `user` (
  `id` int(11) NOT NULL auto_increment,
  `email` varchar(32) NOT NULL,
  `password` varchar(40) NOT NULL,
  `first_name` varchar(32) NOT NULL,
  `last_name` varchar(32) NOT NULL,
  `privileges` set('normal','admin') NOT NULL default 'normal',
  `create_time` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6 ;

-- --------------------------------------------------------

-- 
-- Table structure for table `user_project`
-- 

CREATE TABLE `user_project` (
  `user_id` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `permission` set('yes','no') default NULL,
  KEY `user_id` (`user_id`,`project_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `video`
-- 

CREATE TABLE `video` (
  `id` int(11) NOT NULL,
  `find_id` int(11) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `data_path` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `find_id` (`find_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
