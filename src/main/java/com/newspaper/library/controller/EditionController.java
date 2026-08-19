package com.newspaper.library.controller;

import com.newspaper.library.dto.common.GenericResponse;
import com.newspaper.library.dto.edition.*;
import com.newspaper.library.service.EditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Edition management controller.
 */
@Slf4j
@RestController
@RequestMapping("/editions")
@RequiredArgsConstructor
@Tag(name = "Editions", description = "Newspaper edition management")
public class EditionController {

  private final EditionService editionService;

  @Operation(
          summary = "Create edition (Admin)",
          security = @SecurityRequirement(name = "Bearer Authentication"),
          responses = {
                  @ApiResponse(responseCode = "201", description = "Edition created"),
                  @ApiResponse(responseCode = "401", description = "Unauthorized"),
                  @ApiResponse(responseCode = "409", description = "Duplicate")
          }
  )
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GenericResponse<EditionResponse> createEdition(@Valid @RequestBody CreateEditionRequest request) {
    log.info("POST /api/v1/editions - Creating: {}", request.getName());
    EditionResponse data = editionService.createEdition(request);
    return GenericResponse.success(201, data, "Edition created successfully");
  }

  @Operation(
          summary = "Update edition (Admin)",
          security = @SecurityRequirement(name = "Bearer Authentication"),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Edition updated"),
                  @ApiResponse(responseCode = "404", description = "Not found")
          }
  )
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{editionId}")
  public GenericResponse<EditionResponse> updateEdition(
          @PathVariable Long editionId,
          @Valid @RequestBody UpdateEditionRequest request) {
    log.info("PUT /api/v1/editions/{}", editionId);
    EditionResponse data = editionService.updateEdition(editionId, request);
    return GenericResponse.success(data, "Edition updated successfully");
  }

  @Operation(
          summary = "Delete edition (Admin)",
          security = @SecurityRequirement(name = "Bearer Authentication"),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Edition deleted"),
                  @ApiResponse(responseCode = "404", description = "Not found")
          }
  )
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{editionId}")
  public GenericResponse<Void> deleteEdition(@PathVariable Long editionId) {
    log.info("DELETE /api/v1/editions/{}", editionId);
    editionService.deleteEdition(editionId);
    return GenericResponse.success(null, "Edition deleted successfully");
  }

  @Operation(summary = "List all editions")
  @GetMapping
  public GenericResponse<EditionListResponse> getAllEditions() {
    log.info("GET /api/v1/editions");
    EditionListResponse data = editionService.getAllEditions();
    return GenericResponse.success(data, "Editions retrieved successfully");
  }

  @Operation(summary = "Get edition by ID")
  @GetMapping("/{editionId}")
  public GenericResponse<EditionResponse> getEditionById(@PathVariable Long editionId) {
    log.info("GET /api/v1/editions/{}", editionId);
    EditionResponse data = editionService.getEditionById(editionId);
    return GenericResponse.success(data, "Edition retrieved successfully");
  }

  @Operation(summary = "Get edition by slug")
  @GetMapping("/slug/{slug}")
  public GenericResponse<EditionResponse> getEditionBySlug(@PathVariable String slug) {
    log.info("GET /api/v1/editions/slug/{}", slug);
    EditionResponse data = editionService.getEditionBySlug(slug);
    return GenericResponse.success(data, "Edition retrieved successfully");
  }
}
