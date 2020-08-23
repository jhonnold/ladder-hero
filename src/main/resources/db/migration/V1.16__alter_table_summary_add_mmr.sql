ALTER TABLE public.summary ADD mmr int NULL;
UPDATE public.summary SET mmr = 0 WHERE mmr IS NULL;
ALTER TABLE public.summary ALTER COLUMN mmr SET NOT NULL;
