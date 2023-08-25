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
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public class BottomUpQuantificationRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<BottomUpQuantification> {

  @Autowired
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  private BottomUpQuantification draftedBuq;
  private BottomUpQuantification submittedBuq;
  private BottomUpQuantification authorizedBuq;
  private PageRequest pageable;

  @Override
  public CrudRepository<BottomUpQuantification, UUID> getRepository() {
    return bottomUpQuantificationRepository;
  }

  @Before
  public void setUp() {
    bottomUpQuantificationRepository.deleteAll();

    draftedBuq = generateInstance();
    submittedBuq = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED).build();
    authorizedBuq = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.AUTHORIZED).build();

    bottomUpQuantificationRepository.save(draftedBuq);
    bottomUpQuantificationRepository.save(submittedBuq);
    bottomUpQuantificationRepository.save(authorizedBuq);

    pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.ASC, "status");
  }

  @Test
  public void shouldFindAllBottomUpQuantificationsIfNotStatusSpecified() {
    searchAndCheckResults(draftedBuq, 3);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusSubmitted() {
    searchAndCheckResults(submittedBuq, 1, BottomUpQuantificationStatus.SUBMITTED);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusAuthorized() {
    searchAndCheckResults(authorizedBuq, 1, BottomUpQuantificationStatus.AUTHORIZED);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusDraft() {
    searchAndCheckResults(draftedBuq, 1, BottomUpQuantificationStatus.DRAFT);
  }

  @Test
  public void shouldFindOnlyBottomUpQuantificationsWithStatusSubmittedAndAuthorized() {
    searchAndCheckResults(submittedBuq, 2, BottomUpQuantificationStatus.SUBMITTED,
        BottomUpQuantificationStatus.AUTHORIZED);
  }

  @Override
  public BottomUpQuantification generateInstance() {
    return new BottomUpQuantificationDataBuilder()
        .buildAsNew();
  }

  private void searchAndCheckResults(BottomUpQuantification buq, int expectedSize,
      BottomUpQuantificationStatus... status) {
    BottomUpQuantificationSearchParams params = new TestSearchParams(status);
    List<BottomUpQuantification> foundBuqs = bottomUpQuantificationRepository
        .search(params, pageable)
        .getContent();
    assertThat(foundBuqs, hasSize(expectedSize));
    assertThat(foundBuqs, hasItem(hasProperty("status", equalTo(buq.getStatus()))));
  }

  @Getter
  private static final class TestSearchParams implements
      BottomUpQuantificationSearchParams {
    private final Set<BottomUpQuantificationStatus> statuses;

    TestSearchParams(BottomUpQuantificationStatus... buqStatuses) {
      statuses = Sets.newHashSet(buqStatuses);
    }

    @Override
    public boolean isEmpty() {
      return statuses.isEmpty();
    }

  }

}
