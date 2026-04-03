package com.pokergame.common.test.pattern;

import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternComparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 牌型比较器测试
 *
 * @author poker-platform
 */
@DisplayName("牌型比较器测试")
class PatternComparatorTest {

    @Test
    @DisplayName("火箭最大")
    void testRocketIsMax() {
        assertThat(PatternComparator.canBeat(
                CardPattern.BOMB, 3,
                CardPattern.ROCKET, 0
        )).isTrue();

        assertThat(PatternComparator.canBeat(
                CardPattern.ROCKET, 0,
                CardPattern.BOMB, 4
        )).isFalse();
    }

    @Test
    @DisplayName("炸弹可以压非炸弹")
    void testBombCanBeatNonBomb() {
        assertThat(PatternComparator.canBeat(
                CardPattern.STRAIGHT, 3,
                CardPattern.BOMB, 4
        )).isTrue();
    }

    @Test
    @DisplayName("炸弹比较主牌值")
    void testBombCompareMainRank() {
        assertThat(PatternComparator.canBeat(
                CardPattern.BOMB, 3,
                CardPattern.BOMB, 4
        )).isTrue();

        assertThat(PatternComparator.canBeat(
                CardPattern.BOMB, 5,
                CardPattern.BOMB, 4
        )).isFalse();
    }

    @Test
    @DisplayName("同牌型比较主牌值")
    void testSamePatternCompareMainRank() {
        assertThat(PatternComparator.canBeat(
                CardPattern.SINGLE, 5,
                CardPattern.SINGLE, 10
        )).isTrue();

        assertThat(PatternComparator.canBeat(
                CardPattern.PAIR, 10,
                CardPattern.PAIR, 5
        )).isFalse();
    }

    @Test
    @DisplayName("过牌不能压任何牌")
    void testPassCannotBeat() {
        assertThat(PatternComparator.canBeat(
                CardPattern.SINGLE, 3,
                CardPattern.PASS, 0
        )).isFalse();
    }

    @Test
    @DisplayName("不同牌型不能互压（非炸弹）")
    void testDifferentPatternCannotBeat() {
        assertThat(PatternComparator.canBeat(
                CardPattern.SINGLE, 10,
                CardPattern.PAIR, 5
        )).isFalse();
    }
}