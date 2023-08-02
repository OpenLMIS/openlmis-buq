--
-- Name: sources_of_funds; Type: TABLE; Schema: buq; Owner: postgres; Tablespace:
--

CREATE TABLE sources_of_funds (
    id uuid NOT NULL,
    name text NOT NULL,
    description text
);

CREATE UNIQUE INDEX unq_source_of_fund_name
ON sources_of_funds (LOWER(name))
WHERE name IS NOT NULL;
