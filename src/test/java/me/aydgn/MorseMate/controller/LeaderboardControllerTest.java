package me.aydgn.MorseMate.controller;

import me.aydgn.MorseMate.dto.response.LeaderboardEntryResponse;
import me.aydgn.MorseMate.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    private List<LeaderboardEntryResponse> mockLeaderboard;

    @BeforeEach
    public void setup() {
        mockLeaderboard = Arrays.asList(
                LeaderboardEntryResponse.builder()
                        .userId(1L).username("user1")
                        .totalPoints(1000).level(10).currentStreak(5)
                        .achievementsCount(10L).rank(1L)
                        .build(),
                LeaderboardEntryResponse.builder()
                        .userId(2L).username("user2")
                        .totalPoints(800).level(8).currentStreak(3)
                        .achievementsCount(7L).rank(2L)
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetTopUsersByPoints() throws Exception {
        when(leaderboardService.getTopUsersByPoints(anyInt())).thenReturn(mockLeaderboard);

        mockMvc.perform(get("/v1/leaderboard/points")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].totalPoints").value(1000))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[1].userId").value(2));
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetTopUsersByLevel() throws Exception {
        when(leaderboardService.getTopUsersByLevel(anyInt())).thenReturn(mockLeaderboard);

        mockMvc.perform(get("/v1/leaderboard/level")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].level").value(10))
                .andExpect(jsonPath("$[1].level").value(8));
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetTopUsersByStreak() throws Exception {
        when(leaderboardService.getTopUsersByStreak(anyInt())).thenReturn(mockLeaderboard);

        mockMvc.perform(get("/v1/leaderboard/streak")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentStreak").value(5))
                .andExpect(jsonPath("$[1].currentStreak").value(3));
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetTopUsersByAchievements() throws Exception {
        when(leaderboardService.getTopUsersByAchievements(anyInt())).thenReturn(mockLeaderboard);

        mockMvc.perform(get("/v1/leaderboard/achievements")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievementsCount").value(10))
                .andExpect(jsonPath("$[1].achievementsCount").value(7));
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetMyLeaderboardPosition() throws Exception {
        LeaderboardEntryResponse myPosition = LeaderboardEntryResponse.builder()
                .userId(1L).username("user1")
                .totalPoints(1000).level(10).currentStreak(5)
                .achievementsCount(10L).rank(1L)
                .build();

        when(leaderboardService.getUserLeaderboardPosition(1L)).thenReturn(myPosition);

        mockMvc.perform(get("/v1/leaderboard/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.rank").value(1))
                .andExpect(jsonPath("$.totalPoints").value(1000));
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetMyRankByPoints() throws Exception {
        when(leaderboardService.getUserRankByPoints(1L)).thenReturn(5L);

        mockMvc.perform(get("/v1/leaderboard/me/rank/points")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank").value(5));
    }

    @Test
    public void testLeaderboardEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/v1/leaderboard/points")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
