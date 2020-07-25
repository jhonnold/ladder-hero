CREATE TABLE public.summary (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	replay_id uuid NULL,
	player_id uuid NULL,
	working_id int NOT NULL,
	race varchar(16) NOT NULL,
	"name" varchar(32) NOT NULL,
	CONSTRAINT summary_pk PRIMARY KEY (id),
	CONSTRAINT summary_fk FOREIGN KEY (replay_id) REFERENCES public.replay(id) ON DELETE SET NULL,
	CONSTRAINT summary_fk_1 FOREIGN KEY (player_id) REFERENCES public.player(id) ON DELETE SET NULL
);
