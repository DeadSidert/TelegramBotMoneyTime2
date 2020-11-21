CREATE TABLE usr (
                     id integer NOT NULL,
                     ref_url varchar(100),
                     money decimal,
                     position varchar(20),
                     auth boolean,
                     count_refs integer,
                     refer_id integer,
                     qiwi varchar(20),
                     bonus boolean,
                     money_from_partners integer,
                     reg_date varchar,
                     CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE payment (
                         id serial NOT NULL,
                         user_id integer,
                         sum decimal,
                         date varchar,
                         time_payment varchar,
                         successful boolean,
                         CONSTRAINT pk_payment PRIMARY KEY (id)
);

CREATE TABLE channel (
                         id varchar NOT NULL,
                         url varchar(100),
                         price decimal,
                         start boolean,
                         CONSTRAINT pk_channel PRIMARY KEY (id)
);

CREATE TABLE channel_check (
                               id serial NOT NULL,
                               channel_id varchar(100),
                               user_id integer,
                               CONSTRAINT pk_channel_check PRIMARY KEY (id)
);

CREATE TABLE bonus_channel (
                         id serial NOT NULL,
                         url varchar(100),
                         CONSTRAINT pk_bonus_channel PRIMARY KEY (id)
);