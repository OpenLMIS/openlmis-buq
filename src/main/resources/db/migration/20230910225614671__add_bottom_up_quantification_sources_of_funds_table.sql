--
-- Name: bottom_up_quantification_sources_of_funds; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--
CREATE TABLE bottom_up_quantification_sources_of_funds (
    id UUID NOT NULL,
    bottomUpQuantificationFundingDetailsId UUID,
    amountUsedInLastFinancialYear NUMERIC(19, 2),
    projectedFund NUMERIC(19, 2),
    sourceOfFundId UUID,

    CONSTRAINT bottom_up_quantification_sources_of_funds_pkey PRIMARY KEY (id),
    CONSTRAINT fk_bottom_up_quantification_funding_details FOREIGN KEY (bottomUpQuantificationFundingDetailsId)
        REFERENCES bottom_up_quantification_funding_details(id)
);
