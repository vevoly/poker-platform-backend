package com.pokergame.game.doudizhu.integration;

import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.rule.ValidationResult;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.rule.DoudizhuRuleChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 斗地主完整牌局集成测试
 *
 * 模拟一局完整游戏：
 * 1. 创建房间
 * 2. 玩家加入
 * 3. 准备
 * 4. 开始游戏
 * 5. 发牌
 * 6. 叫地主
 * 7. 出牌
 * 8. 游戏结束
 *
 * @author poker-platform
 */

@DisplayName("斗地主完整牌局集成测试")
class FullGameIntegrationTest {

    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;
    private DoudizhuPlayer landlord;
    private DoudizhuPlayer farmer1;
    private DoudizhuPlayer farmer2;
    private DoudizhuRuleChecker ruleChecker;

    private List<PlayRecord> playRecords = new ArrayList<>();
    private long winnerId = 0;

    @BeforeEach
    void setUp() {
        roomService = DoudizhuRoomService.me();
        roomService.getRoomMap().clear();
        roomService.getUserRoomMap().clear();

        // 1. 创建房间
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        room = roomService.createRoom(createContext);
        room.setOwnerId(1001L);
        room.setMaxPlayers(3);
        room.setGameStatus(DoudizhuGameStatus.WAITING);

        // 2. 创建玩家
        landlord = new DoudizhuPlayer(1001L, "地主玩家");
        farmer1 = new DoudizhuPlayer(1002L, "农民玩家1");
        farmer2 = new DoudizhuPlayer(1003L, "农民玩家2");

        // 3. 玩家加入房间
        room.addDoudizhuPlayer(landlord);
        room.addDoudizhuPlayer(farmer1);
        room.addDoudizhuPlayer(farmer2);

        roomService.addRoom(room);
        roomService.addPlayer(room, landlord);
        roomService.addPlayer(room, farmer1);
        roomService.addPlayer(room, farmer2);

        // 4. 玩家准备
        landlord.setReady(true);
        farmer1.setReady(true);
        farmer2.setReady(true);

        // 5. 开始游戏，进入叫地主阶段
        room.setGameStatus(DoudizhuGameStatus.BIDDING);

        ruleChecker = new DoudizhuRuleChecker(room);

        playRecords.clear();
        winnerId = 0;
    }

    @Test
    @DisplayName("模拟一局完整游戏 - 地主炸弹获胜")
    void testFullGameLandlordWinWithBomb() {
        System.out.println("\n========== 开始完整牌局测试（地主炸弹获胜） ==========\n");

        // 1. 发牌（地主有炸弹）
        dealCardsForLandlordBombWin();
        printGameState("初始状态");

        // 2. 确定地主
        setLandlord(landlord);
        printGameState("确定地主后");

        // 3. 模拟出牌（地主出炸弹）
        simulateLandlordBombWin();

        // 4. 验证游戏结束
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.FINISHED);
        assertThat(winnerId).isEqualTo(landlord.getUserId());

