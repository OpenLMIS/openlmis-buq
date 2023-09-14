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

package org.openlmis.buq.web.buq;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_INVALID_PARAMETER_BOTTOM_UP_QUANTIFICATION_STATUS;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_INVALID_SEARCH_PARAMS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.util.Message;
import org.openlmis.buq.web.SearchParams;
import org.springframework.util.MultiValueMap;

@EqualsAndHashCode
@ToString
final class QueryBottomUpQuantificationSearchParams implements
    BottomUpQuantificationSearchParams {

  private static final String STATUS = "status";
  private static final String FACILITY = "facility";

  private static final List<String> ALL_PARAMETERS = asList(STATUS, FACILITY);

  private SearchParams queryParams;

  /**
   * Wraps map of query params into an object.
   */
  QueryBottomUpQuantificationSearchParams(MultiValueMap<String, String> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets {@link BottomUpQuantificationStatus} for "bottomUpQuantificationStatus" key from params.
   *
   * @return Enum value of Bottom-up quantification status
   *          or null if params doesn't contain "bottomUpQuantificationStatus" key.
   */
  @Override
  public Set<BottomUpQuantificationStatus> getStatuses() {
    if (!queryParams.containsKey(STATUS)) {
      return Collections.emptySet();
    }
    Collection<String> values = queryParams.get(STATUS);
    try {
      return values.stream()
          .map(BottomUpQuantificationStatus::valueOf)
          .collect(toSet());
    } catch (IllegalArgumentException cause) {
      throw new ValidationMessageException(cause,
          new Message(ERROR_INVALID_PARAMETER_BOTTOM_UP_QUANTIFICATION_STATUS, values));
    }
  }

  /**
   * Gets {@link UUID} for "facility" key from params.
   *
   * @return UUID value of facility id or null if params doesn't contain "facility" key.
   */
  @Override
  public UUID getFacility() {
    if (!queryParams.containsKey(FACILITY)) {
      return null;
    }
    return queryParams.getUuid(FACILITY);
  }

  /**
   * Checks if query params are . Returns false if any provided param is not on supported list.
   */
  public boolean isEmpty() {
    return queryParams.isEmpty();
  }

  /**
   * Checks if query params are valid. Returns false if any provided param is not on supported list.
   */
  public void validate() {
    if (!ALL_PARAMETERS.containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_SEARCH_PARAMS));
    }
  }

}
