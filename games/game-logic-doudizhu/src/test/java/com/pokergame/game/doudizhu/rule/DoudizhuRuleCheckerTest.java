package com.pokergame.game.doudizhu.rule;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.rule.ValidationResult;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 斗地主出牌校验测试（适配重构后的 DoudizhuRoom 和 DoudizhuGameState）
 *
 * 测试目标：
 * 1. 牌型识别正确性
 * 2. 大小比较正确性
 * 3. 首出限制
 * 4. 手牌完整性校验
 *
 * @author poker-platform
 */
@DisplayName("斗地主出牌校验测试")
class DoudizhuRuleCheckerTest {

    private DoudizhuRoom room;
    private DoudizhuGameStateManager gameState;
    private DoudizhuRuleChecker ruleChecker;
    private DoudizhuPlayer player;

    @BeforeEach
    void setUp() {
        // 创建真实房间对象
        room = new DoudizhuRoom();
        room.setRoomId(10001L);
        room.setOwnerId(1001L);
        room.setMaxPlayers(3);
        // 必须初始化 gameState
        room.initGameState();
        gameState = room.getStateManager();

        // 更新房间状态（用于展示）
        room.updateGameStatus(DoudizhuGameStatus.PLAYING);
        // 同步更新 gameState 内部状态
        gameState.changeStatus(DoudizhuGameStatus.PLAYING);

        // 创建真实玩家
        player = new DoudizhuPlayer(1001L, "测试玩家");
        room.addPlayer(player);  // 会自动同步到 gameState

        // 设置玩家手牌（完整的17张手牌，用于手牌完整性测试）
        player.setHandCards(createFullHandCards());

        // 设置出牌顺序（通过 gameState）
        List<Long> playOrder = List.of(1001L, 1002L, 1003L);
        gameState.setPlayOrder(playOrder, 1001L);

        // 创建规则检查器（假设它内部通过 room.getGameState() 获取数据）
        ruleChecker = new DoudizhuRuleChecker(room);
    }

    // ==================== 基础牌型测试 ====================

