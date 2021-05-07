package com.springernature.sndeals.web.rest;

import com.springernature.sndeals.service.PostService;
import com.springernature.sndeals.web.rest.errors.BadRequestAlertException;
import com.springernature.sndeals.service.dto.PostDTO;
import com.springernature.sndeals.service.dto.PostCriteria;
import com.springernature.sndeals.service.PostQueryService;

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
 * REST controller for managing {@link com.springernature.sndeals.domain.Post}.
 */
@Controller("/api")
public class PostResource {

    private final Logger log = LoggerFactory.getLogger(PostResource.class);

    private static final String ENTITY_NAME = "post";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PostService postService;

    private final PostQueryService postQueryService;

    public PostResource(PostService postService, PostQueryService postQueryService) {
        this.postService = postService;
        this.postQueryService = postQueryService;
    }

    /**
     * {@code POST  /posts} : Create a new post.
     *
     * @param postDTO the postDTO to create.
     * @return the {@link HttpResponse} with status {@code 201 (Created)} and with body the new postDTO, or with status {@code 400 (Bad Request)} if the post has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @io.micronaut.http.annotation.Post("/posts")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<PostDTO> createPost(@Body PostDTO postDTO) throws URISyntaxException {
        log.debug("REST request to save Post : {}", postDTO);
        if (postDTO.getId() != null) {
            throw new BadRequestAlertException("A new post cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PostDTO result = postService.save(postDTO);
        URI location = new URI("/api/posts/" + result.getId());
        return HttpResponse.created(result).headers(headers -> {
            headers.location(location);
            HeaderUtil.createEntityCreationAlert(headers, applicationName, true, ENTITY_NAME, result.getId().toString());
        });
    }

    /**
     * {@code PUT  /posts} : Updates an existing post.
     *
     * @param postDTO the postDTO to update.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and with body the updated postDTO,
     * or with status {@code 400 (Bad Request)} if the postDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the postDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Put("/posts")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<PostDTO> updatePost(@Body PostDTO postDTO) throws URISyntaxException {
        log.debug("REST request to update Post : {}", postDTO);
        if (postDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        PostDTO result = postService.update(postDTO);
        return HttpResponse.ok(result).headers(headers ->
            HeaderUtil.createEntityUpdateAlert(headers, applicationName, true, ENTITY_NAME, postDTO.getId().toString()));
    }

    /**
     * {@code GET  /posts} : get all the posts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and the list of posts in body.
     */
    @Get("/posts")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<PostDTO>> getAllPosts(HttpRequest request, PostCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Posts by criteria: {}", criteria);
        Page<PostDTO> page = postQueryService.findByCriteria(criteria, pageable);
        return HttpResponse.ok(page.getContent()).headers(headers ->
            PaginationUtil.generatePaginationHttpHeaders(headers, UriBuilder.of(request.getPath()), page));
    }

    /**
     * {@code GET  /posts/count} : count all the posts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and the count in body.
     */
    @Get("/posts/count")
    public HttpResponse<Long> countPosts(PostCriteria criteria) {
        log.debug("REST request to count Posts by criteria: {}", criteria);
        return HttpResponse.ok().body(postQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /posts/:id} : get the "id" post.
     *
     * @param id the id of the postDTO to retrieve.
     * @return the {@link HttpResponse} with status {@code 200 (OK)} and with body the postDTO, or with status {@code 404 (Not Found)}.
     */
    @Get("/posts/{id}")
    @ExecuteOn(TaskExecutors.IO)
    public Optional<PostDTO> getPost(@PathVariable Long id) {
        log.debug("REST request to get Post : {}", id);

        return postService.findOne(id);
    }

    /**
     * {@code DELETE  /posts/:id} : delete the "id" post.
     *
     * @param id the id of the postDTO to delete.
     * @return the {@link HttpResponse} with status {@code 204 (NO_CONTENT)}.
     */
    @Delete("/posts/{id}")
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse deletePost(@PathVariable Long id) {
        log.debug("REST request to delete Post : {}", id);
        postService.delete(id);
        return HttpResponse.noContent().headers(headers -> HeaderUtil.createEntityDeletionAlert(headers, applicationName, true, ENTITY_NAME, id.toString()));
    }
}
