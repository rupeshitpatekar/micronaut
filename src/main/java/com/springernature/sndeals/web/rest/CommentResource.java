package com.springernature.sndeals.web.rest;

import com.springernature.sndeals.service.CommentService;
import com.springernature.sndeals.web.rest.errors.BadRequestAlertException;
import com.springernature.sndeals.service.dto.CommentDTO;
import com.springernature.sndeals.service.dto.CommentCriteria;
import com.springernature.sndeals.service.CommentQueryService;

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
 * REST controller for managing {@link com.springernature.sndeals.domain.Comment}.
 */
@Controller("/api")
public class CommentResource {

    private final Logger log = LoggerFactory.getLogger(CommentResource.class);

    private static final String ENTITY_NAME = "comment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CommentService commentService;

    private final CommentQueryService commentQueryService;

    public CommentResource(CommentService commentService, CommentQueryService commentQueryService) {
        this.commentService = commentService;
        this.commentQueryService = commentQueryService;
    }

    /**
     * {@code POST  /comments} : Create a new comment.
     *
     * @param commentDTO the commentDTO to create.
     * @return the {@link HttpResponse} with status {@code 201 (Created)} and with body the new commentDTO, or with status {@code 400 (Bad Request)} if the comment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Post("/comments")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<CommentDTO> createComment(@Body CommentDTO commentDTO) throws URISyntaxException {
        log.debug("REST request to save Comment : {}", commentDTO);
        if (commentDTO.getId() != null) {
            throw new BadRequestAlertException("A new comment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CommentDTO result = commentService.save(commentDTO);
        URI location = new URI("/api/comments/" + result.getId());
        return HttpResponse.created(result).headers(headers -> {
            headers.location(location);
            HeaderUtil.createEntityCreationAlert(headers, applicationName, true, ENTITY_NAME, result.getId().toString());
        });
    }

    /**
     * {@code PUT  /comments} : Updates an existing comment.
     *
     * @param commentDTO the commentDTO to update.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and with body the updated commentDTO,
     * or with status {@code 400 (Bad Request)} if the commentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the commentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Put("/comments")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<CommentDTO> updateComment(@Body CommentDTO commentDTO) throws URISyntaxException {
        log.debug("REST request to update Comment : {}", commentDTO);
        if (commentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CommentDTO result = commentService.update(commentDTO);
        return HttpResponse.ok(result).headers(headers ->
            HeaderUtil.createEntityUpdateAlert(headers, applicationName, true, ENTITY_NAME, commentDTO.getId().toString()));
    }

    /**
     * {@code GET  /comments} : get all the comments.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and the list of comments in body.
     */
    @Get("/comments")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<CommentDTO>> getAllComments(HttpRequest request, CommentCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Comments by criteria: {}", criteria);
        Page<CommentDTO> page = commentQueryService.findByCriteria(criteria, pageable);
        return HttpResponse.ok(page.getContent()).headers(headers ->
            PaginationUtil.generatePaginationHttpHeaders(headers, UriBuilder.of(request.getPath()), page));
    }

    /**
     * {@code GET  /comments/count} : count all the comments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and the count in body.
     */
    @Get("/comments/count")
    public HttpResponse<Long> countComments(CommentCriteria criteria) {
        log.debug("REST request to count Comments by criteria: {}", criteria);
        return HttpResponse.ok().body(commentQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /comments/:id} : get the "id" comment.
     *
     * @param id the id of the commentDTO to retrieve.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and with body the commentDTO, or with status {@code 404 (Not Found)}.
     */
    @Get("/comments/{id}")
    @ExecuteOn(TaskExecutors.IO)
    public Optional<CommentDTO> getComment(@PathVariable Long id) {
        log.debug("REST request to get Comment : {}", id);

        return commentService.findOne(id);
    }

    /**
     * {@code DELETE  /comments/:id} : delete the "id" comment.
     *
     * @param id the id of the commentDTO to delete.
     * @return the {@link HttpResponse} with status {@code 204 (NO_CONTENT)}.
     */
    @Delete("/comments/{id}")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse deleteComment(@PathVariable Long id) {
        log.debug("REST request to delete Comment : {}", id);
        commentService.delete(id);
        return HttpResponse.noContent().headers(headers -> HeaderUtil.createEntityDeletionAlert(headers, applicationName, true, ENTITY_NAME, id.toString()));
    }
}
