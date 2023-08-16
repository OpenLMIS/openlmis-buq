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

package org.openlmis.buq.builder;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.dto.referencedata.MetadataDto;

@SuppressWarnings("PMD.TooManyMethods")
public class OrderableDtoDataBuilder implements DtoDataBuilder<BasicOrderableDto> {

  private static int instanceNumber = 0;

  private UUID id;
  private Long versionNumber;
  private ZonedDateTime lastUpdated;
  private String productCode;
  private String fullProductName;
  private long netContent;
  private long packRoundingThreshold;
  private boolean roundToZero;

  /**
   * Creates builder for creating new instance of {@link OrderableDtoDataBuilder}.
   */
  public OrderableDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    versionNumber = 1L;
    lastUpdated = ZonedDateTime.now();
    productCode = "P" + instanceNumber;
    fullProductName = "Product " + instanceNumber;
    netContent = 10;
    packRoundingThreshold = 1;
    roundToZero = true;
  }

  public OrderableDtoDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public OrderableDtoDataBuilder withVersionNumber(Long versionNumber) {
    this.versionNumber = versionNumber;
    return this;
  }

  public OrderableDtoDataBuilder withNetContent(long netContent) {
    this.netContent = netContent;
    return this;
  }

  public OrderableDtoDataBuilder withFullProductName(String fullProductName) {
    this.fullProductName = fullProductName;
    return this;
  }

  public OrderableDtoDataBuilder withPackRoundingThreshold(Integer packRoundingThreshold) {
    this.packRoundingThreshold = packRoundingThreshold;
    return this;
  }

  public OrderableDtoDataBuilder withRoundToZero(Boolean roundToZero) {
    this.roundToZero = roundToZero;
    return this;
  }

  /**
   * Creates new instance of {@link BasicOrderableDto} with properties.
   * @return created orderable.
   */
  @Override
  public BasicOrderableDto buildAsDto() {
    BasicOrderableDto dto = new BasicOrderableDto();
    dto.setId(id);
    dto.setProductCode(productCode);
    dto.setFullProductName(fullProductName);
    dto.setNetContent(netContent);
    dto.setPackRoundingThreshold(packRoundingThreshold);
    dto.setRoundToZero(roundToZero);
    dto.setMeta(new MetadataDto(versionNumber, lastUpdated));

    return dto;
  }

  public OrderableDtoDataBuilder withProductCode(String productCode) {
    this.productCode = productCode;
    return this;
  }

}
