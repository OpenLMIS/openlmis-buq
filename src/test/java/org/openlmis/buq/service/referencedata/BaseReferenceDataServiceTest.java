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

package org.openlmis.buq.service.referencedata;

import org.openlmis.buq.service.BaseCommunicationService;
import org.openlmis.buq.service.BaseCommunicationServiceTest;
import org.springframework.test.util.ReflectionTestUtils;

public abstract class BaseReferenceDataServiceTest<T> extends BaseCommunicationServiceTest<T> {

  protected final String serviceUrl = "http://localhost";

  @Override
  protected BaseReferenceDataService<T> prepareService() {
    BaseCommunicationService<T> service = super.prepareService();

    ReflectionTestUtils.setField(service, "referenceDataUrl", serviceUrl);

    return (BaseReferenceDataService<T>) service;
  }

}
