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

package org.openlmis.buq.service.referencedata;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.OrderableDtoDataBuilder;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;

public class OrderableReferenceDataServiceTest
    extends BaseReferenceDataServiceTest<BasicOrderableDto> {

  private OrderableReferenceDataService service;

  @Override
  protected BaseReferenceDataService<BasicOrderableDto> getService() {
    return new OrderableReferenceDataService();
  }

  @Override
  protected BasicOrderableDto generateInstance() {
    return new OrderableDtoDataBuilder().buildAsDto();
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
    service = (OrderableReferenceDataService) prepareService();
  }

  @Test
  public void shouldFindAllResources() {
    // when
    BasicOrderableDto dto = mockPageResponseEntityAndGetDto();
    List<BasicOrderableDto> found = service.findAll();

    // then
    assertThat(found, hasItem(dto));

    verifyPageRequest()
        .isGetRequest()
        .hasAuthHeader()
        .hasEmptyBody()
        .isUriStartsWith(service.getServiceUrl() + service.getUrl());
  }

}
