package com.skillpath.model.ProjectCommentLike;
import lombok.*;
import java.io.Serializable;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProjectCommentLikeId implements Serializable {
    private Long commentId;
    private Long userId;
}