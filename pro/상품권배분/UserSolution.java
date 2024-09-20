package B.상품권배분;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

class UserSolution {

    /**
     * * 상품권배분
     * ! N개의 그룹마다 여러 부서가 하나의 트리로 구성된다.
     * ! 부서는 ID와 소속 인원 수를 가지고, 최대 3개의 하위 부서를 갖는다.
     * ! N개의 그룹에 인원 수대로 상품권을 나누어 주려고 한다. 상품권의 총 개수는 K개이다.
     *
     * * 배분 규칙
     * ! 1-1. 총 인원수가 K이하인 경우, 각 그룹의 인원 수대로 상품권 나눠준다.
     * ! 1-2. 총 인원수가 K보다 큰 경우, 상한 개수 L을 정한다. 그룹의 인원 수가 L이하인 경우에는 그룹의 인원 수대로 상품권을 주고,
     * !      그룹의 인원 수가 L을 초과하는 경우에는 L개의 상품권을 준다.
     *
     * * K개 내에서 최대한 많은 상품권을 나누어 주었을 때, 각 그룹에 배분된 상품권 개수 중에서 가장 큰 값을 구해야 한다.
     *
     */

    private static class Part {
        int id; // ! 각 부서의 고유 ID
        int peopleCount;  // ! 해당 부서에 직접 속한 인원 수.
        int totalPeopleCount;  // ! 이 부서와 그 아래 모든 하위 부서를 포함한 전체 인원 수 -> 부서 추가되거나 제거될 때 값 갱신해야 함
        int parentId; // ! 부모 부서의 ID -> 최상위
        List<Part> next;  // ! 하위 부서 리스트
        boolean isRoot; // ! 최상위 부서인지

        Part(int id, int peopleCount) {
            this.id = id;
            this.peopleCount = peopleCount;
            this.totalPeopleCount = peopleCount;
            this.next = new ArrayList<>();
        }

    }

    static int totalGroup;

    static HashMap<Integer, Part> partMap;
    static Part[] roots; // ! 루트 배열
    static int[] totalPeople;
    /**
     * ! 테스트 케이스 처음에 호출.
     * * N개의 그룹에 대한 최상위 부서 ID와 부서 인원 수가 각각 배열로 주어진다.
     * N <= 1000
     * mId[i] -> i+1 번째 그룹의 최상위 부서 ID
     */
    public void init(int N, int mId[], int mNum[]) {
        totalGroup = N;
        partMap = new HashMap<>();
        roots = new Part[totalGroup];

        for (int i = 0; i < totalGroup; i++) {
            int id = mId[i]; // ! 각 그룹의 최상위 부서 ID
            int peopleCount = mNum[i]; // ! 해당 그룹의 소속 인원

            Part part = new Part(id, peopleCount);
            part.parentId = -1; // ! 최상위 부서는 일단 -1로
            part.isRoot = true; // ! init에서 최상위 부서는 루트로 표시
            // ! Hash에도 최상위(루트) 그룹 추가
            partMap.put(id, part);
            roots[i] = part;
        }
    }

    /**
     * ! mId 부서를 mParent 부서의 하위 부서로 추가. mParent -> mId
     * ! mId 부서의 인원수는 mNum
     * ! mParent 값은 항상 존재하는 부서의 ID만 주어진다.
     * ! mParent 부서에 이미 3개의 하위 부서가 존재한다면, 추가에 실패하고 -1반환
     * ! mId 값으로 이미 존재하는 부서의 ID가 주어지는 경우는 없음
     * ! add 호출 횟수 17,000 이하. -> 충분함
     * @return 추가 성공 시, mParent 부서를 포함하여 그 아래 모든 부서의 인원 수 합을 반환한다. -> mParent가 루트 노드인 서브 트리의 인원 수 합 반환
     */
    public int add(int mId, int mNum, int mParent) {
        Part parent = partMap.get(mParent);
        if (parent.next.size() >= 3) {
//            System.out.println("add result : " + -1);
            return -1;
        }

        // ! 이제 mId인 부서를 추가
        Part part = new Part(mId, mNum);
        part.parentId = mParent; // ! 부모 ID 저장 후
        parent.next.add(part); // ! 하위 부서로 추가. -> 추가할 때 사람 수도 추가해야함.
        // ! Hash에도 해당 부서 추가.
        partMap.put(mId, part); // ! 부서 추가
        // ! 부서 추가 후 합 갱신
        updateSum(mParent, mNum); // ! -> 중요한 것이 가장 마지막으로 추가한 곳부터 위로 올라가면서 추가해줘야 함 !

//        System.out.println("add result : " + parent.totalPeopleCount);
        return parent.totalPeopleCount;
    }

