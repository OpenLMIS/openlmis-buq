CREATE TABLE rejections (
    id uuid NOT NULL,
    createdDate timestamptz,
    modifiedDate timestamptz,
    generalComments TEXT,
    statusChangeId uuid,

    CONSTRAINT rejections_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_status_change FOREIGN KEY (statusChangeId) REFERENCES bottom_up_quantification_status_changes(id)
);

CREATE TABLE rejection_rejection_reasons (
    rejectionId uuid,
    rejectionReasons uuid,

    CONSTRAINT fkey_rejection FOREIGN KEY (rejectionId) REFERENCES rejections(id)
);
