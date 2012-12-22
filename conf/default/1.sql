#schema

# --- !Ups



CREATE TABLE post (
	id bigint NOT NULL AUTO_INCREMENT,
	chapeau  longtext,
	content longtext,
	hits int,
	postedat datetime NOT NULL,
	title varchar(255),
	url varchar(255),
	published bool,
	PRIMARY KEY (id)
);


CREATE TABLE image (
  id bigint not null auto_increment primary key,
  contenttype varchar(64) NOT NULL,
  data blob not null,
  filename varchar(255)
);

create table account(
  id bigint not null auto_increment primary key,
  email varchar(255) NOT NULL,
  password varchar(255) NOT NULL
);







# --- !Downs


drop table post;
drop table image;
drop table account;






