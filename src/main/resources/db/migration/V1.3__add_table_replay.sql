CREATE TABLE public.replays (
	id uuid NOT NULL,
	file_upload_id uuid NULL,
	map_nm varchar(128) NOT NULL,
	dur_s int NULL,
	played_at timestamp NULL,
	CONSTRAINT replays_pk PRIMARY KEY (id),
	CONSTRAINT replays_fk_uploads FOREIGN KEY (file_upload_id) REFERENCES public.file_uploads(id) ON DELETE SET NULL
);
