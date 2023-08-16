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

package org.openlmis.buq.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.openlmis.buq.dto.csv.BottomUpQuantificationLineItemCsv;

public class CsvServiceTest {

  private static final String CONTENT =
      "\"Product Code\",\"Product Name\",\"Unit of measure\",\"Adjusted Consumption (in Packs)\"\n"
          + "TEST_A_1,\"Test product A name\",10,100\n"
          + "TEST_B_2,\"Test product B name\",20,200\n";

  private final CsvService csvService = new CsvService();

  @Test
  public void shouldWriteDataToCsvFile() throws Exception {
    List<BottomUpQuantificationLineItemCsv> data = provideCsvData();

    byte[] fileByte = csvService.generateCsv(data, BottomUpQuantificationLineItemCsv.class);
    byte[] convertedContent = CONTENT.getBytes(StandardCharsets.UTF_8);

    assertThat(fileByte).isEqualTo(convertedContent);
  }

  private List<BottomUpQuantificationLineItemCsv> provideCsvData() {
    return Stream.of(
        new BottomUpQuantificationLineItemCsv(
            "TEST_A_1",
            "Test product A name",
            10,
            100
        ),
        new BottomUpQuantificationLineItemCsv(
            "TEST_B_2",
            "Test product B name",
            20,
            200
        )
    ).collect(Collectors.toList());
  }
}
