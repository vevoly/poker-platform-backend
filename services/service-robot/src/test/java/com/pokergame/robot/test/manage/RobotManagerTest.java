package com.pokergame.robot.test.manage;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.card.CardRank;
import com.pokergame.common.card.CardSuit;
import com.pokergame.common.enums.GameActionType;
import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.event.*;
import com.pokergame.common.game.GameType;
import com.pokergame.common.model.robot.RobotAccountDTO;
import com.pokergame.robot.manager.RobotManager;
import com.pokergame.robot.manager.RoomState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RobotManagerTest {

    private RobotManager robotManager;

    @BeforeEach
    void setUp() {
        robotManager = new RobotManager();
        // 可选：预先加载一些机器人账号（用于 isRobot 判断）
        // robotManager.addOrUpdateRobot(new RobotAccountDTO().setUserId(1001L).setEnabled(1));
    }

    @Test
    void testOnGameStart_CreatesRoomState() {
        String roomId = "room_001";
        List<Long> playerIds = Arrays.asList(1001L, 1002L, 1003L);
        GameStartEvent event = new GameStartEvent(GameType.DOUDIZHU, roomId, playerIds, 1000);

        robotManager.onGameStart(event);

        RoomState state = robotManager.getRoomState(roomId);
        assertNotNull(state);
        assertEquals(roomId, state.getRoomId());
        assertEquals(GameType.DOUDIZHU, state.getGameType());
        assertEquals(3, state.getPlayerIds().size());
        assertEquals(1000, state.getScores().get(1001L));
        assertTrue(state.getLastEventTime() > 0);
    }

    @Test
    void testOnTurnChanged_UpdatesCurrentPlayer() {
        String roomId = "room_001";
        // 先创建房间
        robotManager.onGameStart(new GameStartEvent(GameType.DOUDIZHU, roomId, Arrays.asList(1001L, 1002L), 1000));

        TurnChangedEvent event = new TurnChangedEvent(GameType.DOUDIZHU, roomId, 1002L, 30);
        robotManager.onTurnChanged(event);

        RoomState state = robotManager.getRoomState(roomId);
        assertEquals(1002L, state.getCurrentPlayerId());
        assertEquals(30, state.getTimeoutSeconds());
    }

    @Test
    void testOnCardPlayed_RecordsPlayedCards() {
        String roomId = "room_001";
        robotManager.onGameStart(new GameStartEvent(GameType.DOUDIZHU, roomId, Arrays.asList(1001L, 1002L), 1000));

        List<Card> played = Arrays.asList(Card.of(CardSuit.SPADE, CardRank.ACE));
        GameActionEvent event = new GameActionEvent(
                GameEventType.DOUDIZHU_PLAY_CARD, GameType.DOUDIZHU, roomId,
                1001L, GameActionType.PLAY, played, CardPattern.SINGLE);
        robotManager.onCardPlayed(event);

        RoomState state = robotManager.getRoomState(roomId);
        assertNotNull(state.getPlayedCards());
        assertTrue(state.getPlayedCards().get(1001L).containsAll(played));
        assertEquals(1001L, state.getLastActionPlayerId());
        assertEquals(played, state.getLastPlayedCards());
    }

    @Test
    void testOnGameOver_ClearsRoomState() {
        String roomId = "room_001";
        robotManager.onGameStart(new GameStartEvent(GameType.DOUDIZHU, roomId, Arrays.asList(1001L, 1002L), 1000));
        assertNotNull(robotManager.getRoomState(roomId));

        GameOverEvent event = new GameOverEvent(GameType.DOUDIZHU, roomId, 1001L, Map.of(1001L, 1200, 1002L, 800));
        robotManager.onGameOver(event);

        assertNull(robotManager.getRoomState(roomId));
    }

    @Test
    void testIsRobot_ReturnsTrueForRobotId() {
        // 假设机器人账号 9999 已添加到池中
        robotManager.addOrUpdateRobot(new RobotAccountDTO().setUserId(9999L).setEnabled(1));
        assertTrue(robotManager.isRobot(9999L));
        assertFalse(robotManager.isRobot(1001L));
    }
}
