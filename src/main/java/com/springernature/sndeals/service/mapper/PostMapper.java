package com.springernature.sndeals.service.mapper;


import com.springernature.sndeals.domain.*;
import com.springernature.sndeals.service.dto.PostDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Post} and its DTO {@link PostDTO}.
 */
@Mapper(componentModel = "jsr330", uses = {CategoryMapper.class})
public interface PostMapper extends EntityMapper<PostDTO, Post> {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.displayName", target = "categoryDisplayName")
    PostDTO toDto(Post post);

    @Mapping(source = "categoryId", target = "category")
    Post toEntity(PostDTO postDTO);

    default Post fromId(Long id) {
        if (id == null) {
            return null;
        }
        Post post = new Post();
        post.setId(id);
        return post;
    }
}
