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

package org.openlmis.buq.repository.forecasting;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.buq.builder.SupplyRatioDataBuilder;
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.domain.forecasting.SupplyRatioType;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyRatioRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<SupplyRatio> {

  private static final Pageable PAGEABLE = PageRequest.of(0, 10);

  @Autowired
  private SupplyRatioRepository supplyRatioRepository;

  @Override
  public CrudRepository<SupplyRatio, UUID> getRepository() {
    return supplyRatioRepository;
  }

  @Override
  public SupplyRatio generateInstance() {
    return new SupplyRatioDataBuilder().buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowZeroRatio() {
    supplyRatioRepository.saveAndFlush(new SupplyRatioDataBuilder()
        .withRatio(BigDecimal.ZERO)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowSameVaccineAndSupply() {
    UUID orderableId = UUID.randomUUID();

    supplyRatioRepository.saveAndFlush(new SupplyRatioDataBuilder()
        .withVaccineOrderableId(orderableId)
        .withSupplyOrderableId(orderableId)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicatePair() {
    SupplyRatio ratio1 = generateInstance();
    SupplyRatio ratio2 = new SupplyRatioDataBuilder()
        .withVaccineOrderableId(ratio1.getVaccineOrderableId())
        .withSupplyOrderableId(ratio1.getSupplyOrderableId())
        .withType(SupplyRatioType.VIAL)
        .buildAsNew();

    supplyRatioRepository.saveAndFlush(ratio1);
    supplyRatioRepository.saveAndFlush(ratio2);
  }

  @Test
  public void shouldAllowBoxRatioOf0_01() {
    SupplyRatio saved = supplyRatioRepository.saveAndFlush(new SupplyRatioDataBuilder()
        .withType(SupplyRatioType.BOX)
        .withRatio(new BigDecimal("0.01"))
        .buildAsNew());

    SupplyRatio found = supplyRatioRepository.findById(saved.getId()).get();
    assertThat(found.getRatio()).isEqualByComparingTo("0.0100");
    assertThat(found.getType()).isEqualTo(SupplyRatioType.BOX);
  }

  @Test
  public void shouldSearchByVaccine() {
    SupplyRatio match = supplyRatioRepository.save(generateInstance());
    SupplyRatio other = supplyRatioRepository.save(generateInstance());

    Page<SupplyRatio> page = supplyRatioRepository.search(match.getVaccineOrderableId(), null,
        PAGEABLE);

    assertThat(page.getContent()).containsExactly(match);
    assertThat(page.getContent()).doesNotContain(other);
  }

  @Test
  public void shouldSearchBySupply() {
    SupplyRatio match = supplyRatioRepository.save(generateInstance());
    supplyRatioRepository.save(generateInstance());

    Page<SupplyRatio> page = supplyRatioRepository.search(null, match.getSupplyOrderableId(),
        PAGEABLE);

    assertThat(page.getContent()).containsExactly(match);
  }

  @Test
  public void shouldSearchByVaccineAndSupply() {
    UUID vaccineId = UUID.randomUUID();
    SupplyRatio match = supplyRatioRepository.save(new SupplyRatioDataBuilder()
        .withVaccineOrderableId(vaccineId).buildAsNew());
    supplyRatioRepository.save(new SupplyRatioDataBuilder()
        .withVaccineOrderableId(vaccineId).buildAsNew());

    Page<SupplyRatio> page = supplyRatioRepository.search(vaccineId,
        match.getSupplyOrderableId(), PAGEABLE);

    assertThat(page.getContent()).containsExactly(match);
    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void shouldReturnAllRatiosWithoutFilters() {
    SupplyRatio first = supplyRatioRepository.save(generateInstance());
    SupplyRatio second = supplyRatioRepository.save(generateInstance());

    Page<SupplyRatio> page = supplyRatioRepository.search(null, null, PAGEABLE);

    assertThat(page.getContent()).contains(first, second);
  }

  @Test
  public void shouldPageAndSortSearchResults() {
    UUID vaccineId = UUID.randomUUID();
    SupplyRatio small = supplyRatioRepository.save(new SupplyRatioDataBuilder()
        .withVaccineOrderableId(vaccineId).withRatio(new BigDecimal("0.0100")).buildAsNew());
    supplyRatioRepository.save(new SupplyRatioDataBuilder()
        .withVaccineOrderableId(vaccineId).withRatio(new BigDecimal("1.1100")).buildAsNew());

    Page<SupplyRatio> page = supplyRatioRepository.search(vaccineId, null,
        PageRequest.of(0, 1, Sort.by("ratio")));

    assertThat(page.getContent()).containsExactly(small);
    assertThat(page.getTotalElements()).isEqualTo(2);
  }

  @Test
  public void shouldPageUnsortedSearchInIdOrder() {
    UUID vaccineId = UUID.randomUUID();
    List<String> savedIds = saveRatiosFor(vaccineId, 5);

    assertThat(pageThroughOneByOne(vaccineId, Sort.unsorted()))
        .containsExactlyElementsOf(sortedAsPostgresOrdersUuids(savedIds));
  }

  @Test
  public void shouldBreakSortTiesById() {
    UUID vaccineId = UUID.randomUUID();
    List<String> savedIds = saveRatiosFor(vaccineId, 5);

    assertThat(pageThroughOneByOne(vaccineId, Sort.by("type")))
        .containsExactlyElementsOf(sortedAsPostgresOrdersUuids(savedIds));
  }

  private List<String> saveRatiosFor(UUID vaccineId, int count) {
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ids.add(supplyRatioRepository.save(new SupplyRatioDataBuilder()
          .withVaccineOrderableId(vaccineId).buildAsNew()).getId().toString());
    }
    return ids;
  }

  private List<String> pageThroughOneByOne(UUID vaccineId, Sort sort) {
    List<String> ids = new ArrayList<>();
    Page<SupplyRatio> page;
    int number = 0;
    do {
      page = supplyRatioRepository.search(vaccineId, null, PageRequest.of(number++, 1, sort));
      page.getContent().forEach(ratio -> ids.add(ratio.getId().toString()));
    } while (page.hasNext());
    return ids;
  }

  // Postgres compares uuids byte by byte, which is the order of their lowercase hex strings.
  private List<String> sortedAsPostgresOrdersUuids(List<String> ids) {
    List<String> sorted = new ArrayList<>(ids);
    Collections.sort(sorted);
    return sorted;
  }

}
