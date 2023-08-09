--
-- Name: bottom_up_quantifications; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantifications (
    id uuid NOT NULL,
    createddate timestamptz,
    modifieddate timestamptz,
    facilityid uuid NOT NULL,
    programid uuid NOT NULL,
    processingperiodid uuid NOT NULL,
    targetyear integer NOT NULL,
    status character varying(255) NOT NULL,

    CONSTRAINT bottom_up_quantifications_pkey PRIMARY KEY (id)
);


--
-- Name: bottom_up_quantification_line_items; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantification_line_items (
    id uuid NOT NULL,
    orderableid uuid NOT NULL,
    annualAdjustedConsumption integer,
    verifiedAnnualAdjustedConsumption integer,
    forecastedDemand integer,
    bottomupquantificationid uuid NOT NULL,

    CONSTRAINT bottom_up_quantification_line_items_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_bottom_up_quantifications FOREIGN KEY (bottomupquantificationid) REFERENCES bottom_up_quantifications(id)
);


--
-- Name: bottom_up_quantification_status_changes; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantification_status_changes (
    id uuid NOT NULL,
    occurredDate timestamptz,
    authorid uuid NOT NULL,
    status character varying(255) NOT NULL,
    bottomupquantificationid uuid NOT NULL,

    CONSTRAINT bottom_up_quantification_status_changes_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_bottom_up_quantifications FOREIGN KEY (bottomupquantificationid) REFERENCES bottom_up_quantifications(id)
);