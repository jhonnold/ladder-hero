ALTER TABLE public.summary ADD did_win boolean NULL;

UPDATE public.summary SET did_win = false WHERE did_win IS NULL;

ALTER TABLE public.summary ALTER COLUMN did_win SET NOT NULL;