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

import java.util.List;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.service.RequestParameters;
import org.springframework.stereotype.Service;

@Service
public class OrderableReferenceDataService
    extends BaseReferenceDataService<BasicOrderableDto> {

  @Override
  protected String getUrl() {
    return "/api/orderables/";
  }

  @Override
  protected Class<BasicOrderableDto> getResultClass() {
    return BasicOrderableDto.class;
  }

  @Override
  protected Class<BasicOrderableDto[]> getArrayResultClass() {
    return BasicOrderableDto[].class;
  }

  @Override
  public List<BasicOrderableDto> findAll() {
    return getPage(RequestParameters.init()).getContent();
  }

  public List<BasicOrderableDto> findAll(RequestParameters parameters) {
    return getPage(parameters).getContent();
  }


  /**
   * Returns the number of packs of product based on a given data.
   *
   * @param dispensingUnits # of dispensing units.
   * @param netContent # of products per package.
   * @param packRoundingThreshold A threshold value for rounding up to the next pack when
   *                              the remainder exceeds this value.
   * @param roundToZero A boolean indicating whether to round down to zero when no
   *                    full packs are needed.
   * @return The calculated number of packs.
   */
  public static long calculatePacks(long dispensingUnits, long netContent,
      long packRoundingThreshold, boolean roundToZero) {
    if (dispensingUnits <= 0 || netContent == 0) {
      return 0;
    }

    long packsToOrder = dispensingUnits / netContent;
    long remainderQuantity = dispensingUnits % netContent;

    if (remainderQuantity > 0 && remainderQuantity > packRoundingThreshold) {
      packsToOrder += 1;
    }

    if (packsToOrder == 0 && !roundToZero) {
      packsToOrder = 1;
    }

    return packsToOrder;
  }

}
