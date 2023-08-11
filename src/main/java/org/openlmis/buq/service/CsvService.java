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

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvService {

  /**
   * Fills the provided CSV file with the data of type {@code T}.
   *
   * @param elements to be written in the CSV file
   * @param type     of the data being written
   * @return CSV file with data.
   * @throws IOException serializing errors occurred
   */
  public <T> byte[] generateCsv(List<T> elements, Class<T> type)
      throws IOException {
    CsvMapper csvMapper = new CsvMapper();
    csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
    CsvSchema csvSchema = csvMapper
        .schemaFor(type)
        .withHeader();
    ObjectWriter csvWriter = csvMapper.writer(csvSchema.withLineSeparator("\n"));
    return csvWriter.writeValueAsBytes(elements);
  }

}
