DROP TABLE environment;
DROP TABLE adminUserRole;
DROP TABLE adminUser;

DROP TABLE taskgroup;
DROP TABLE task;
DROP TABLE label;
DROP TABLE cohort;
DROP TABLE status;
DROP TABLE role;
DROP TABLE language;

CREATE TABLE environment (
  	id 					BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  	rev					BIGINT NOT NULL,
	name 				VARCHAR(250) NOT NULL,
	location 			VARCHAR(250) NOT NULL,
	url					VARCHAR(1000) NOT NULL,
	host				VARCHAR(250) NOT NULL,
	port				INT NOT NULL,
	userName			VARCHAR(250) NOT NULL,
	password			VARCHAR(250) NOT NULL,
	UNIQUE KEY			name(name(250))
) ENGINE=InnoDB;

CREATE TABLE adminuser (
  	id 					BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  	rev					BIGINT NOT NULL,
	email 				VARCHAR(250) NOT NULL,
	fullName 			VARCHAR(250) NOT NULL,
	isSuperUser 		BIT NOT NULL,
	password 			VARCHAR(1000),
	temporaryPassword 	VARCHAR(250),
	isPasswordValid		BIT NOT NULL,
	failedLoginAttempts	INT,
	UNIQUE KEY			email(email(250))
) ENGINE=InnoDB;

CREATE TABLE role (
  	id 					BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  	rev					BIGINT NOT NULL,
	name 				VARCHAR(250) NOT NULL,
	UNIQUE KEY			name(name(250))
) ENGINE=InnoDB;

CREATE TABLE cohort (
  	id 					BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  	rev					BIGINT NOT NULL,
	role 				BIGINT NOT NULL,
	name 				VARCHAR(250) NOT NULL,
	startdate 			CHAR(10),
	disabledate 		CHAR(10)
) ENGINE=InnoDB;

CREATE TABLE adminuserrole (
	adminUser 			BIGINT NOT NULL,
	role 				BIGINT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE taskGroup (
  	id 					BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  	entityType			INT NOT NULL,
	entityId			BIGINT NOT NULL,
	name 				VARCHAR(250) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE task (
  	id 					BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	taskGroup			BIGINT NOT NULL,
	name 				VARCHAR(250) NOT NULL,
	dueDate 			VARCHAR(10)
) ENGINE=InnoDB;

CREATE TABLE label (
	entity 				BIGINT NOT NULL,
	entityType			INT NOT NULL,
	language 			VARCHAR(10) NOT NULL,
	text 				VARCHAR(1000),
	PRIMARY KEY			labelId(entity, entityType, language)
) ENGINE=InnoDB;


CREATE TABLE status (
	role 				BIGINT NOT NULL,
	name 				VARCHAR(250) NOT NULL,
	PRIMARY KEY 		statusId(role,name)
) ENGINE=InnoDB;

CREATE TABLE language (
	name				VARCHAR(10) NOT NULL PRIMARY KEY
) ENGINE=InnoDB;

ALTER TABLE task
ADD CONSTRAINT fk_taskGroup
FOREIGN KEY (taskGroup)
REFERENCES taskGroup(id)
ON DELETE CASCADE;

ALTER TABLE adminuserrole
ADD PRIMARY KEY (role,adminUser);

ALTER TABLE COHORT
ADD CONSTRAINT uc_cohort_name UNIQUE (role,name);

ALTER TABLE status
ADD CONSTRAINT uc_status_name UNIQUE (role,name);

ALTER TABLE task
ADD CONSTRAINT uc_task_name UNIQUE (taskGroup,name);

ALTER TABLE cohort
ADD FOREIGN KEY (role)
REFERENCES role(id);

ALTER TABLE taskGroup
ADD CONSTRAINT uc_task_group UNIQUE (entityType,entityId,name);

insert into adminuser (email, fullName, isSuperUser,password,isPasswordValid,failedLoginAttempts)
values ('test@rbc.com','Test User',true,'ba5308ec7701df248c9384289b4d202f55b7d2dc4844f2802975f3c761e4dab439c7551b1b6f31e0', true,0);

