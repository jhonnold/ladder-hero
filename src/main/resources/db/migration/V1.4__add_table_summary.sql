CREATE TABLE public.summaries (
	id uuid NOT NULL,
	replay_id uuid NULL,
	player_id uuid NULL,
	working_id int NOT NULL,
	race varchar(16) NOT NULL,
	"name" varchar(32) NOT NULL,
	CONSTRAINT summaries_pk PRIMARY KEY (id),
	CONSTRAINT summaries_fk_replays FOREIGN KEY (replay_id) REFERENCES public.replays(id) ON DELETE SET NULL,
	CONSTRAINT summaries_fk_players FOREIGN KEY (player_id) REFERENCES public.players(id) ON DELETE SET NULL
);
