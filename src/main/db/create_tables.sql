-- --------------------------------------------------------
-- ----     dictionary tables                          ----
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `inf` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `inf` varchar(100) COLLATE utf8_bin NOT NULL,
  `type` smallint(3) NOT NULL DEFAULT '0',
  `transcription` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ind_lang_inf_type` (`lang`,`inf`,`type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `word_type` (
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `id` smallint(3) NOT NULL,
  `desc` varchar(30) COLLATE utf8_bin NOT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`lang`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `tr` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_nr` smallint(3) NOT NULL,
  `fk_inf` int(11) NOT NULL,
  `tr_lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `translation` varchar(2000) COLLATE utf8_bin NOT NULL,
  `example` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ind_fk_id` (`fk_inf`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

--
-- Table `frequency` (optional table)
--

CREATE TABLE IF NOT EXISTS `frequency` (
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `word` varchar(100) COLLATE utf8_bin NOT NULL,
  `rank` int(11) NOT NULL,
  PRIMARY KEY (`lang`, `rank`),
  KEY `word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

--
-- Table `transcription` (optional table)
--

CREATE TABLE IF NOT EXISTS `transcription` (
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `word` varchar(100) COLLATE utf8_bin NOT NULL,
  `transcription` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`lang`,`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `en_wf` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fk_inf` int(11) NOT NULL DEFAULT '0',
  `wf` varchar(100) COLLATE utf8_bin NOT NULL,
  `fid` varchar(20) COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `inf_fk_en_inf` (`fk_inf`),
  KEY `ind_en_wf` (`wf`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `de_wf` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fk_inf` int(11) NOT NULL DEFAULT '0',
  `wf` varchar(100) COLLATE utf8_bin NOT NULL,
  `fid` varchar(20) COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `inf_fk_de_inf` (`fk_inf`),
  KEY `ind_de_wf` (`wf`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------
-- ----     dictionary edit history                    ----
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inf_id` int(11) NOT NULL,
  `lang_to` varchar(2) COLLATE utf8_bin NOT NULL,
  `val` varchar(2000) COLLATE utf8_bin NOT NULL,
  `uid` int(11) NOT NULL,
  `changed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `inf_id_ind` (`inf_id`,`lang_to`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `daily_word` (
  `day` int(11) NOT NULL,
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `word` varchar(100) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`day`,`lang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------
-- ----     user sessions and actions                  ----
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `sessions` (
  `id` varchar(50) COLLATE utf8_bin NOT NULL,
  `ip` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `agent` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `def_lang` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `action` varchar(30) COLLATE utf8_bin NOT NULL,
  `atime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `site_text` (
  `id` varchar(50) COLLATE utf8_bin NOT NULL,
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `val` varchar(20000) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`,`lang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE `publications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lang` varchar(2) COLLATE utf8_bin NOT NULL,
  `visible` varchar(1) COLLATE utf8_bin NOT NULL DEFAULT 'n',
  `title` varchar(250) COLLATE utf8_bin NOT NULL,
  `header` varchar(250) COLLATE utf8_bin DEFAULT NULL,
  `content` varchar(20000) COLLATE utf8_bin DEFAULT NULL,
  `author` varchar(250) COLLATE utf8_bin DEFAULT NULL,
  `publish_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`,`lang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8_bin NOT NULL,
  `pwd` varchar(32) COLLATE utf8_bin NOT NULL,
  `email` varchar(50) COLLATE utf8_bin NOT NULL,
  `confirm_id` varchar(32) COLLATE utf8_bin NOT NULL,
  `confirmed` tinyint(1) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

CREATE TABLE `user_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `type` varchar(10) COLLATE utf8_bin NOT NULL,
  `hash` varchar(50) COLLATE utf8_bin NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------
-- ----     user known words                           ----
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `user_inf` (
  `user_id` int(11) NOT NULL,
  `inf_id` int(11) NOT NULL,
  `known` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`,`inf_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `user_word_class` (
  `uid` int(11) NOT NULL,
  `lang` varchar(2) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `wcl_uid` int(11) NOT NULL,
  UNIQUE KEY `uid` (`uid`,`lang`,`wcl_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------
-- ----     word frequency (rank) dictionary           ----
-- ----     for vocabulary size tests                  ----
-- --------------------------------------------------------

CREATE TABLE `de_frequency` (
  `word` varchar(100) COLLATE utf8_bin NOT NULL,
  `rank` int(11) NOT NULL,
  PRIMARY KEY (`rank`),
  KEY `word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

CREATE TABLE `en_frequency` (
  `word` varchar(100) CHARACTER SET utf8 NOT NULL,
  `rank` int(11) NOT NULL,
  PRIMARY KEY (`rank`),
  KEY `word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

CREATE TABLE `uk_frequency` (
  `word` varchar(100) COLLATE utf8_bin NOT NULL,
  `rank` int(11) NOT NULL,
  PRIMARY KEY (`rank`),
  UNIQUE KEY `uk_word_ind` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- --------------------------------------------------------
-- ----     word usage trand                           ----
-- --------------------------------------------------------

CREATE TABLE `stats_yearly` (
  `word` varchar(100) COLLATE utf8_bin NOT NULL,
  `cnt1990` double DEFAULT NULL,
  `cnt1991` double DEFAULT NULL,
  `cnt1992` double DEFAULT NULL,
  `cnt1993` double DEFAULT NULL,
  `cnt1994` double DEFAULT NULL,
  `cnt1995` double DEFAULT NULL,
  `cnt1996` double DEFAULT NULL,
  `cnt1997` double DEFAULT NULL,
  `cnt1998` double DEFAULT NULL,
  `cnt1999` double DEFAULT NULL,
  `cnt2000` double DEFAULT NULL,
  `cnt2001` double DEFAULT NULL,
  `cnt2002` double DEFAULT NULL,
  `cnt2003` double DEFAULT NULL,
  `cnt2004` double DEFAULT NULL,
  `cnt2005` double DEFAULT NULL,
  `cnt2006` double DEFAULT NULL,
  `cnt2007` double DEFAULT NULL,
  `cnt2008` double DEFAULT NULL,
  `cnt2009` double DEFAULT NULL,
  `cnt2010` double DEFAULT NULL,
  `cnt2011` double DEFAULT NULL,
  `cnt2012` double DEFAULT NULL,
  `cnt2013` double DEFAULT NULL,
  `cnt2014` double DEFAULT NULL,
  `cnt2015` double DEFAULT NULL,
  `cnt2016` double DEFAULT NULL,
  `cnt2017` double DEFAULT NULL,
  `deviation` double DEFAULT NULL,
  PRIMARY KEY (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPRESSED;


