CREATE TABLE public.users (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	username varchar(32) NOT NULL,
	"password" varchar(64) NOT NULL,
	profile_id int NULL,
	is_admin boolean NULL DEFAULT false,
	CONSTRAINT users_pk PRIMARY KEY (id),
	CONSTRAINT users_username_uniq UNIQUE (username)
);
