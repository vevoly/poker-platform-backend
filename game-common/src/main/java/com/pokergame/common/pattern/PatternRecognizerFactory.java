package com.pokergame.common.pattern;

import com.pokergame.common.game.GameType;
import com.pokergame.common.pattern.bull.BullPatternRecognizer;
import com.pokergame.common.pattern.doudizhu.DoudizhuPatternRecognizer;
import com.pokergame.common.pattern.texas.TexasPatternRecognizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 牌型识别器工厂
 */
public class PatternRecognizerFactory {
    private static final Map<GameType, PatternRecognizer> RECOGNIZERS = new ConcurrentHashMap<>();

    static {
        register(GameType.DOUDIZHU, new DoudizhuPatternRecognizer());
        register(GameType.TEXAS, new TexasPatternRecognizer());
        register(GameType.BULL, new BullPatternRecognizer());
    }

    public static void register(GameType gameType, PatternRecognizer recognizer) {
        RECOGNIZERS.put(gameType, recognizer);
    }

    public static PatternRecognizer get(GameType gameType) {
        PatternRecognizer recognizer = RECOGNIZERS.get(gameType);
        if (recognizer == null) {
            throw new UnsupportedOperationException("不支持的游戏类型: " + gameType);
        }
        return recognizer;
    }
}
