package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.CreatePowerUpRequest;
import me.aydgn.MorseMate.dto.response.PowerUpResponse;
import me.aydgn.MorseMate.dto.response.UserPowerUpResponse;
import me.aydgn.MorseMate.entity.PowerUp;
import me.aydgn.MorseMate.service.PowerUpService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PowerUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PowerUpService powerUpService;

    private PowerUpResponse mockPowerUp;
    private UserPowerUpResponse mockUserPowerUp;

    @BeforeEach
    public void setup() {
        mockPowerUp = PowerUpResponse.builder()
                .id(1L).name("XP Boost")
                .description("50% more XP for 24 hours")
                .type("XP_BOOST")
                .costGems(100).durationHours(24)
                .icon("boost_icon")
                .isActive(true)
                .build();

        mockUserPowerUp = UserPowerUpResponse.builder()
                .id(1L).userId(1L)
                .powerUp(mockPowerUp)
                .isActive(false).isUsed(false)
                .build();
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetMyPowerUps() throws Exception {
        List<UserPowerUpResponse> powerUps = Arrays.asList(mockUserPowerUp);
        when(powerUpService.getUserPowerUps(1L)).thenReturn(powerUps);

        mockMvc.perform(get("/v1/powerups/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].powerUp.name").value("XP Boost"));
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetMyActivePowerUps() throws Exception {
        UserPowerUpResponse activePowerUp = UserPowerUpResponse.builder()
                .id(1L).userId(1L)
                .powerUp(mockPowerUp)
                .isActive(true).isUsed(false)
                .build();

        when(powerUpService.getUserActivePowerUps(1L)).thenReturn(Arrays.asList(activePowerUp));

        mockMvc.perform(get("/v1/powerups/me/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @WithMockUser(username = "1")
    public void testPurchasePowerUp() throws Exception {
        when(powerUpService.purchasePowerUp(1L, 1L)).thenReturn(mockUserPowerUp);

        mockMvc.perform(post("/v1/powerups/me/purchase/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.powerUp.costGems").value(100));
    }

    @Test
    @WithMockUser(username = "1")
    public void testActivatePowerUp() throws Exception {
        UserPowerUpResponse activatedPowerUp = UserPowerUpResponse.builder()
                .id(1L).userId(1L)
                .powerUp(mockPowerUp)
                .isActive(true).isUsed(false)
                .build();

        when(powerUpService.activatePowerUp(1L, 1L)).thenReturn(activatedPowerUp);

        mockMvc.perform(post("/v1/powerups/me/activate/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(username = "1")
    public void testCheckActivePowerUp() throws Exception {
        when(powerUpService.hasActivePowerUp(1L, PowerUp.Type.XP_BOOST)).thenReturn(true);

        mockMvc.perform(get("/v1/powerups/me/check/xp_boost")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasActive").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetAllPowerUps() throws Exception {
        when(powerUpService.getAllPowerUps()).thenReturn(Arrays.asList(mockPowerUp));

        mockMvc.perform(get("/v1/powerups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("XP Boost"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreatePowerUp() throws Exception {
        CreatePowerUpRequest request = CreatePowerUpRequest.builder()
                .name("Heart Refill")
                .description("Instantly refill all hearts")
                .type("HEART_REFILL")
                .costGems(50)
                .icon("heart_icon")
                .isActive(true)
                .build();

        PowerUpResponse newPowerUp = PowerUpResponse.builder()
                .id(2L).name("Heart Refill")
                .description("Instantly refill all hearts")
                .type("HEART_REFILL")
                .costGems(50)
                .icon("heart_icon")
                .isActive(true)
                .build();

        when(powerUpService.createPowerUp(any(CreatePowerUpRequest.class))).thenReturn(newPowerUp);

        mockMvc.perform(post("/v1/powerups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Heart Refill"))
                .andExpect(jsonPath("$.type").value("HEART_REFILL"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetPowerUpsByType() throws Exception {
        when(powerUpService.getPowerUpsByType(PowerUp.Type.XP_BOOST))
                .thenReturn(Arrays.asList(mockPowerUp));

        mockMvc.perform(get("/v1/powerups/type/xp_boost")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("XP_BOOST"));
    }

    @Test
    public void testPowerUpEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/v1/powerups/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
