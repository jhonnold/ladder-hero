CREATE TABLE public.file_upload (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	"key" uuid NOT NULL,
	orig_file_nm varchar(260) NOT NULL,
	status varchar(16) NOT NULL,
	CONSTRAINT file_upload_pk PRIMARY KEY (id)
);
