CREATE TABLE public.players (
	id uuid NOT NULL,
	profile_id int NULL,
	region_id int NULL,
	realm_id int NULL,
	CONSTRAINT players_pk PRIMARY KEY (id)
);
