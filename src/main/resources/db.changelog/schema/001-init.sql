CREATE TABLE usr (
	id integer NOT NULL,
    refUrl varchar(100),
    money decimal,
    position varchar(20),
    auth boolean,
    count_refs integer,
    qiwi varchar(20),
    bonus boolean,
    money_from_partners integer,
	CONSTRAINT pk_car PRIMARY KEY (id)
);

CREATE TABLE payment (
     id uuid NOT NULL,
     user_id integer,
     sum integer,
     date varchar,
     time_payment varchar,
     successful boolean,
     CONSTRAINT pk_car PRIMARY KEY (id)
);

CREATE TABLE channel (
     id varchar NOT NULL,
     url varchar(100),
     price integer,
     start boolean,
     CONSTRAINT pk_car PRIMARY KEY (id)
);

CREATE TABLE channel_check (
     id uuid NOT NULL,
     channel_id varchar(100),
     user_id integer,
     CONSTRAINT pk_car PRIMARY KEY (id)
);