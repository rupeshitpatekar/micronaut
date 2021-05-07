package com.springernature.sndeals.web.rest;

import com.springernature.sndeals.service.AttachmentService;
import com.springernature.sndeals.web.rest.errors.BadRequestAlertException;
import com.springernature.sndeals.service.dto.AttachmentDTO;
import com.springernature.sndeals.service.dto.AttachmentCriteria;
import com.springernature.sndeals.service.AttachmentQueryService;

import com.springernature.sndeals.util.HeaderUtil;
import com.springernature.sndeals.util.PaginationUtil;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.ReadOnly;




import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.springernature.sndeals.domain.Attachment}.
 */
@Controller("/api")
public class AttachmentResource {

    private final Logger log = LoggerFactory.getLogger(AttachmentResource.class);

    private static final String ENTITY_NAME = "attachment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AttachmentService attachmentService;

    private final AttachmentQueryService attachmentQueryService;

    public AttachmentResource(AttachmentService attachmentService, AttachmentQueryService attachmentQueryService) {
        this.attachmentService = attachmentService;
        this.attachmentQueryService = attachmentQueryService;
    }

    /**
     * {@code POST  /attachments} : Create a new attachment.
     *
     * @param attachmentDTO the attachmentDTO to create.
     * @return the {@link HttpResponse} with status {@code 201 (Created)} and with body the new attachmentDTO, or with status {@code 400 (Bad Request)} if the attachment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Post("/attachments")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<AttachmentDTO> createAttachment(@Body AttachmentDTO attachmentDTO) throws URISyntaxException {
        log.debug("REST request to save Attachment : {}", attachmentDTO);
        if (attachmentDTO.getId() != null) {
            throw new BadRequestAlertException("A new attachment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AttachmentDTO result = attachmentService.save(attachmentDTO);
        URI location = new URI("/api/attachments/" + result.getId());
        return HttpResponse.created(result).headers(headers -> {
            headers.location(location);
            HeaderUtil.createEntityCreationAlert(headers, applicationName, true, ENTITY_NAME, result.getId().toString());
        });
    }

    /**
     * {@code PUT  /attachments} : Updates an existing attachment.
     *
     * @param attachmentDTO the attachmentDTO to update.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and with body the updated attachmentDTO,
     * or with status {@code 400 (Bad Request)} if the attachmentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the attachmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Put("/attachments")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<AttachmentDTO> updateAttachment(@Body AttachmentDTO attachmentDTO) throws URISyntaxException {
        log.debug("REST request to update Attachment : {}", attachmentDTO);
        if (attachmentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AttachmentDTO result = attachmentService.update(attachmentDTO);
        return HttpResponse.ok(result).headers(headers ->
            HeaderUtil.createEntityUpdateAlert(headers, applicationName, true, ENTITY_NAME, attachmentDTO.getId().toString()));
    }

    /**
     * {@code GET  /attachments} : get all the attachments.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and the list of attachments in body.
     */
    @Get("/attachments")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<AttachmentDTO>> getAllAttachments(HttpRequest request, AttachmentCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Attachments by criteria: {}", criteria);
        Page<AttachmentDTO> page = attachmentQueryService.findByCriteria(criteria, pageable);
        return HttpResponse.ok(page.getContent()).headers(headers ->
            PaginationUtil.generatePaginationHttpHeaders(headers, UriBuilder.of(request.getPath()), page));
    }

    /**
     * {@code GET  /attachments/count} : count all the attachments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and the count in body.
     */
    @Get("/attachments/count")
    public HttpResponse<Long> countAttachments(AttachmentCriteria criteria) {
        log.debug("REST request to count Attachments by criteria: {}", criteria);
        return HttpResponse.ok().body(attachmentQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /attachments/:id} : get the "id" attachment.
     *
     * @param id the id of the attachmentDTO to retrieve.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and with body the attachmentDTO, or with status {@code 404 (Not Found)}.
     */
    @Get("/attachments/{id}")
    @ExecuteOn(TaskExecutors.IO)
    public Optional<AttachmentDTO> getAttachment(@PathVariable Long id) {
        log.debug("REST request to get Attachment : {}", id);

        return attachmentService.findOne(id);
    }

    /**
     * {@code DELETE  /attachments/:id} : delete the "id" attachment.
     *
     * @param id the id of the attachmentDTO to delete.
     * @return the {@link HttpResponse} with status {@code 204 (NO_CONTENT)}.
     */
    @Delete("/attachments/{id}")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse deleteAttachment(@PathVariable Long id) {
        log.debug("REST request to delete Attachment : {}", id);
        attachmentService.delete(id);
        return HttpResponse.noContent().headers(headers -> HeaderUtil.createEntityDeletionAlert(headers, applicationName, true, ENTITY_NAME, id.toString()));
    }
}
