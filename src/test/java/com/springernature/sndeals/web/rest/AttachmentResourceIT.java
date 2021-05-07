package com.springernature.sndeals.web.rest;


import com.springernature.sndeals.domain.Attachment;
import com.springernature.sndeals.domain.Post;
import com.springernature.sndeals.repository.AttachmentRepository;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionOperations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.sql.Connection;
import java.util.List;

import com.springernature.sndeals.service.dto.AttachmentDTO;
import com.springernature.sndeals.service.mapper.AttachmentMapper;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the {@Link AttachmentResource} REST controller.
 */
@MicronautTest(transactional = false)
@Property(name = "micronaut.security.enabled", value = "false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttachmentResourceIT {

    private static final String DEFAULT_FILE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FILE_NAME = "BBBBBBBBBB";

    private static final byte[] DEFAULT_CONTENT = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_CONTENT = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_CONTENT_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_CONTENT_CONTENT_TYPE = "image/png";

    @Inject
    private AttachmentMapper attachmentMapper;
    @Inject
    private AttachmentRepository attachmentRepository;

    @Inject
    private EntityManager em;

    @Inject
    SynchronousTransactionManager<Connection> transactionManager;

    @Inject @Client("/")
    RxHttpClient client;

    private Attachment attachment;

    @BeforeEach
    public void initTest() {
        attachment = createEntity(transactionManager, em);
    }

    @AfterEach
    public void cleanUpTest() {
        deleteAll(transactionManager, em);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Attachment createEntity(TransactionOperations<Connection> transactionManager, EntityManager em) {
        Attachment attachment = new Attachment()
            .fileName(DEFAULT_FILE_NAME)
            .content(DEFAULT_CONTENT)
            .contentContentType(DEFAULT_CONTENT_CONTENT_TYPE);
        return attachment;
    }

    /**
     * Delete all attachment entities.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static void deleteAll(TransactionOperations<Connection> transactionManager, EntityManager em) {
        TestUtil.removeAll(transactionManager, em, Attachment.class);
    }


    @Test
    public void createAttachment() throws Exception {
        int databaseSizeBeforeCreate = attachmentRepository.findAll().size();

        AttachmentDTO attachmentDTO = attachmentMapper.toDto(attachment);

        // Create the Attachment
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.POST("/api/attachments", attachmentDTO), AttachmentDTO.class).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.CREATED.getCode());

        // Validate the Attachment in the database
        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeCreate + 1);
        Attachment testAttachment = attachmentList.get(attachmentList.size() - 1);

        assertThat(testAttachment.getFileName()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(testAttachment.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testAttachment.getContentContentType()).isEqualTo(DEFAULT_CONTENT_CONTENT_TYPE);
    }

    @Test
    public void createAttachmentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = attachmentRepository.findAll().size();

        // Create the Attachment with an existing ID
        attachment.setId(1L);
        AttachmentDTO attachmentDTO = attachmentMapper.toDto(attachment);

        // An entity with an existing ID cannot be created, so this API call must fail
        @SuppressWarnings("unchecked")
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.POST("/api/attachments", attachmentDTO), AttachmentDTO.class)
            .onErrorReturn(t -> (HttpResponse<AttachmentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Attachment in the database
        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkFileNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = attachmentRepository.findAll().size();
        // set the field null
        attachment.setFileName(null);

        // Create the Attachment, which fails.
        AttachmentDTO attachmentDTO = attachmentMapper.toDto(attachment);

        @SuppressWarnings("unchecked")
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.POST("/api/attachments", attachmentDTO), AttachmentDTO.class)
            .onErrorReturn(t -> (HttpResponse<AttachmentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeTest);
    }

    //TODO Check below test
    //@Test
    public void getAllAttachments() throws Exception {
        // Initialize the database
        attachmentRepository.saveAndFlush(attachment);

        // Get the attachmentList w/ all the attachments
        List<AttachmentDTO> attachments = client.retrieve(HttpRequest.GET("/api/attachments?eagerload=true"), Argument.listOf(AttachmentDTO.class)).blockingFirst();
        AttachmentDTO testAttachment = attachments.get(0);


        assertThat(testAttachment.getFileName()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(testAttachment.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testAttachment.getContentContentType()).isEqualTo(DEFAULT_CONTENT_CONTENT_TYPE);
    }

    @Test
    public void getAttachment() throws Exception {
        // Initialize the database
        attachmentRepository.saveAndFlush(attachment);

        // Get the attachment
        AttachmentDTO testAttachment = client.retrieve(HttpRequest.GET("/api/attachments/" + attachment.getId()), AttachmentDTO.class).blockingFirst();


        assertThat(testAttachment.getFileName()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(testAttachment.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testAttachment.getContentContentType()).isEqualTo(DEFAULT_CONTENT_CONTENT_TYPE);
    }

    @Test
    public void getNonExistingAttachment() throws Exception {
        // Get the attachment
        @SuppressWarnings("unchecked")
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.GET("/api/attachments/"+ Long.MAX_VALUE), AttachmentDTO.class)
            .onErrorReturn(t -> (HttpResponse<AttachmentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    public void updateAttachment() throws Exception {
        // Initialize the database
        attachmentRepository.saveAndFlush(attachment);

        int databaseSizeBeforeUpdate = attachmentRepository.findAll().size();

        // Update the attachment
        Attachment updatedAttachment = attachmentRepository.findById(attachment.getId()).get();

        updatedAttachment
            .fileName(UPDATED_FILE_NAME)
            .content(UPDATED_CONTENT)
            .contentContentType(UPDATED_CONTENT_CONTENT_TYPE);
        AttachmentDTO updatedAttachmentDTO = attachmentMapper.toDto(updatedAttachment);

        @SuppressWarnings("unchecked")
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.PUT("/api/attachments", updatedAttachmentDTO), AttachmentDTO.class)
            .onErrorReturn(t -> (HttpResponse<AttachmentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.OK.getCode());

        // Validate the Attachment in the database
        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeUpdate);
        Attachment testAttachment = attachmentList.get(attachmentList.size() - 1);

        assertThat(testAttachment.getFileName()).isEqualTo(UPDATED_FILE_NAME);
        assertThat(testAttachment.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testAttachment.getContentContentType()).isEqualTo(UPDATED_CONTENT_CONTENT_TYPE);
    }

    @Test
    public void updateNonExistingAttachment() throws Exception {
        int databaseSizeBeforeUpdate = attachmentRepository.findAll().size();

        // Create the Attachment
        AttachmentDTO attachmentDTO = attachmentMapper.toDto(attachment);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        @SuppressWarnings("unchecked")
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.PUT("/api/attachments", attachmentDTO), AttachmentDTO.class)
            .onErrorReturn(t -> (HttpResponse<AttachmentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Attachment in the database
        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteAttachment() throws Exception {
        // Initialize the database with one entity
        attachmentRepository.saveAndFlush(attachment);

        int databaseSizeBeforeDelete = attachmentRepository.findAll().size();

        // Delete the attachment
        @SuppressWarnings("unchecked")
        HttpResponse<AttachmentDTO> response = client.exchange(HttpRequest.DELETE("/api/attachments/"+ attachment.getId()), AttachmentDTO.class)
            .onErrorReturn(t -> (HttpResponse<AttachmentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NO_CONTENT.getCode());

            // Validate the database is now empty
        List<Attachment> attachmentList = attachmentRepository.findAll();
        assertThat(attachmentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Attachment.class);
        Attachment attachment1 = new Attachment();
        attachment1.setId(1L);
        Attachment attachment2 = new Attachment();
        attachment2.setId(attachment1.getId());
        assertThat(attachment1).isEqualTo(attachment2);
        attachment2.setId(2L);
        assertThat(attachment1).isNotEqualTo(attachment2);
        attachment1.setId(null);
        assertThat(attachment1).isNotEqualTo(attachment2);
    }
}
