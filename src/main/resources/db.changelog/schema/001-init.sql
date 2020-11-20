CREATE TABLE car (
	id uuid NOT NULL,
    name varchar(100),
    cost integer,
	CONSTRAINT pk_car PRIMARY KEY (id),
    CONSTRAINT car_name_unique UNIQUE (name)
);