ALTER TABLE public.replay ADD slug varchar(64) NULL;
ALTER TABLE public.replay ADD CONSTRAINT replay_un UNIQUE (slug);
