package com.skillpath.model.ProjectMember;
import com.skillpath.model.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "project_members")
@IdClass(ProjectMemberId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectMember {
 @Id @Column(name = "project_id") private Long projectId;
 @Id @Column(name = "user_id") private Long userId;
 @Column(length = 100) private String role;
 @Enumerated(EnumType.STRING) @Builder.Default
 private MemberStatus status = MemberStatus.PENDING;
 @Column(name = "joined_at") final private Instant joinedAt = Instant.now();
}
