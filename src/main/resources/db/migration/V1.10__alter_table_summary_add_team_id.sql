ALTER TABLE public.summary ADD team_id int NULL;

UPDATE public.summary SET team_id = 0 WHERE team_id IS NULL;

ALTER TABLE public.summary ALTER COLUMN team_id SET NOT NULL;