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

import java.util.UUID;
import org.openlmis.buq.domain.Remark;

public class RemarkDataBuilder {

  private final UUID id = UUID.randomUUID();
  private String name = "name";
  private String description = "remark-description";

  public RemarkDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public RemarkDataBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Builds new instance of SourceOfFund (with id field).
   */
  public Remark build() {
    Remark remark = buildAsNew();
    remark.setId(id);

    return remark;
  }

  /**
   * Builds new instance of SourceOfFund as a new object (without id field).
   */
  public Remark buildAsNew() {
    Remark remark = new Remark();
    remark.setName(name);
    remark.setDescription(description);

    return remark;
  }
}