    /**
     * ! ID가 mId인 부서 삭제. mId 하위 부서도 함께 삭제.
     * ! 최상위 부서의 ID가 주어지는 경우는 없음.
     * ! 이미 삭제된 부서의 ID가 주어질 수도 있음
     * ! mId 부서가 존재하지 않을 경우, -1 반환
     * * remove 호출 횟수 2000 이하 -> 충분함
     * @return mId 부서가 존재할 경우, mId 부서를 포함하여 그 아래 부서의 인원 수 합을 반환한다.-> mId가 루트 노드인 서브 트리의 인원 수 합 반환, mId 부서 존재하지 않을 경우 -1 반환
     * Hash로 관리해야겠다고 생각함.
     */
    public int remove(int mId) {
        // ! mId인 부서 map에서 제거
        Part part = partMap.get(mId);
        if (part == null) { // ! 존재하지 않으면
            return -1;
        }
        // ! 부모 부서에서 해당 부서 제거
        int removedCount = part.totalPeopleCount;

        // ! 부모 부서에서 이 부서 제거
        Part parent = partMap.get(part.parentId);
        parent.next.remove(part);

        // ! 상위 부서들의 totalPeopleCount 갱신 -> 해당 부서가 가지고 있던 수만큼 다시 빼줘야지
        updateSum(parent.id, -removedCount);

        // ! map에서 mId 부서 포함 하위 부서들까지 모두 제거
        Deque<Part> dq = new ArrayDeque<>();
        dq.offer(part);
        while (!dq.isEmpty()) {
            Part now = dq.poll();
            partMap.remove(now.id);
            for (Part p : now.next) {
                dq.offer(p);
            }
        }

//        System.out.println("remove result : " + removedCount);
        return removedCount;
    }

    /**
     * ! N개의 그룹에 상품권 K개를 배분 규칙에 맞게 최대한 많이 나누어 주었을 때, 각 그룹에 배분된 상품권 개수 중에서 가장 큰 값을 반환한다.
     * ! 상품권의 개수 K <= 800,000
     * * distribute() 호출 횟수 1000 이하 -> 충분함
     *
     * * 배분 규칙
     *  * ! 1-1. 총 인원수가 Km이하인 경우, 각 그룹의 인원 수대로 상품권 나눠준다.
     *  * ! 1-2. 총 인원수가 K보다 큰 경우, 상한 개수 L을 정한다. 그룹의 인원 수가 L 이하인 경우에는 그룹의 인원 수대로 상품권을 주고,
     *  * ! 그룹의 인원 수가 L을 초과하는 경우에는 L개의 상품권을 준다. -> 최대한 많이 나눠주는 L 정해야함
     * @return 각 그룹에 배분된 상품권 개수 중에서 가장 큰 값 반환.
     */
    public int distribute(int K) {
        // ! 각 루트 그룹의 totalPeopleCount 합산해서 K와 비교.
        int sum = 0;
        int max_size = 0; // ! 각 그룹에 배분된 상품권 개수 중 가장 큰 값 반환.
        for (Part part : roots) { // ! 최대 1000개의 루트
            sum += part.totalPeopleCount;
            max_size = Math.max(max_size, part.totalPeopleCount);
        }

        if (sum <= K) { // ! 총 인원수가 K이하인 경우, 각 그룹의 인원 수대로 상품권 나눠준다.
//            System.out.println("distribute result : " + max_size);
            return max_size;
        } else { // ! 상한선 L -> 이분탐색으로 찾아야함.
            int left = 1;
            int right = max_size;

            while (left <= right) {
                int mid = (left + right) / 2;
                int L = calculate(mid);

                if (L <= K) { //  ! 더 큰 상한선 L을 찾기 위해 left = mid + 1
                    left = mid + 1;
                } else { // ! 상한선이 너무 커서 right = mid - 1
                    right = mid - 1;
                }
            }
            // ! 루프가 종료되면 right 값 반환 -> 이 값이 K개 이하의 상품권을 분배하면서 가능한 가장 큰 상한선.
//            System.out.println("distribute result : " + right);
            return right;
        }
    }

    // ! 주어진 상한선으로 분배했을 때 총 분배되는 상품권의 개수 계산
    static int calculate(int L) {
        int total = 0;
        for (Part part : roots) {
            total += Math.min(L, part.totalPeopleCount);
        }
        return total;
    }

    static void updateSum(int id, int num) {
        Part part = partMap.get(id);
        while (part != null) { // ! 최상위 부서는 -1 로 넣어뒀으니 + 최상위 부서는 이미 값을 가지고 시작함 !
            part.totalPeopleCount += num;
            part = partMap.get(part.parentId);
        }
    }
}
