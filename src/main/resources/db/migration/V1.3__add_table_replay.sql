CREATE TABLE public.replay (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	file_upload_id uuid NULL,
	map_nm varchar(128) NOT NULL,
	dur_s int NULL,
	played_at timestamp NULL,
	CONSTRAINT replay_pk PRIMARY KEY (id),
	CONSTRAINT replay_fk FOREIGN KEY (file_upload_id) REFERENCES public.file_upload(id) ON DELETE SET NULL
);
