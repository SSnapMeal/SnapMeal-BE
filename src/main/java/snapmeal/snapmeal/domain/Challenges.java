package snapmeal.snapmeal.domain;

import jakarta.persistence.*;
        import lombok.*;
        import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.common.BaseEntity;
import snapmeal.snapmeal.domain.enums.ChallengeStatus;

/**
 * 주당 3개 생성되는 "음식 기반 챌린지" 엔티티
 * - BaseEntity를 상속해서 createdAt/updatedAt 자동 관리
 * - 컬럼/조인 컬럼은 스네이크 케이스로 명시
 */
@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenges extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    /** 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 스타일 통일
    private User user;

    /** 챌린지 제목(예: "커피 마시기") */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /** 성공 판정용 타겟 메뉴 키워드(식사기록 menu 에 포함되면 성공) */
    @Column(name = "target_menu_name", nullable = false, length = 100)
    private String targetMenuName;

    /** 간단 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 유효기간(포함 범위: start_date ~ end_date 23:59:59) */
    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ChallengeStatus status;

    /** 참여/완료/취소 타임스탬프 (표시용) */
    @Column(name = "participated_at")
    private java.time.LocalDateTime participatedAt;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;

    // ✅ 회피형(예: “커피 안마시기”) 여부
    // true  = 회피형(그날 해당 메뉴가 없어야 충족)
    // false = 섭취형(그날 해당 메뉴가 있으면 충족)
    @Column(name = "is_avoid_type", nullable = false)
    @Builder.Default
    private boolean isAvoidType = false;

    // ====== 도메인 메서드: 상태 전환 로직(서비스에서 재사용) ======

    /** 사용자가 "참여하기" 버튼을 눌렀을 때 호출 */
    public void participate() {
        if (this.status != ChallengeStatus.PENDING) {
            throw new IllegalStateException("현재 상태에서는 참여할 수 없어요.");
        }
        this.status = ChallengeStatus.IN_PROGRESS;
        this.participatedAt = java.time.LocalDateTime.now();
    }

    /** 사용자가 "포기" 버튼을 눌렀을 때 호출 */
    public void giveUp() {
        if (this.status != ChallengeStatus.IN_PROGRESS) {
            throw new IllegalStateException("도전 중(IN_PROGRESS) 상태에서만 포기할 수 있어요.");
        }
        this.status = ChallengeStatus.CANCELLED;
        this.cancelledAt = java.time.LocalDateTime.now();
    }

    /** 자정 배치에서 성공 판정 시 호출 */
    public void success(java.time.LocalDateTime when) {
        this.status = ChallengeStatus.SUCCESS;
        this.completedAt = when;
    }

    /** 기간 종료 시 실패로 마감 */
    public void fail() {
        if (this.status == ChallengeStatus.PENDING || this.status == ChallengeStatus.IN_PROGRESS) {
            this.status = ChallengeStatus.FAIL;
        }
    }
}
