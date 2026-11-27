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

package org.openlmis.buq.domain.forecasting;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseEntity;

@Entity
@Table(name = "forecast_status_changes")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForecastStatusChange extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "forecastId", nullable = false)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Forecast forecast;

  @NotNull
  private UUID authorId;

  @Column(columnDefinition = "timestamp with time zone")
  private ZonedDateTime occurredDate;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ForecastStatus status;

  /**
   * Creates a status change of the forecast to the given status, made by the given user.
   */
  public static ForecastStatusChange newInstance(Forecast forecast, UUID authorId,
      ForecastStatus status) {
    ForecastStatusChange statusChange = new ForecastStatusChange();
    statusChange.setForecast(forecast);
    statusChange.setAuthorId(authorId);
    statusChange.setStatus(status);
    return statusChange;
  }

  @PrePersist
  private void prePersist() {
    this.occurredDate = ZonedDateTime.now();
  }

}
