--
-- Name: bottom_up_quantifications; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantifications (
    id uuid NOT NULL,
    createdDate timestamptz,
    modifiedDate timestamptz,
    facilityId uuid NOT NULL,
    programId uuid NOT NULL,
    processingPeriodId uuid NOT NULL,
    targetYear integer NOT NULL,
    status character varying(255) NOT NULL,

    CONSTRAINT bottom_up_quantifications_pkey PRIMARY KEY (id)
);


--
-- Name: bottom_up_quantification_line_items; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantification_line_items (
    id uuid NOT NULL,
    orderableId uuid NOT NULL,
    annualAdjustedConsumption integer,
    verifiedAnnualAdjustedConsumption integer,
    forecastedDemand integer,
    bottomUpQuantificationId uuid NOT NULL,

    CONSTRAINT bottom_up_quantification_line_items_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_bottom_up_quantifications FOREIGN KEY (bottomUpQuantificationId) REFERENCES bottom_up_quantifications(id)
);


--
-- Name: bottom_up_quantification_status_changes; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantification_status_changes (
    id uuid NOT NULL,
    occurredDate timestamptz,
    authorId uuid NOT NULL,
    status character varying(255) NOT NULL,
    bottomUpQuantificationId uuid NOT NULL,

    CONSTRAINT bottom_up_quantification_status_changes_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_bottom_up_quantifications FOREIGN KEY (bottomUpQuantificationId) REFERENCES bottom_up_quantifications(id)
);
