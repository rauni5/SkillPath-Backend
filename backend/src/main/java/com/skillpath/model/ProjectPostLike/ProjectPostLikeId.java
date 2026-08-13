package com.skillpath.model.ProjectPostLike;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProjectPostLikeId implements Serializable {
    private Long postId;
    private Long userId;
}