    @Test
    @DisplayName("测试单张出牌")
    void testSingleCard() {
        // 清空上家出牌（首出）
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0)); // ♠3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试对子出牌")
    void testPairCards() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13)); // ♠3, ♥3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试三张出牌")
    void testTripleCards() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13), Card.of(26)); // ♠3, ♥3, ♣3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试三带一出牌")
    void testThreeWithSingleCards() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), // 三张3
                Card.of(1)                            // 单张4
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE_WITH_SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试顺子出牌 - 3,4,5,6,7")
    void testStraightCards() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3), Card.of(4)
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(5); // 5张顺子
    }

    @Test
    @DisplayName("测试连对出牌 - 33,44,55")
    void testStraightPairCards() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), // 33
                Card.of(1), Card.of(14), // 44
                Card.of(2), Card.of(15)  // 55
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(3); // 3连对
    }

    @Test
    @DisplayName("测试炸弹出牌")
    void testBombCards() {
        // 先让上家出一张牌（非首出）
        List<Card> lastCards = Arrays.asList(Card.of(2)); // ♠5
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.SINGLE, 5, lastCards, 0));

        // 当前玩家出炸弹
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39) // 四张3
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.isBomb()).isTrue();
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试王炸出牌")
    void testRocketCards() {
        // 先让上家出一张牌（非首出）
        List<Card> lastCards = Arrays.asList(Card.of(2)); // ♠5
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.SINGLE, 5, lastCards, 0));

        // 当前玩家出王炸
        List<Card> cards = Arrays.asList(Card.of(52), Card.of(53)); // 小王, 大王

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.ROCKET);
        assertThat(result.isRocket()).isTrue();
    }

    // ==================== 大小比较测试 ====================

    @Test
    @DisplayName("测试单张比较 - 5 > 3 能压")
    void testSingleCanBeat() {
        // 设置上家出牌：♠3
        List<Card> lastCards = Arrays.asList(Card.of(0));
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.SINGLE, 3, lastCards, 0));

        // 当前出牌：♠5
        List<Card> currentCards = Arrays.asList(Card.of(2));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
        assertThat(result.getMainRank()).isEqualTo(5);
    }

    @Test
    @DisplayName("测试单张比较 - 3 < 5 不能压")
    void testSingleCannotBeat() {
        // 设置上家出牌：♠5
        List<Card> lastCards = Arrays.asList(Card.of(2));
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.SINGLE, 5, lastCards, 0));

        // 当前出牌：♠3
        List<Card> currentCards = Arrays.asList(Card.of(0));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(30002);
        assertThat(result.getErrorMessage()).isEqualTo("不能压过上家的牌");
    }

    @Test
    @DisplayName("测试对子比较 - 55 > 33 能压")
    void testPairCanBeat() {
        // 上家出牌：33
        List<Card> lastCards = Arrays.asList(Card.of(0), Card.of(13));
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.PAIR, 3, lastCards, 0));

        // 当前出牌：55
        List<Card> currentCards = Arrays.asList(Card.of(2), Card.of(15));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.PAIR);
        assertThat(result.getMainRank()).isEqualTo(5);
    }

    @Test
    @DisplayName("测试炸弹可以压任何非炸弹")
    void testBombBeatAnything() {
        // 上家出牌：单张5
        List<Card> lastCards = Arrays.asList(Card.of(2));
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.SINGLE, 5, lastCards, 0));

        // 当前出牌：炸弹3
        List<Card> currentCards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.isBomb()).isTrue();
    }

    @Test
    @DisplayName("测试炸弹比较大小 - 炸弹4 > 炸弹3")
    void testBombCompare() {
        // 上家出牌：炸弹3
        List<Card> lastCards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)
        );
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.BOMB, 3, lastCards, 0));

        // 当前出牌：炸弹4
        List<Card> currentCards = Arrays.asList(
                Card.of(1), Card.of(14), Card.of(27), Card.of(40)
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.getMainRank()).isEqualTo(4);
    }

    @Test
    @DisplayName("测试王炸可以压任何牌")
    void testRocketBeatAnything() {
        // 上家出牌：炸弹4
        List<Card> lastCards = Arrays.asList(
                Card.of(1), Card.of(14), Card.of(27), Card.of(40)
        );
        gameState.setLastPlayCards(lastCards);
        gameState.updateLastPlay(1002L, lastCards, new PatternResult(CardPattern.BOMB, 4, lastCards, 0));

        // 当前出牌：王炸
        List<Card> currentCards = Arrays.asList(Card.of(52), Card.of(53));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.ROCKET);
        assertThat(result.isRocket()).isTrue();
    }

    // ==================== 首出限制测试 ====================

    @Test
    @DisplayName("测试首出不能出炸弹")
    void testFirstPlayCannotPlayBomb() {
        // 清空上家出牌（首出）
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39) // 炸弹
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(30006);
        assertThat(result.getErrorMessage()).isEqualTo("首出不能出炸弹");
    }

    @Test
    @DisplayName("测试首出可以出单张")
    void testFirstPlayCanPlaySingle() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0)); // ♠3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
    }

    // ==================== 手牌完整性测试 ====================

    @Test
    @DisplayName("测试手牌中没有要出的牌")
    void testCardsNotInHand() {
        gameState.setLastPlayCards(null);

        // 获取玩家手牌中已有的牌 ID
        List<Card> handCards = player.getHandCards();
        Set<Integer> handCardIds = handCards.stream()
                .map(Card::getId)
                .collect(Collectors.toSet());

        // 找一个不在手牌中的有效牌 ID（0-53 范围内）
        int testCardId = -1;
        for (int i = 0; i < 54; i++) {
            if (!handCardIds.contains(i)) {
                testCardId = i;
                break;
            }
        }

        // 确保找到了测试用的牌 ID
        assertThat(testCardId).isNotEqualTo(-1);

        List<Card> cards = Arrays.asList(Card.of(testCardId));

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(30003);
        assertThat(result.getErrorMessage()).isEqualTo("手牌中没有这些牌");
    }

    @Test
    @DisplayName("测试出牌数量超过手牌数量")
    void testPlayMoreCardsThanHand() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        // 玩家只有17张手牌，尝试出18张（不可能）
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            cards.add(Card.of(i));
        }

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        // 应该返回牌型无效或手牌不足
        assertThat(result.getErrorCode()).isIn(30001, 30003);
    }

    // ==================== 非法操作测试 ====================

    @Test
    @DisplayName("测试不是当前回合玩家不能出牌")
    void testNotCurrentPlayerCannotPlay() {
        gameState.setLastPlayCards(null);
        gameState.setLastPattern(null);

        // 当前玩家是 1001，用 1002 出牌
        List<Card> cards = Arrays.asList(Card.of(0));

        ValidationResult result = ruleChecker.validatePlay(1002L, cards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(20002);
        assertThat(result.getErrorMessage()).isEqualTo("不是你的回合");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建完整的手牌，确保测试中使用的所有牌都存在
     */
    private List<Card> createFullHandCards() {
        List<Card> handCards = new ArrayList<>();

        // 添加各种 3 的牌
        handCards.add(Card.of(0));   // ♠3
        handCards.add(Card.of(13));  // ♥3
        handCards.add(Card.of(26));  // ♣3
        handCards.add(Card.of(39));  // ♦3

        // 添加各种 4 的牌
        handCards.add(Card.of(1));   // ♠4
        handCards.add(Card.of(14));  // ♥4
        handCards.add(Card.of(27));  // ♣4
        handCards.add(Card.of(40));  // ♦4

        // 添加各种 5 的牌
        handCards.add(Card.of(2));   // ♠5
        handCards.add(Card.of(15));  // ♥5
        handCards.add(Card.of(28));  // ♣5
        handCards.add(Card.of(41));  // ♦5

        // 添加各种 6 的牌
        handCards.add(Card.of(3));   // ♠6
        handCards.add(Card.of(16));  // ♥6
        handCards.add(Card.of(29));  // ♣6
        handCards.add(Card.of(42));  // ♦6

        // 添加一张 7（用于顺子）
        handCards.add(Card.of(4));   // ♠7

        // 添加大小王
        handCards.add(Card.of(52));  // 小王
        handCards.add(Card.of(53));  // 大王

        // 确保手牌数量足够
        System.out.println("手牌数量: " + handCards.size());

        return handCards;
    }
}
