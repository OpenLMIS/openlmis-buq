/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.buq.web.buq;

import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_INVALID_PARAMETER_BOTTOM_UP_QUANTIFICATION_STATUS;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_INVALID_SEARCH_PARAMS;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.exception.ValidationMessageException;
import org.springframework.util.LinkedMultiValueMap;

public class QueryBottomUpQuantificationSearchParamsTest {

  private static final String STATUS = "status";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private LinkedMultiValueMap<String, String> queryMap;

  @Before
  public void setUp() {
    queryMap = new LinkedMultiValueMap<>();
  }

  @Test
  public void shouldGetEmptySetIfNoStatusesProvided() {
    QueryBottomUpQuantificationSearchParams params =
        new QueryBottomUpQuantificationSearchParams(queryMap);

    assertEquals(emptySet(), params.getStatuses());
  }

  @Test
  public void shouldGetStatusValueFromParameters() {
    queryMap.add(STATUS, BottomUpQuantificationStatus.DRAFT.toString());
    queryMap.add(STATUS, BottomUpQuantificationStatus.SUBMITTED.toString());
    QueryBottomUpQuantificationSearchParams params =
        new QueryBottomUpQuantificationSearchParams(queryMap);

    assertThat(params.getStatuses(),
        hasItems(BottomUpQuantificationStatus.DRAFT, BottomUpQuantificationStatus.SUBMITTED));
  }

  @Test
  public void shouldThrowExceptionIfBottomUpQuantificationStatusIsNotValid() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_INVALID_PARAMETER_BOTTOM_UP_QUANTIFICATION_STATUS);

    queryMap.add(STATUS, BottomUpQuantificationStatus.APPROVED_BY_DP.toString());
    queryMap.add(STATUS, "INVALID_STATUS");
    QueryBottomUpQuantificationSearchParams params =
        new QueryBottomUpQuantificationSearchParams(queryMap);

    params.getStatuses();
  }

  @Test
  public void shouldThrowExceptionIfThereIsUnknownParameterInParameters() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_INVALID_SEARCH_PARAMS);

    queryMap.add("some-param", "some-value");
    new QueryBottomUpQuantificationSearchParams(queryMap);
  }

}
