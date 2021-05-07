package com.springernature.sndeals.service.mapper;


import com.springernature.sndeals.domain.*;
import com.springernature.sndeals.service.dto.AttachmentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Attachment} and its DTO {@link AttachmentDTO}.
 */
@Mapper(componentModel = "jsr330", uses = {PostMapper.class})
public interface AttachmentMapper extends EntityMapper<AttachmentDTO, Attachment> {

    @Mapping(source = "post.id", target = "postId")
    AttachmentDTO toDto(Attachment attachment);

    @Mapping(source = "postId", target = "post")
    Attachment toEntity(AttachmentDTO attachmentDTO);

    default Attachment fromId(Long id) {
        if (id == null) {
            return null;
        }
        Attachment attachment = new Attachment();
        attachment.setId(id);
        return attachment;
    }
}
