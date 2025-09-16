package snapmeal.snapmeal.domain.enums;

public enum ChallengeStatus {
        PENDING,       // 만들어졌지만 아직 참여 버튼을 안 눌렀음 (주당 3개가 여기에 해당)
        IN_PROGRESS,   // 사용자가 참여 버튼을 눌러 도전 중
        SUCCESS,       // 성공(자정 배치가 식사기록을 보고 성공으로 판정)
        FAIL,          // 기간 끝났는데 성공 못함
        CANCELLED      // 사용자가 포기하기 버튼을 눌러 취소
}
