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

package org.openlmis.buq.web.productgroup;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.buq.domain.productgroup.ProductGroup;
import org.openlmis.buq.dto.productgroup.ProductGroupDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.productgroup.ProductGroupRepository;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose Product groups via HTTP.
 */
@Controller
@RequestMapping(ProductGroupController.RESOURCE_PATH)
@Transactional
public class ProductGroupController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductGroupController.class);

  public static final String RESOURCE_PATH = API_PATH + "/productGroups";

  @Autowired
  private ProductGroupRepository productGroupRepository;

  /**
   * Allows the creation of a new product group. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ProductGroupDto createProductGroup(@RequestBody ProductGroupDto productGroup) {
    LOGGER.debug("Creating new product group with code {}", productGroup.getCode());
    ProductGroup newProductGroup = ProductGroup.newInstance(productGroup);
    newProductGroup.setId(null);
    newProductGroup = productGroupRepository.save(newProductGroup);

    return ProductGroupDto.newInstance(newProductGroup);
  }

  /**
   * Updates the specified product group.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ProductGroupDto saveProductGroup(@PathVariable("id") UUID id,
                                          @RequestBody ProductGroupDto productGroup) {
    if (null != productGroup.getId() && !Objects.equals(productGroup.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_PRODUCT_GROUP_ID_MISMATCH);
    }

    LOGGER.debug("Updating product group with code {}", productGroup.getCode());
    ProductGroup db;
    Optional<ProductGroup> productGroupOptional = productGroupRepository.findById(id);
    if (productGroupOptional.isPresent()) {
      db = productGroupOptional.get();
      db.updateFrom(productGroup);
    } else {
      db = ProductGroup.newInstance(productGroup);
      db.setId(id);
    }

    productGroupRepository.save(db);

    return ProductGroupDto.newInstance(db);
  }

  /**
   * Deletes the specified product group.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProductGroup(@PathVariable("id") UUID id) {
    if (!productGroupRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_PRODUCT_GROUP_NOT_FOUND);
    }

    productGroupRepository.deleteById(id);
  }

  /**
   * Retrieves all product groups. Note that an empty collection rather than a 404 should be
   * returned if no product groups exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ProductGroupDto> getAllProductGroups(Pageable pageable) {
    Page<ProductGroup> page = productGroupRepository.findAll(pageable);
    List<ProductGroupDto> content = page
        .getContent()
        .stream()
        .map(ProductGroupDto::newInstance)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified product group.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ProductGroupDto getSpecifiedProductGroup(@PathVariable("id") UUID id) {
    ProductGroup productGroup = productGroupRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_PRODUCT_GROUP_NOT_FOUND));

    return ProductGroupDto.newInstance(productGroup);
  }

  /**
   * Retrieves audit information related to the specified product group.
   *
   * @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @GetMapping(value = "/{id}/auditLog")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getSourceOfFundAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
      String changedPropertyName, Pageable page) {

    // Return a 404 if the specified instance can't be found
    if (!productGroupRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_PRODUCT_GROUP_NOT_FOUND);
    }

    return getAuditLogResponse(ProductGroup.class, id, author, changedPropertyName, page);
  }

}
