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
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
public class PopulationSegmentDtoTest {

  private static final String POPULATION_PERCENTAGE = "populationPercentage";

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
      .getValidator();

  private final PopulationSegmentDto dto = PopulationSegmentDto
      .newInstance(new PopulationSegmentDataBuilder().build());

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(PopulationSegmentDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(PopulationSegmentDto.class, new PopulationSegmentDto());
  }

  @Test
  public void shouldAcceptValidSegment() {
    assertThat(invalidFields()).isEmpty();
  }

  @Test
  public void shouldRefuseBlankCode() {
    dto.setCode(" ");
    assertThat(invalidFields()).containsExactly("code");
  }

  @Test
  public void shouldRefuseCodeLongerThan50() {
    dto.setCode(StringUtils.repeat('C', 51));
    assertThat(invalidFields()).containsExactly("code");
  }

  @Test
  public void shouldRefuseBlankName() {
    dto.setName("");
    assertThat(invalidFields()).containsExactly("name");
  }

  @Test
  public void shouldRefuseNameLongerThan255() {
    dto.setName(StringUtils.repeat('N', 256));
    assertThat(invalidFields()).containsExactly("name");
  }

  @Test
  public void shouldRefuseMissingPercentage() {
    dto.setPopulationPercentage(null);
    assertThat(invalidFields()).containsExactly(POPULATION_PERCENTAGE);
  }

  @Test
  public void shouldRefuseNegativePercentage() {
    dto.setPopulationPercentage(new BigDecimal("-0.01"));
    assertThat(invalidFields()).containsExactly(POPULATION_PERCENTAGE);
  }

  @Test
  public void shouldRefusePercentageAbove100() {
    dto.setPopulationPercentage(new BigDecimal("100.01"));
    assertThat(invalidFields()).containsExactly(POPULATION_PERCENTAGE);
  }

  @Test
  public void shouldRefusePercentageWithThreeDecimals() {
    dto.setPopulationPercentage(new BigDecimal("3.505"));
    assertThat(invalidFields()).containsExactly(POPULATION_PERCENTAGE);
  }

  @Test
  public void shouldAcceptBoundaryPercentages() {
    dto.setPopulationPercentage(BigDecimal.ZERO);
    assertThat(invalidFields()).isEmpty();
    dto.setPopulationPercentage(new BigDecimal("100.00"));
    assertThat(invalidFields()).isEmpty();
  }

  private Set<String> invalidFields() {
    return VALIDATOR.validate(dto).stream()
        .map(violation -> violation.getPropertyPath().toString())
        .collect(Collectors.toSet());
  }

}
