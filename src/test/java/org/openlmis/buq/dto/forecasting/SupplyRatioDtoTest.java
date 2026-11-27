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

package org.openlmis.buq.dto.forecasting;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.SupplyRatioDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyRatioDtoTest {

  private static final String RATIO = "ratio";

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
      .getValidator();

  private final SupplyRatioDto dto = SupplyRatioDto
      .newInstance(new SupplyRatioDataBuilder().build());

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(SupplyRatioDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(SupplyRatioDto.class, new SupplyRatioDto());
  }

  @Test
  public void shouldAcceptValidRatio() {
    assertThat(invalidFields()).isEmpty();
  }

  @Test
  public void shouldRefuseZeroRatio() {
    dto.setRatio(BigDecimal.ZERO);
    assertThat(invalidFields()).containsExactly(RATIO);
  }

  @Test
  public void shouldAcceptRatioOf100() {
    dto.setRatio(new BigDecimal("100"));
    assertThat(invalidFields()).isEmpty();
  }

  @Test
  public void shouldRefuseRatioAbove100() {
    dto.setRatio(new BigDecimal("100.0001"));
    assertThat(invalidFields()).containsExactly(RATIO);
  }

  @Test
  public void shouldRefuseRatioWithFiveDecimals() {
    dto.setRatio(new BigDecimal("1.12345"));
    assertThat(invalidFields()).containsExactly(RATIO);
  }

  @Test
  public void shouldRefuseMissingRatio() {
    dto.setRatio(null);
    assertThat(invalidFields()).containsExactly(RATIO);
  }

  @Test
  public void shouldRefuseMissingType() {
    dto.setType(null);
    assertThat(invalidFields()).containsExactly("type");
  }

  @Test
  public void shouldRefuseMissingOrderables() {
    dto.setVaccineOrderableId(null);
    dto.setSupplyOrderableId(null);
    assertThat(invalidFields())
        .containsExactlyInAnyOrder("vaccineOrderableId", "supplyOrderableId");
  }

  private Set<String> invalidFields() {
    return VALIDATOR.validate(dto).stream()
        .map(violation -> violation.getPropertyPath().toString())
        .collect(Collectors.toSet());
  }

}
