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

package org.openlmis.buq.domain.buq;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.buq.domain.BaseTimestampedEntity;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.dto.referencedata.SupplyLineDto;

@Entity
@TypeName("BottomUpQuantification")
@Table(name = "bottom_up_quantifications", schema = "buq")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BottomUpQuantification extends BaseTimestampedEntity {

  public static final String STATUS = "status";
  public static final String FACILITY_ID = "facilityId";
  public static final String PROGRAM_ID = "programId";
  public static final String SUPERVISORY_NODE_ID = "supervisoryNodeId";
  public static final String STATUS_CHANGES = "statusChanges";
  public static final String PROCESSING_PERIOD_ID = "processingPeriodId";

  @NotNull
  @Getter
  @Setter
  private UUID facilityId;

  @NotNull
  @Getter
  @Setter
  private UUID programId;

  @NotNull
  @Getter
  @Setter
  private UUID processingPeriodId;

  @NotNull
  @Getter
  @Setter
  private Integer targetYear;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private BottomUpQuantificationStatus status;

  @OneToMany(
      mappedBy = "bottomUpQuantification",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Getter
  @Setter
  private List<BottomUpQuantificationLineItem> bottomUpQuantificationLineItems;

  @OneToMany(
      mappedBy = "bottomUpQuantification",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Getter
  @Setter
  private List<BottomUpQuantificationStatusChange> statusChanges = new ArrayList<>();

  @OneToOne(mappedBy = "bottomUpQuantification",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Getter
  @Setter
  private BottomUpQuantificationFundingDetails fundingDetails;

  @Getter
  @Setter
  private UUID supervisoryNodeId;

  /**
   * Constructor.
   *
   * @param facilityId         id of the Facility
   * @param programId          id of the Program
   * @param processingPeriodId id of the ProcessingPeriod
   */
  public BottomUpQuantification(UUID facilityId, UUID programId, UUID processingPeriodId,
      Integer targetYear) {
    this.facilityId = facilityId;
    this.programId = programId;
    this.processingPeriodId = processingPeriodId;
    this.targetYear = targetYear;
    this.status = BottomUpQuantificationStatus.DRAFT;
  }

  /**
   * Creates new instance based on data from the importer.
   */
  public static BottomUpQuantification newInstance(Importer importer) {
    BottomUpQuantification buq = new BottomUpQuantification();

    buq.setCreatedDate(importer.getCreatedDate());
    buq.setModifiedDate(importer.getModifiedDate());
    buq.setFacilityId(importer.getFacilityId());
    buq.setProgramId(importer.getProgramId());
    buq.setProcessingPeriodId(importer.getProcessingPeriodId());
    buq.setTargetYear(importer.getTargetYear());
    buq.setStatus(importer.getStatus());

    return buq;
  }

  /**
   * Copy values of attributes into new or updated BottomUpQuantification.
   *
   * @param lineItems list of bottom-up quantification line items.
   */
  public void updateFrom(List<BottomUpQuantificationLineItem> lineItems) {
    if (lineItems != null) {
      final List<BottomUpQuantificationLineItem> newItems = new ArrayList<>(lineItems);
      final List<BottomUpQuantificationLineItem> removedItems = new ArrayList<>();

      for (BottomUpQuantificationLineItem existingItem : bottomUpQuantificationLineItems) {
        final Optional<BottomUpQuantificationLineItem> updatedItemOpt =
                removeSameItem(existingItem, newItems);

        if (updatedItemOpt.isPresent()) {
          existingItem.updateFrom(updatedItemOpt.get());
        } else {
          removedItems.add(existingItem);
        }
      }

      bottomUpQuantificationLineItems.addAll(newItems);
      bottomUpQuantificationLineItems.removeAll(removedItems);
    }
    setModifiedDate(ZonedDateTime.now());
  }

  private Optional<BottomUpQuantificationLineItem> removeSameItem(
      BottomUpQuantificationLineItem item,
      List<BottomUpQuantificationLineItem> items) {
    final Iterator<BottomUpQuantificationLineItem> updatedItemIterator =
            items.iterator();
    while (updatedItemIterator.hasNext()) {
      final BottomUpQuantificationLineItem updatedItem = updatedItemIterator.next();

      if (item.getId().equals(updatedItem.getId())) {
        updatedItemIterator.remove();
        return Optional.of(updatedItem);
      }
    }
    return Optional.empty();
  }


  /**
   * Check if the bottom-up quantification is post-submitted.
   *
   */
  public boolean isPostSubmitted() {
    return status.isPostSubmitted();
  }

  public boolean isApprovable() {
    return status.duringApproval();
  }

  /**
   * Approve a bottom-up quantification.
   *
   */
  public BottomUpQuantificationStatusChange approve(UUID parentNodeId,
      List<SupplyLineDto> supplyLines,
      UUID approver) {
    setApprovalStatus(supplyLines, parentNodeId);
    setModifiedDate(ZonedDateTime.now());
    BottomUpQuantificationStatusChange statusChange =
            BottomUpQuantificationStatusChange.newInstance(this, approver, this.getStatus());
    statusChanges.add(statusChange);
    return statusChange;
  }

  private void setApprovalStatus(List<SupplyLineDto> supplyLines, UUID parentNodeId) {
    if (supplyLines.isEmpty() && parentNodeId != null) {
      status = BottomUpQuantificationStatus.IN_APPROVAL;
      supervisoryNodeId = parentNodeId;
      return;
    }
    status = BottomUpQuantificationStatus.APPROVED;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setCreatedDate(getCreatedDate());
    exporter.setModifiedDate(getModifiedDate());
    exporter.setFacilityId(facilityId);
    exporter.setProgramId(programId);
    exporter.setProcessingPeriodId(processingPeriodId);
    exporter.setTargetYear(targetYear);
    exporter.setStatus(status);
    exporter.setFundingDetails(fundingDetails);
    exporter.setSupervisoryNodeId(supervisoryNodeId);
  }

  public interface Exporter extends BaseTimestampedExporter {

    void setFacilityId(UUID facilityId);

    void setProgramId(UUID programId);

    void setProcessingPeriodId(UUID processingPeriodId);

    void setTargetYear(Integer targetYear);

    void setStatus(BottomUpQuantificationStatus status);

    void setFundingDetails(BottomUpQuantificationFundingDetails fundingDetails);

    void setSupervisoryNodeId(UUID supervisoryNodeId);

  }

  public interface Importer extends BaseTimestampedImporter {

    UUID getFacilityId();

    UUID getProgramId();

    UUID getProcessingPeriodId();

    Integer getTargetYear();

    BottomUpQuantificationStatus getStatus();

    List<BottomUpQuantificationLineItemDto> getBottomUpQuantificationLineItems();

    UUID getSupervisoryNodeId();

  }

}
