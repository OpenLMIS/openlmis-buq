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
import org.openlmis.buq.builder.ForecastingSettingDataBuilder;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
public class ForecastingSettingDtoTest {

  private static final String WASTAGE_PERCENTAGE = "wastagePercentage";
  private static final String BUFFER_MONTHS = "bufferMonths";

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
      .getValidator();

  private final ForecastingSettingDto dto = ForecastingSettingDto
      .newInstance(new ForecastingSettingDataBuilder()
          .withPopulationSegment(new PopulationSegmentDataBuilder().build())
          .build());

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ForecastingSettingDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(ForecastingSettingDto.class, new ForecastingSettingDto());
  }

  @Test
  public void shouldAcceptValidSetting() {
    assertThat(invalidFields()).isEmpty();
  }

  @Test
  public void shouldRefuseZeroDosesPerTarget() {
    dto.setDosesPerTarget(0);
    assertThat(invalidFields()).containsExactly("dosesPerTarget");
  }

  @Test
  public void shouldRefuseCoverageAbove100() {
    dto.setCoveragePercentage(new BigDecimal("100.01"));
    assertThat(invalidFields()).containsExactly("coveragePercentage");
  }

  @Test
  public void shouldRefuseWastageOf100() {
    dto.setWastagePercentage(new BigDecimal("100"));
    assertThat(invalidFields()).containsExactly(WASTAGE_PERCENTAGE);
  }

  @Test
  public void shouldAcceptWastageJustBelow100() {
    dto.setWastagePercentage(new BigDecimal("99.99"));
    assertThat(invalidFields()).isEmpty();
  }

  @Test
  public void shouldRefuseNegativeWastage() {
    dto.setWastagePercentage(new BigDecimal("-0.01"));
    assertThat(invalidFields()).containsExactly(WASTAGE_PERCENTAGE);
  }

  @Test
  public void shouldRefuseBufferAbove12Months() {
    dto.setBufferMonths(new BigDecimal("12.01"));
    assertThat(invalidFields()).containsExactly(BUFFER_MONTHS);
  }

  @Test
  public void shouldAcceptZeroAndMissingBuffer() {
    dto.setBufferMonths(BigDecimal.ZERO);
    assertThat(invalidFields()).isEmpty();
    dto.setBufferMonths(null);
    assertThat(invalidFields()).isEmpty();
  }

  @Test
  public void shouldRefuseBufferWithThreeDecimals() {
    dto.setBufferMonths(new BigDecimal("0.125"));
    assertThat(invalidFields()).containsExactly(BUFFER_MONTHS);
  }

  @Test
  public void shouldRefuseMissingReferences() {
    dto.setProgramId(null);
    dto.setOrderableId(null);
    dto.setPopulationSegmentId(null);
    assertThat(invalidFields())
        .containsExactlyInAnyOrder("programId", "orderableId", "populationSegmentId");
  }

  private Set<String> invalidFields() {
    return VALIDATOR.validate(dto).stream()
        .map(violation -> violation.getPropertyPath().toString())
        .collect(Collectors.toSet());
  }

}