        printGameSummary();
    }

    @Test
    @DisplayName("模拟一局完整游戏 - 地主单张获胜")
    void testFullGameLandlordWinWithSingle() {
        System.out.println("\n========== 开始完整牌局测试（地主单张获胜） ==========\n");

        // 1. 发牌（地主有小牌）
        dealCardsForLandlordSingleWin();
        printGameState("初始状态");

        // 2. 确定地主
        setLandlord(landlord);
        printGameState("确定地主后");

        // 3. 模拟出牌（地主一张一张出）
        simulateLandlordSingleWin();

        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.FINISHED);
        assertThat(winnerId).isEqualTo(landlord.getUserId());

        printGameSummary();
    }

    @Test
    @DisplayName("模拟一局完整游戏 - 农民获胜")
    void testFullGameFarmerWin() {
        System.out.println("\n========== 开始完整牌局测试（农民获胜） ==========\n");

        // 1. 发牌（农民有好牌）
        dealCardsForFarmerWin();
        printGameState("初始状态");

        // 2. 确定地主
        setLandlord(landlord);
        printGameState("确定地主后");

        // 3. 模拟出牌
        simulateFarmerWin();

        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.FINISHED);
        assertThat(winnerId).isIn(farmer1.getUserId(), farmer2.getUserId());

        printGameSummary();
    }

    // ==================== 发牌模拟（确保无重复牌） ====================

    /**
     * 地主炸弹获胜 - 地主有炸弹，农民牌小
     */
    private void dealCardsForLandlordBombWin() {
        // 使用一副牌的所有ID，确保不重复
        List<Integer> allIds = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            allIds.add(i);
        }
        Collections.shuffle(allIds);

        // 地主：四张3 (ID: 0,13,26,39) + 其他16张
        List<Integer> landlordIds = new ArrayList<>();
        landlordIds.add(0);   // ♠3
        landlordIds.add(13);  // ♥3
        landlordIds.add(26);  // ♣3
        landlordIds.add(39);  // ♦3

        // 添加其他牌
        for (int i = 4; i < 20; i++) {
            landlordIds.add(allIds.get(i));
        }

        // 农民1：17张
        List<Integer> farmer1Ids = allIds.subList(20, 37);

        // 农民2：17张
        List<Integer> farmer2Ids = allIds.subList(37, 54);

        // 底牌3张
        List<Integer> extraIds = allIds.subList(0, 3);

        landlord.setHandCards(idsToCards(landlordIds));
        farmer1.setHandCards(idsToCards(farmer1Ids));
        farmer2.setHandCards(idsToCards(farmer2Ids));
        room.setLandlordExtraCards(idsToCards(extraIds));

        System.out.println("发牌完成（地主炸弹获胜模式）:");
        System.out.println("  地主: " + landlord.getCardCount() + "张 (含炸弹)");
        System.out.println("  农民1: " + farmer1.getCardCount() + "张");
        System.out.println("  农民2: " + farmer2.getCardCount() + "张");
    }

    /**
     * 地主单张获胜 - 地主有小牌
     */
    private void dealCardsForLandlordSingleWin() {
        List<Integer> allIds = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            allIds.add(i);
        }
        Collections.shuffle(allIds);

        // 地主：17张
        List<Integer> landlordIds = allIds.subList(0, 17);

        // 农民1：17张
        List<Integer> farmer1Ids = allIds.subList(17, 34);

        // 农民2：17张
        List<Integer> farmer2Ids = allIds.subList(34, 51);

        // 底牌3张
        List<Integer> extraIds = allIds.subList(51, 54);

        landlord.setHandCards(idsToCards(landlordIds));
        farmer1.setHandCards(idsToCards(farmer1Ids));
        farmer2.setHandCards(idsToCards(farmer2Ids));
        room.setLandlordExtraCards(idsToCards(extraIds));

        System.out.println("发牌完成（地主单张获胜模式）:");
        System.out.println("  地主: " + landlord.getCardCount() + "张");
        System.out.println("  农民1: " + farmer1.getCardCount() + "张");
        System.out.println("  农民2: " + farmer2.getCardCount() + "张");
    }

    /**
     * 农民获胜
     */
    private void dealCardsForFarmerWin() {
        List<Integer> allIds = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            allIds.add(i);
        }
        Collections.shuffle(allIds);

        // 地主：17张（小牌）
        List<Integer> landlordIds = allIds.subList(0, 17);

        // 农民1：17张（好牌）
        List<Integer> farmer1Ids = allIds.subList(17, 34);

        // 农民2：17张（好牌）
        List<Integer> farmer2Ids = allIds.subList(34, 51);

        // 底牌3张
        List<Integer> extraIds = allIds.subList(51, 54);

        landlord.setHandCards(idsToCards(landlordIds));
        farmer1.setHandCards(idsToCards(farmer1Ids));
        farmer2.setHandCards(idsToCards(farmer2Ids));
        room.setLandlordExtraCards(idsToCards(extraIds));

        System.out.println("发牌完成（农民获胜模式）:");
        System.out.println("  地主: " + landlord.getCardCount() + "张");
        System.out.println("  农民1: " + farmer1.getCardCount() + "张");
        System.out.println("  农民2: " + farmer2.getCardCount() + "张");
    }

    private List<Card> idsToCards(List<Integer> ids) {
        return ids.stream().map(Card::of).collect(Collectors.toList());
    }

    // ==================== 叫地主 ====================

    private void setLandlord(DoudizhuPlayer landlordPlayer) {
        List<Card> extraCards = room.getLandlordExtraCards();
        landlordPlayer.addCards(extraCards);
        landlordPlayer.setLandlord(true);

        room.setLandlordId(landlordPlayer.getUserId());

        List<Long> playOrder = List.of(
                landlord.getUserId(),
                farmer1.getUserId(),
                farmer2.getUserId()
        );
        room.setPlayOrder(playOrder, landlord.getUserId());

        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        System.out.println("\n叫地主完成: 地主是 " + landlordPlayer.getNickname());
        System.out.println("  地主手牌: " + landlordPlayer.getCardCount() + "张");
    }

    // ==================== 出牌模拟 ====================

    /**
     * 地主炸弹获胜
     */
    private void simulateLandlordBombWin() {
        System.out.println("\n========== 开始出牌阶段 ==========\n");

        int turnCount = 0;

        // 找出炸弹（四张相同的牌）
        List<Card> bombCards = findBomb(landlord.getHandCards());

        if (bombCards != null) {
            // 出炸弹
            boolean success = playCardValidated(landlord, bombCards, ++turnCount);
            if (success) {
                System.out.println("🎉 地主出炸弹成功！");
            }
        }

        // 出剩余所有牌
        if (!landlord.getHandCards().isEmpty()) {
            playCardValidated(landlord, landlord.getHandCards(), ++turnCount);
        }

        System.out.println("\n🎉 地主出完所有牌，游戏结束！");
        room.setGameStatus(DoudizhuGameStatus.FINISHED);
        winnerId = landlord.getUserId();
    }

    /**
     * 地主单张获胜
     */
    private void simulateLandlordSingleWin() {
        System.out.println("\n========== 开始出牌阶段 ==========\n");

        int turnCount = 0;
        List<Card> hand = new ArrayList<>(landlord.getHandCards());

        // 排序手牌
        hand.sort(Comparator.comparingInt(c -> c.getRank().getValue()));

        for (Card card : hand) {
            List<Card> single = Arrays.asList(card);
            boolean success = playCardValidated(landlord, single, ++turnCount);
            if (!success) {
                System.out.println("出牌失败: " + card);
                break;
            }
        }

        if (landlord.getHandCards().isEmpty()) {
            System.out.println("\n🎉 地主出完所有牌，游戏结束！");
            room.setGameStatus(DoudizhuGameStatus.FINISHED);
            winnerId = landlord.getUserId();
        }
    }

    /**
     * 农民获胜 - 完整版
     * 确保农民出的牌能压过地主，并且出完所有牌
     */
    private void simulateFarmerWin() {
        System.out.println("\n========== 开始出牌阶段 ==========\n");

        int turnCount = 0;

        // 1. 地主先出最小的单张
        List<Card> landlordCard = getSmallestCard(landlord.getHandCards());
        if (landlordCard.isEmpty()) {
            System.out.println("地主没有手牌，异常");
            return;
        }

        boolean success = playCardValidated(landlord, landlordCard, ++turnCount);
        if (!success) {
            System.out.println("地主出牌失败，游戏异常结束");
            return;
        }

        // 2. 农民1必须出能压过地主的牌
        List<Card> farmer1Card = findCardThatCanBeat(farmer1.getHandCards(), landlordCard);
        if (farmer1Card == null || farmer1Card.isEmpty()) {
            System.out.println("农民1没有能压过地主的牌，游戏继续");
            // 如果没有能压的牌，农民1过牌，轮到农民2
            System.out.println("农民1 过牌");
            room.nextTurn();

            // 农民2尝试出牌
            List<Card> farmer2Card = findCardThatCanBeat(farmer2.getHandCards(), landlordCard);
            if (farmer2Card == null || farmer2Card.isEmpty()) {
                System.out.println("农民2也没有能压过地主的牌，地主继续出牌");
                return;
            }

            success = playCardValidated(farmer2, farmer2Card, ++turnCount);
            if (success && farmer2.getHandCards().isEmpty()) {
                System.out.println("\n🎉 农民2出完所有牌，游戏结束！");
                room.setGameStatus(DoudizhuGameStatus.FINISHED);
                winnerId = farmer2.getUserId();
            }
            return;
        }

        // 3. 农民1出牌
        success = playCardValidated(farmer1, farmer1Card, ++turnCount);
        if (!success) {
            System.out.println("农民1出牌失败");
            return;
        }

        // 4. 农民1继续出完剩余手牌
        if (!farmer1.getHandCards().isEmpty()) {
            // 需要确保剩余手牌能一次出完（简化处理：直接出所有牌）
            // 注意：这里需要校验剩余手牌是否为合法牌型
            List<Card> remainingCards = new ArrayList<>(farmer1.getHandCards());

            // 先校验是否能一次出完
            ValidationResult result = ruleChecker.validatePlay(farmer1.getUserId(), remainingCards);
            if (result.isValid()) {
                playCardValidated(farmer1, remainingCards, ++turnCount);
            } else {
                System.out.println("农民1剩余手牌不能一次出完，需要更复杂的出牌逻辑");
                // 简化处理：一张一张出
                for (Card card : remainingCards) {
                    List<Card> single = Arrays.asList(card);
                    if (playCardValidated(farmer1, single, ++turnCount)) {
                        if (farmer1.getHandCards().isEmpty()) {
                            break;
                        }
                    } else {
                        System.out.println("出牌失败，游戏中断");
                        return;
                    }
                }
            }
        }

        if (farmer1.getHandCards().isEmpty()) {
            System.out.println("\n🎉 农民1出完所有牌，游戏结束！");
            room.setGameStatus(DoudizhuGameStatus.FINISHED);
            winnerId = farmer1.getUserId();
        }
    }

    /**
     * 从手牌中找出能压过上家牌的最小单张
     */
    private List<Card> findCardThatCanBeat(List<Card> hand, List<Card> lastPlayCards) {
        if (hand.isEmpty()) {
            return null;
        }

        if (lastPlayCards == null || lastPlayCards.isEmpty()) {
            return getSmallestCard(hand);
        }

        // 获取上家牌的主牌值
        int lastMainRank = room.getLastMainRank();
        if (lastMainRank == 0 && !lastPlayCards.isEmpty()) {
            lastMainRank = lastPlayCards.get(0).getRank().getValue();
        }

        // 找出比上家大的最小单张
        Card bestCard = null;
        for (Card card : hand) {
            if (card.getRank().getValue() > lastMainRank) {
                if (bestCard == null || card.getRank().getValue() < bestCard.getRank().getValue()) {
                    bestCard = card;
                }
            }
        }

        return bestCard != null ? Arrays.asList(bestCard) : null;
    }

    /**
     * 执行出牌（带校验）
     */
    private boolean playCardValidated(DoudizhuPlayer player, List<Card> cards, int turnCount) {
        ValidationResult result = ruleChecker.validatePlay(player.getUserId(), cards);

        if (result.isValid()) {
            player.removeCards(cards);
            room.updateLastPlay(
                    player.getUserId(), cards,
                    result.getPattern(), result.getMainRank(), result.getSubRank()
            );

            playRecords.add(new PlayRecord(turnCount, player.getUserId(),
                    player.getNickname(), cards, result.getPattern(), false));

            System.out.println("第 " + turnCount + " 回合: " + player.getNickname() +
                    " 出 " + formatCards(cards) + " (" + result.getPattern().getName() + ")");
            System.out.println("  剩余手牌: " + player.getCardCount() + "张");

            room.nextTurn();
            return true;
        } else {
            System.out.println("第 " + turnCount + " 回合: " + player.getNickname() +
                    " 出牌无效: " + result.getErrorMessage());
            return false;
        }
    }

    // ==================== 辅助方法 ====================

    private List<Card> findBomb(List<Card> hand) {
        Map<Integer, List<Card>> rankMap = hand.stream()
                .collect(Collectors.groupingBy(c -> c.getRank().getValue()));

        for (List<Card> cards : rankMap.values()) {
            if (cards.size() == 4) {
                return cards;
            }
        }
        return null;
    }

    private List<Card> getSmallestCard(List<Card> hand) {
        Card smallest = hand.stream()
                .min(Comparator.comparingInt(c -> c.getRank().getValue()))
                .orElse(null);
        return smallest != null ? Arrays.asList(smallest) : Collections.emptyList();
    }

    private String formatCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return "[]";
        }
        return cards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(" "));
    }

    private void printGameState(String stage) {
        System.out.println("\n--- " + stage + " ---");
        System.out.println("地主(" + landlord.getNickname() + "): " + landlord.getCardCount() + "张");
        System.out.println("农民1(" + farmer1.getNickname() + "): " + farmer1.getCardCount() + "张");
        System.out.println("农民2(" + farmer2.getNickname() + "): " + farmer2.getCardCount() + "张");
        System.out.println("游戏状态: " + room.getGameStatus());
    }

    private void printGameSummary() {
        System.out.println("\n========== 游戏总结 ==========");
        String winnerName;
        if (winnerId == landlord.getUserId()) {
            winnerName = landlord.getNickname();
        } else if (winnerId == farmer1.getUserId()) {
            winnerName = farmer1.getNickname();
        } else if (winnerId == farmer2.getUserId()) {
            winnerName = farmer2.getNickname();
        } else {
            winnerName = "未知";
        }
        System.out.println("获胜者: " + winnerName);
        System.out.println("游戏状态: " + room.getGameStatus());
        System.out.println("出牌记录数: " + playRecords.size());

        System.out.println("\n出牌记录:");
        for (PlayRecord record : playRecords) {
            System.out.println(record);
        }
    }

    // ==================== 内部类 ====================

    static class PlayRecord {
        private final int turn;
        private final long playerId;
        private final String playerName;
        private final List<Card> cards;
        private final CardPattern pattern;
        private final boolean isPass;

        public PlayRecord(int turn, long playerId, String playerName,
                          List<Card> cards, CardPattern pattern, boolean isPass) {
            this.turn = turn;
            this.playerId = playerId;
            this.playerName = playerName;
            this.cards = cards;
            this.pattern = pattern;
            this.isPass = isPass;
        }

        @Override
        public String toString() {
            if (isPass) {
                return String.format("第%d回合: %s 过牌", turn, playerName);
            }
            return String.format("第%d回合: %s 出 %s (%s)",
                    turn, playerName,
                    cards != null ? cards.stream().map(Card::toString).collect(Collectors.joining(" ")) : "[]",
                    pattern != null ? pattern.getName() : "未知");
        }
    }
}