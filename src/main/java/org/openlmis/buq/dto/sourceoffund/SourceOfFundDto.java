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

package org.openlmis.buq.dto.sourceoffund;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;
import org.openlmis.buq.dto.BaseDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class SourceOfFundDto extends BaseDto implements SourceOfFund.Importer,
        SourceOfFund.Exporter {

  private String name;
  private String description;
  private boolean editable;

  /**
   * Creates new instance based on domain object.
   */
  public static SourceOfFundDto newInstance(SourceOfFund sourceOfFund) {
    SourceOfFundDto dto = new SourceOfFundDto();
    sourceOfFund.export(dto);

    return dto;
  }

}
