ALTER TABLE public.users ADD account_id int NULL;
ALTER TABLE public.users ADD battle_tag varchar(64) NULL;
ALTER TABLE public.users DROP COLUMN code;
