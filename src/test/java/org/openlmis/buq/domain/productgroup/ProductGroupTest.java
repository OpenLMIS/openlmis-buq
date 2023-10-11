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

package org.openlmis.buq.domain.productgroup;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.ProductGroupDataBuilder;
import org.openlmis.buq.dto.productgroup.ProductGroupDto;

public class ProductGroupTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ProductGroup.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ProductGroup productGroup = new ProductGroupDataBuilder().build();
    ToStringTestUtils.verify(ProductGroup.class, productGroup, "TEXT");
  }

  @Test
  public void shouldCreateNewInstance() {
    ProductGroup productGroup = new ProductGroupDataBuilder().build();
    ProductGroupDto dto = ProductGroupDto.newInstance(productGroup);

    ProductGroup newSourceOfFund = ProductGroup.newInstance(dto);

    assertThat(newSourceOfFund).isEqualTo(productGroup);
  }

  @Test
  public void shouldUpdateFrom() {
    ProductGroup productGroup = new ProductGroupDataBuilder().build();
    ProductGroupDto dto = ProductGroupDto.newInstance(productGroup);
    dto.setName("pg-name");
    dto.setCode("pg-code");

    productGroup.updateFrom(dto);

    assertThat(productGroup.getName()).isEqualTo("pg-name");
    assertThat(productGroup.getCode()).isEqualTo("pg-code");
  }

  @Test
  public void shouldExportData() {
    ProductGroup productGroup = new ProductGroupDataBuilder().build();
    ProductGroupDto dto = new ProductGroupDto();

    productGroup.export(dto);

    assertThat(dto.getId()).isEqualTo(productGroup.getId());
    assertThat(dto.getName()).isEqualTo(productGroup.getName());
    assertThat(dto.getCode()).isEqualTo(productGroup.getCode());
  }

}