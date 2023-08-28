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

package org.openlmis.buq.errorhandling;

import java.util.HashMap;
import java.util.Map;
import org.openlmis.buq.exception.BindingResultException;
import org.openlmis.buq.util.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BottomUpQuantificationErrorHandling extends AbstractErrorHandling {
  /**
   * Handles {@link BindingResultException} exceptions.base
   */
  @ExceptionHandler(BindingResultException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Map<String, Message.LocalizedMessage> handleBindingResultException(
          BindingResultException ex) {
    Map<String, Message.LocalizedMessage> errors = new HashMap<>();

    ex.getErrors()
            .forEach((field, message) -> errors.put(field, getLocalizedMessage(message)));

    return errors;
  }
}
