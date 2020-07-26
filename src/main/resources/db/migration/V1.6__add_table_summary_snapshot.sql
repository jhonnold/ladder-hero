CREATE TABLE public.summary_snapshot (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	summary_id uuid NULL,
    "loop" int NULL,
	lost_minerals int NULL,
	lost_vespene int NULL,
	unspent_minerals int NULL,
	unspent_vespene int NULL,
	collection_rate_minerals int NULL,
	collection_rate_vespene int NULL,
	active_workers int NULL,
	army_value_minerals int NULL,
	army_value_vespene int NULL,
	CONSTRAINT summary_snapshot_pk PRIMARY KEY (id),
	CONSTRAINT summary_snapshot_fk FOREIGN KEY (summary_id) REFERENCES public.summary(id) ON DELETE CASCADE
);
