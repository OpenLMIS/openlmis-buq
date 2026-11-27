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

package org.openlmis.buq.service.role;

/**
 * Names the rights the forecasting endpoints check. The rights themselves are defined by the
 * referencedata service, and a check on a name it doesn't know fails for every user token.
 */
public final class ForecastingRights {

  public static final String PREPARE = "VACCINE_FORECAST_PREPARE";
  public static final String SUBMIT = "VACCINE_FORECAST_SUBMIT";
  public static final String AUTHORIZE = "VACCINE_FORECAST_AUTHORIZE";
  public static final String APPROVE = "VACCINE_FORECAST_APPROVE";
  public static final String MANAGE = "VACCINE_FORECAST_MANAGE";

  private ForecastingRights() {
  }

}
