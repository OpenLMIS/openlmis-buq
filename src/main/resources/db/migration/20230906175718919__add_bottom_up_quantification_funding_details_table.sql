ALTER TABLE bottom_up_quantifications
ADD COLUMN fundingDetailsId UUID;

--
-- Name: bottom_up_quantification_funding_details; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE bottom_up_quantification_funding_details (
    id UUID NOT NULL,
    totalProjectedFund BIGINT,
    totalForecastedCost BIGINT,
    gap BIGINT,
    bottomUpQuantificationId uuid NOT NULL,

    CONSTRAINT bottom_up_quantifications_funding_details_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_bottom_up_quantifications FOREIGN KEY (bottomUpQuantificationId) REFERENCES bottom_up_quantifications(id)
);

ALTER TABLE bottom_up_quantifications
ADD CONSTRAINT fk_bottom_up_quantification_funding_details FOREIGN KEY (fundingDetailsId) REFERENCES bottom_up_quantification_funding_details(id);