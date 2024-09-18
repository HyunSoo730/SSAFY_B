package SW_B;


import java.util.Collections;
import java.util.TreeSet;


/**
 * 1. N명의 선수들이 경기를 진행.
 *  ! 1-1. 선수들의 수 N <= 40,000
 *  ! 1-2. 선수들이 많기 때문에 여러 개의 리그로 나눠서 경기 진행
 *  ! 1-3. 리그의 개수는 L개 (0~L-1개의 id값을 가진다.) -> id값이 작을 수록 우수한 리그
 *  ! 1-4. 리그 개수 L <= 10, N/L <= 4000, mAbility <= 10,000
 *
 * 2. N명의 선수들은 0~N-1까지의 id값과 각각 능력 값을 가지고 있다.
 *  ! 2-1. 능력 값이 높을수록 좋은 선수 -> 능력 값이 같으면 id가 작을수록 더 능력 좋은 선수
 *
 * 3. 리그 승강제 도입
 *  ! 3-1. 각각의 리그에서 능력이 가장 좋지 않은 선수는 바로 아래 리그로 내려가고, 리그에서 능력이 가장 좋은 선수는 바로 위 리그로 올라감
 *  ! 3-2. 상위 리그 : i, 하위 리그 i + 1
 *
 * 4. 트레이드 제도 도입
 *  ! 4-1. 각각 리그에서 능력이 가장 좋은 선수를 바로 위 리그의 중간 급 능력의 선수와 맞교환
 *  ! 4-2. 리그 내의 중간 급 능력 선수 = (M+1) / 2 번째 선수, 하지만 이건 1번부터 시작했을 경우 !!
 *
 * * 문제 풀이 핵심
 * 1. 바꿀 선수들을 각 리그에서 제거하고, 따로 기록해야함. movingUp, movingDown을 통해 각 리그에서 올라갈 선수, 내려갈 선수를 기록
 * 2. 모든 리그들에 대해 찾아놓은 후 두 선수 제거 후 추가. (TreeSet이라서 정렬 유지)
 */

/**
 * ! 시간복잡도 계싼
 * N <= 40,000 (선수 수)
 * L <= 10 (리그 수)
 * N/L <= 4,000 (리그 당 선수 수)
 * mAbility <= 10,000 (능력치)
 *
 * init 메서드
 *     ! 복잡도 : O(Nlog(N/L))
 *     ! N명의 선수를 L개의 리그에 분배하고 각 리그에서 TreeSet에 삽입.
 *     ! 최악의 경우 : 40,000 * log(4000)
 *
 * move 메서드
 *     ! 복잡도 : O(Llog(N/L) -> 선수를 제거하는 것이 리그당 선수 제거니까 결국 N/L이 맞아.
 *     ! 최대 호출 500번.
 *     ! 최악의 경우 : 500 * 10 * log(4000)) = 500 * 120 = 60,000
 *
 * trade 함수
 *     ! 복잡도 : O(L*N/L))
 *     ! 최대 호출 : 1000번
 *     ! 최악의 경우 1000 * 10 * 4000 = 40,000,000
 *
 * * 전체 최악의 경우도 4000만이라서 가능
 */

class UserSolution {

    private static class Player implements Comparable<Player>{
        int id, ability;

        Player(int id, int ability) {
            this.id = id;
            this.ability = ability;
        }

        // ! return this.value - other.value -> 오름차순
        // ! return other.value - this.value -> 내림차순
        @Override
        public int compareTo(Player o) {
            if (this.ability == o.ability) {
                return this.id - o.id; // ! 능력이 같다면 id가 작을수록 더 좋은 선수 (오름차순)
            }
            return o.ability - this.ability; // ! 능력이 더 높은 (내림차순)
        }
    }

    private static class League {
        TreeSet<Player> upper; // ! 상위 절반 선수들 -> 능력이 좋은 상위 절반 선수 저장 (능력 높은 순)
        TreeSet<Player> lower; // ! 하위 절반 선수들 -> 능력이 낮은 하위 절반 선수 저장 (능력 낮은 순)

        League() {
            upper = new TreeSet<>();
            lower = new TreeSet<>(Collections.reverseOrder()); // ! 능력 낮은 순으로 저장하기 위해
        }

        void add(Player player){
            // ! 1. upper TreeSet이 비어있는 경우, 새로운 선수를 무조건 upper에 추가. -> 첫 번째 선수를 추가할 때 발생
            // ! 2. 새로 추가하려는 선수(player)와 upper의 마지막 선수(가장 능력이 낮은 선수) 비교
            // ! 2-1. 0보다 작으면 새 선수의 능력이 upper.last()보다 높다는 의미 -> 내가 구현한 compareTo를 보고 조건문 작성하면됨
            if (upper.isEmpty() || player.compareTo(upper.last()) < 0) {
                upper.add(player);
            }else {
                lower.add(player);
            }
            balance(); // ! 이제 추가 후에 개수 조정 진행해야해.
        }

