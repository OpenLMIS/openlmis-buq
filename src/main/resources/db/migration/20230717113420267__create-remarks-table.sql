CREATE TABLE remarks
(
    id uuid NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    CONSTRAINT remarks_pkey PRIMARY KEY (id)
);
