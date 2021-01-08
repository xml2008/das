CREATE TABLE `txlog` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `XID` varchar(45) NOT NULL,
  `nodeID` varchar(45) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  `status` varchar(45) NOT NULL,
  `applicationID` varchar(11) DEFAULT NULL,
  `inserttime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY key pk_id(`id`),
   KEY `idx_inserttime` (`inserttime`)
);