        private void balance() {
            while (upper.size() > lower.size() + 1) { // ! 딱 같거나 1개 커야함.
                lower.add(upper.pollLast());
            }
            while (upper.size() < lower.size()) {
                upper.add(lower.pollLast()); // ! lower은 가장 첫번째가 능력이 좋음 정렬 조건 반대라.
            }
        }

        /**
         * ! 위치 불확실성 처리. -> 선수가 upper에 있을지 lower에 있을지 미리 알 수 없다.
         * ! 따라서 두 TreeSet 모두에서 제거 시도.
         * * remove 메서드는 요소가 없으면 false 반환. 따라서 OlogN
         * * 제거가 성공했을 때만 balance 호출 (균형 유지)
         */
        void remove(Player player) {
            if (upper.remove(player) || lower.remove(player)) {
                balance();
            }
        }

        Player getFirst() {
            return upper.first(); // ! 가장 능력 높은 선수
        }

        // ! 무조건 lower의 first라고 생각했으면 안됐음. 없을 수도 있잖아.
        Player getLast() {
            return lower.isEmpty() ? upper.last() : lower.first(); // ! 가장 능력 낮은 선수
        }

        /**
         * ! add함수와 balance() 메서드의 작동 방식 떄문에 upper의 크기는 항상 lower와 같거나 1개 더 많게 유지
         * ! 이로 인해 중간값은 항상 upper.last()가 된다.
         * * 전체 선수가 홀수 : upper의 크기는 lower크기보다 항상 1크게 된다.
         * * 전체 선수가 짝수 : upper와 lower의 크기가 같아진다.
         */
        Player getMiddle() {
            return upper.last(); // ! 중간 순위의 선수
        }
    }

    static League[] leagues; // ! 리그별 선수 관리
    static int totalPlayers;
    static int totalLeagues;
    static int playerPerLeague;

    /**
     * ! 선수의 수, 리그의 수, 선수의 능력치가 주어짐
     * ! 선수들은 순서대로 리그에 배치됨 -> 처음에 최초로 모든 리그들을 정렬한다.
     * ! 초기화 함수
     */
    void init(int N, int L, int mAbility[]) {
        totalPlayers = N;
        totalLeagues = L;
        playerPerLeague = N / L;

        leagues = new League[totalLeagues];
        for (int i = 0; i < L; i++) {
            leagues[i] = new League();
        }

        for (int i = 0; i < totalPlayers; i++) {
            int ability = mAbility[i]; // ! 각 플레이어 능력치
            int leagueId = i / playerPerLeague;
            leagues[leagueId].add(new Player(i, ability));
        }
    }

    /**
     * ! 최대 500번 호출.
     * ! 리그 승강제.
     * * 이진탐색 필요가 없음. TreeSet이 내부적으로 정렬된 상태를 유지하기 때문.
     * * 최고/최하위 선수를 찾는 데 O(1) 시간이 걸린다.
     * * 삽입과 삭제가 O(logN)시간에 이루어짐.
     * ! 총 totalLeagues-1번 반복 -> 전체 시간복잡도 O(LlogN)
     */
    int move() {
        int result = 0;

        Player[] movingUp = new Player[totalLeagues - 1];
        Player[] movingDown = new Player[totalLeagues - 1];

        // ! 이동할 선수 선택
        for (int i = 0; i < totalLeagues - 1; i++) {
            movingUp[i] = leagues[i + 1].getFirst(); // ! 하위 리그의 최고 선수
            movingDown[i] = leagues[i].getLast(); // ! 상위 리그의 최하위 선수

            result += movingUp[i].id + movingDown[i].id;

            // ! 선택한 선수들 제거
            leagues[i + 1].remove(movingUp[i]);
            leagues[i].remove(movingDown[i]);

        }

        // ! 선수들 이동
        for (int i = 0; i < totalLeagues - 1; i++) {
            leagues[i].add(movingUp[i]); // ! 상위 리그로 이동
            leagues[i + 1].add(movingDown[i]); // ! 하위 리그로 이동
        }

        return result;
    }

    /**
     * ! 최대 1000번 호출.
     * ! 트레이드 제도
     * ! 일단 while문으로 바로 찾아보기.
     * * 각각의 리그에서 능력이 가장 좋은 선수를 바로 위 리그의 중간 급 능력의 선수와 맞교환
     */
    int trade() {
        int result = 0;

        Player[] movingUp = new Player[totalLeagues - 1];
        Player[] movingDown = new Player[totalLeagues - 1];

        // ! 트레이드할 선수 선택
        for (int i = 0; i < totalLeagues - 1; i++) {
            movingUp[i] = leagues[i + 1].getFirst(); // ! 하위 리그의 최고 선수
            movingDown[i] = leagues[i].getMiddle(); // ! 상위 리그의 중위권 선수

            result += movingUp[i].id + movingDown[i].id;
            // ! 해당 선수들 제거
            leagues[i + 1].remove(movingUp[i]);
            leagues[i].remove(movingDown[i]);
        }

        // ! 선수들 트레이드
        for (int i = 0; i < totalLeagues - 1; i++) {
            leagues[i].add(movingUp[i]);   // ! 상위 리그로 이동
            leagues[i + 1].add(movingDown[i]); // ! 하위 리그로 이동
        }

        return result;
    }



}
