CREATE TABLE public.player (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	profile_id int NULL,
	region_id int NULL,
	realm_id int NULL,
	"name" varchar(256) NOT NULL,
	CONSTRAINT player_pk PRIMARY KEY (id)
);
