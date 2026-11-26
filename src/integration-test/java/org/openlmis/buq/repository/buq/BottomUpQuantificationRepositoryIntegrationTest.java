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

package org.openlmis.buq.repository.buq;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings("PMD.TooManyMethods")
public class BottomUpQuantificationRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<BottomUpQuantification> {

  @Autowired
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  private BottomUpQuantification draftedBuq;
  private BottomUpQuantification submittedBuq;
  private BottomUpQuantification authorizedBuq;
  private PageRequest pageable;
  private final UUID facility = UUID.randomUUID();

  @Override
  public CrudRepository<BottomUpQuantification, UUID> getRepository() {
    return bottomUpQuantificationRepository;
  }

  @Before
  public void setUp() {
    bottomUpQuantificationRepository.deleteAll();

    draftedBuq = generateInstance();
    submittedBuq = new BottomUpQuantificationDataBuilder()
        .withFacilityId(facility)
        .withStatus(BottomUpQuantificationStatus.SUBMITTED).build();
    authorizedBuq = new BottomUpQuantificationDataBuilder()
        .withFacilityId(facility)
        .withStatus(BottomUpQuantificationStatus.AUTHORIZED).build();

    bottomUpQuantificationRepository.save(draftedBuq);
    bottomUpQuantificationRepository.save(submittedBuq);
    bottomUpQuantificationRepository.save(authorizedBuq);

    pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.ASC, "status");
  }

  @Test
  public void shouldFindAllBottomUpQuantificationsIfNotStatusSpecified() {
    searchAndCheckResults(draftedBuq, 3, null);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusSubmitted() {
    searchAndCheckResults(submittedBuq, 1, null, BottomUpQuantificationStatus.SUBMITTED);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusAuthorized() {
    searchAndCheckResults(authorizedBuq, 1, null, BottomUpQuantificationStatus.AUTHORIZED);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusDraft() {
    searchAndCheckResults(draftedBuq, 1, null, BottomUpQuantificationStatus.DRAFT);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusSubmittedAndAuthorized() {
    searchAndCheckResults(submittedBuq, 2, null, BottomUpQuantificationStatus.SUBMITTED,
        BottomUpQuantificationStatus.AUTHORIZED);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithGivenFacilityId() {
    searchAndCheckResults(submittedBuq, 2, facility);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsOfGivenProgramFacilityPairs() {
    UUID program = UUID.randomUUID();
    UUID otherFacility = UUID.randomUUID();
    final BottomUpQuantification visible = saveBuq(program, facility);
    final BottomUpQuantification visibleAtOtherFacility = saveBuq(program, otherFacility);
    saveBuq(program, UUID.randomUUID());
    saveBuq(UUID.randomUUID(), facility);
    Set<Pair<UUID, UUID>> pairs = Sets.newHashSet(
        new ImmutablePair<>(program, facility), new ImmutablePair<>(program, otherFacility));

    Page<BottomUpQuantification> firstPage = bottomUpQuantificationRepository
        .search(new TestSearchParams(null), pairs, PageRequest.of(0, 1));
    List<BottomUpQuantification> all = bottomUpQuantificationRepository
        .search(new TestSearchParams(null), pairs, pageable)
        .getContent();

    assertEquals(2, firstPage.getTotalElements());
    assertThat(firstPage.getContent(), hasSize(1));
    assertEquals(Sets.newHashSet(visible.getId(), visibleAtOtherFacility.getId()),
        all.stream().map(BottomUpQuantification::getId).collect(Collectors.toSet()));
  }

  @Test
  public void shouldApplySearchParamsWithinProgramFacilityPairs() {
    UUID program = UUID.randomUUID();
    saveBuq(program, facility);
    Set<Pair<UUID, UUID>> pairs = Collections.singleton(new ImmutablePair<>(program, facility));

    List<BottomUpQuantification> found = bottomUpQuantificationRepository
        .search(new TestSearchParams(facility, BottomUpQuantificationStatus.SUBMITTED), pairs,
            pageable)
        .getContent();

    assertThat(found, hasSize(0));
  }

  @Test
  public void shouldFindNothingForNoProgramFacilityPairs() {
    Page<BottomUpQuantification> page = bottomUpQuantificationRepository
        .search(new TestSearchParams(null), Collections.emptySet(), pageable);

    assertEquals(0, page.getTotalElements());
    assertThat(page.getContent(), hasSize(0));
  }

  private BottomUpQuantification saveBuq(UUID program, UUID facilityId) {
    return bottomUpQuantificationRepository.save(new BottomUpQuantificationDataBuilder()
        .withProgramId(program)
        .withFacilityId(facilityId)
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .buildAsNew());
  }

  @Override
  public BottomUpQuantification generateInstance() {
    return new BottomUpQuantificationDataBuilder()
        .buildAsNew();
  }

  private void searchAndCheckResults(BottomUpQuantification buq, int expectedSize, UUID facility,
      BottomUpQuantificationStatus... status) {
    BottomUpQuantificationSearchParams params = new TestSearchParams(facility, status);
    List<BottomUpQuantification> foundBuqs = bottomUpQuantificationRepository
        .search(params, pageable)
        .getContent();
    assertThat(foundBuqs, hasSize(expectedSize));
    assertThat(foundBuqs, hasItem(hasProperty("status", equalTo(buq.getStatus()))));
  }

  @Getter
  @Setter
  private static final class TestSearchParams implements
      BottomUpQuantificationSearchParams {
    private final Set<BottomUpQuantificationStatus> statuses;
    private UUID facility;

    TestSearchParams(UUID facilityParam, BottomUpQuantificationStatus... buqStatuses) {
      facility = facilityParam;
      statuses = Sets.newHashSet(buqStatuses);
    }

    @Override
    public boolean isEmpty() {
      return statuses.isEmpty();
    }

  }

}
