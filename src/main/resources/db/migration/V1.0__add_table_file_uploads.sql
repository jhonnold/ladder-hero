CREATE TABLE public.file_uploads (
	id uuid NOT NULL,
	"key" uuid NOT NULL,
	orig_file_nm varchar(260) NOT NULL,
	status int NOT NULL,
	CONSTRAINT file_uploads_pk PRIMARY KEY (id)
);
