-- phpMyAdmin SQL Dump
-- version 3.1.5
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 24, 2009 at 06:20 PM
-- Server version: 5.0.32
-- PHP Version: 5.2.6-1+lenny3

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: 'posit_dev_new'
--

-- --------------------------------------------------------

--
-- Table structure for table 'device'
--

CREATE TABLE IF NOT EXISTS device (
  imei varchar(16) default NULL,
  `name` varchar(32) default NULL,
  user_id int(11) NOT NULL,
  auth_key varchar(32) NOT NULL,
  add_time datetime NOT NULL,
  `status` set('pending','ok') NOT NULL default 'pending',
  PRIMARY KEY  (auth_key),
  UNIQUE KEY imei (imei),
  KEY user_id (user_id),
  KEY `status` (`status`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table 'find'
--

CREATE TABLE IF NOT EXISTS find (
  id mediumint(9) NOT NULL,
  project_id int(11) NOT NULL,
  description varchar(100) NOT NULL,
  `name` varchar(32) NOT NULL,
  add_time datetime NOT NULL,
  modify_time datetime NOT NULL,
  latitude double NOT NULL,
  longitude double NOT NULL,
  PRIMARY KEY  (id),
  KEY project_id (project_id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table 'gps_sample'
--

CREATE TABLE IF NOT EXISTS gps_sample (
  id int(11) NOT NULL auto_increment,
  sample_time datetime NOT NULL,
  expedition_id int(11) NOT NULL,
  latitude double NOT NULL,
  longitude double NOT NULL,
  altitude double NOT NULL,
  accuracy double NOT NULL,
  PRIMARY KEY  (id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table 'photo'
--

CREATE TABLE IF NOT EXISTS photo (
  id int(11) NOT NULL,
  find_id int(11) NOT NULL,
  mime_type varchar(32) NOT NULL,
  data_full blob NOT NULL,
  data_thumb blob NOT NULL,
  PRIMARY KEY  (id),
  KEY find_id (find_id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table 'video'
--

CREATE TABLE IF NOT EXISTS `video` (
  `id` int(11) NOT NULL,
  `find_id` int(11) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `data_path` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `find_id` (`find_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
-- --------------------------------------------------------

--
-- Table structure for table 'audio'
--


CREATE TABLE IF NOT EXISTS `audio` (
  `id` int(11) NOT NULL,
  `find_id` int(11) NOT NULL,
  `mime_type` varchar(32) NOT NULL,
  `data_path` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `find_id` (`find_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table 'project'
--

CREATE TABLE IF NOT EXISTS project (
  id int(11) NOT NULL auto_increment,
  `name` varchar(32) NOT NULL,
  description text NOT NULL,
  create_time datetime NOT NULL,
  permission_type set('open','closed') NOT NULL default 'open',
  PRIMARY KEY  (id),
  FULLTEXT KEY description (description)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table 'user'
--

CREATE TABLE IF NOT EXISTS `user` (
  id int(11) NOT NULL auto_increment,
  email varchar(32) NOT NULL,
  `password` varchar(40) NOT NULL,
  first_name varchar(32) NOT NULL,
  last_name varchar(32) NOT NULL,
  `privileges` set('normal','admin') NOT NULL default 'normal',
  create_time datetime NOT NULL,
  PRIMARY KEY  (id),
  UNIQUE KEY email (email)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table 'user_project'
--

CREATE TABLE IF NOT EXISTS user_project (
  user_id int(11) NOT NULL,
  project_id int(11) NOT NULL,
  permission set('yes','no') default NULL,
  KEY user_id (user_id,project_id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
