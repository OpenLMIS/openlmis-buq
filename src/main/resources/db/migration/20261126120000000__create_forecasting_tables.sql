CREATE TABLE population_segments (
    id uuid NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    populationPercentage numeric(5,2) NOT NULL,

    CONSTRAINT population_segments_pkey PRIMARY KEY (id),
    CONSTRAINT population_segments_code_unique UNIQUE (code),
    CONSTRAINT population_segments_percentage_check
        CHECK (populationPercentage >= 0 AND populationPercentage <= 100)
);

CREATE TABLE forecasting_settings (
    id uuid NOT NULL,
    createdDate timestamptz,
    modifiedDate timestamptz,
    programId uuid NOT NULL,
    orderableId uuid NOT NULL,
    populationSegmentId uuid NOT NULL,
    dosesPerTarget integer NOT NULL,
    coveragePercentage numeric(5,2) NOT NULL,
    wastagePercentage numeric(5,2) NOT NULL,
    bufferMonths numeric(4,2) NOT NULL DEFAULT 1,

    CONSTRAINT forecasting_settings_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_population_segments FOREIGN KEY (populationSegmentId)
        REFERENCES population_segments(id),
    CONSTRAINT forecasting_settings_program_orderable_unique UNIQUE (programId, orderableId),
    CONSTRAINT forecasting_settings_doses_check CHECK (dosesPerTarget >= 1),
    CONSTRAINT forecasting_settings_coverage_check
        CHECK (coveragePercentage >= 0 AND coveragePercentage <= 100),
    -- the wastage factor is 100 / (100 - wastage), so 100 % is not a valid wastage
    CONSTRAINT forecasting_settings_wastage_check
        CHECK (wastagePercentage >= 0 AND wastagePercentage < 100),
    CONSTRAINT forecasting_settings_buffer_check CHECK (bufferMonths >= 0 AND bufferMonths <= 12)
);

CREATE TABLE supply_ratios (
    id uuid NOT NULL,
    createdDate timestamptz,
    modifiedDate timestamptz,
    vaccineOrderableId uuid NOT NULL,
    supplyOrderableId uuid NOT NULL,
    type character varying(20) NOT NULL,
    ratio numeric(9,4) NOT NULL,

    CONSTRAINT supply_ratios_pkey PRIMARY KEY (id),
    CONSTRAINT supply_ratios_vaccine_supply_unique UNIQUE (vaccineOrderableId, supplyOrderableId),
    CONSTRAINT supply_ratios_type_check CHECK (type IN ('DOSE', 'VIAL', 'BOX')),
    CONSTRAINT supply_ratios_ratio_check CHECK (ratio > 0 AND ratio <= 100),
    CONSTRAINT supply_ratios_distinct_orderables_check
        CHECK (vaccineOrderableId <> supplyOrderableId)
);

CREATE TABLE forecasts (
    id uuid NOT NULL,
    createdDate timestamptz,
    modifiedDate timestamptz,
    facilityId uuid NOT NULL,
    programId uuid NOT NULL,
    targetYear integer NOT NULL,
    status character varying(255) NOT NULL,
    catchmentPopulation integer NOT NULL,
    supervisoryNodeId uuid,

    CONSTRAINT forecasts_pkey PRIMARY KEY (id),
    CONSTRAINT forecasts_facility_program_year_unique UNIQUE (facilityId, programId, targetYear),
    CONSTRAINT forecasts_catchment_check CHECK (catchmentPopulation >= 0)
);

CREATE TABLE forecast_line_items (
    id uuid NOT NULL,
    forecastId uuid NOT NULL,
    orderableId uuid NOT NULL,
    type character varying(20) NOT NULL,
    populationSegmentId uuid,
    dosesPerTarget integer,
    coveragePercentage numeric(5,2),
    wastagePercentage numeric(5,2),
    bufferMonths numeric(4,2),
    targetPopulation integer,
    annualDemand integer,
    bufferStock integer,
    forecastedAmount integer NOT NULL,

    CONSTRAINT forecast_line_items_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_forecasts FOREIGN KEY (forecastId) REFERENCES forecasts(id),
    CONSTRAINT fkey_population_segments FOREIGN KEY (populationSegmentId)
        REFERENCES population_segments(id),
    CONSTRAINT forecast_line_items_forecast_orderable_unique UNIQUE (forecastId, orderableId),
    CONSTRAINT forecast_line_items_type_check CHECK (type IN ('VACCINE', 'SUPPLY')),
    CONSTRAINT forecast_line_items_amounts_check CHECK (
        forecastedAmount >= 0
        AND (targetPopulation IS NULL OR targetPopulation >= 0)
        AND (annualDemand IS NULL OR annualDemand >= 0)
        AND (bufferStock IS NULL OR bufferStock >= 0)
    ),
    -- vaccine rows carry the setting they were computed from; supply rows only an amount
    CONSTRAINT forecast_line_items_shape_check CHECK (
        (type = 'VACCINE'
            AND populationSegmentId IS NOT NULL
            AND dosesPerTarget IS NOT NULL
            AND coveragePercentage IS NOT NULL
            AND wastagePercentage IS NOT NULL
            AND bufferMonths IS NOT NULL
            AND targetPopulation IS NOT NULL
            AND annualDemand IS NOT NULL
            AND bufferStock IS NOT NULL)
        OR (type = 'SUPPLY'
            AND populationSegmentId IS NULL
            AND dosesPerTarget IS NULL
            AND coveragePercentage IS NULL
            AND wastagePercentage IS NULL
            AND bufferMonths IS NULL
            AND targetPopulation IS NULL
            AND annualDemand IS NULL
            AND bufferStock IS NULL)
    )
);

CREATE TABLE forecast_status_changes (
    id uuid NOT NULL,
    occurredDate timestamptz,
    authorId uuid NOT NULL,
    status character varying(255) NOT NULL,
    forecastId uuid NOT NULL,

    CONSTRAINT forecast_status_changes_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_forecasts FOREIGN KEY (forecastId) REFERENCES forecasts(id)
);

CREATE TABLE forecast_rejections (
    id uuid NOT NULL,
    createdDate timestamptz,
    modifiedDate timestamptz,
    generalComments text,
    statusChangeId uuid NOT NULL,

    CONSTRAINT forecast_rejections_pkey PRIMARY KEY (id),
    CONSTRAINT fkey_forecast_status_changes FOREIGN KEY (statusChangeId)
        REFERENCES forecast_status_changes(id),
    CONSTRAINT forecast_rejections_status_change_unique UNIQUE (statusChangeId)
);

CREATE TABLE forecast_rejection_reasons (
    rejectionId uuid NOT NULL,
    rejectionReasonId uuid NOT NULL,

    CONSTRAINT fkey_forecast_rejections FOREIGN KEY (rejectionId)
        REFERENCES forecast_rejections(id)
);
