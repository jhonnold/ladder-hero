ALTER TABLE public.replays ADD slug varchar(64) NULL;
ALTER TABLE public.replays ADD CONSTRAINT replays_un_slug UNIQUE (slug);
