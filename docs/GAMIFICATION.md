# MorseMate Gamification System

## Overview

The MorseMate gamification system provides a comprehensive engagement layer including:
- Achievement system with customizable criteria
- Virtual currency (Gems) economy
- Power-ups for enhanced learning experience
- Competitive leaderboards

## Features

### 1. Achievement System

#### API Endpoints

**User Endpoints:**
- `GET /v1/achievements/me` - Get user's achievements
- `GET /v1/achievements/me/paged` - Get user's achievements (paginated)
- `GET /v1/achievements/available` - Get available achievements
- `GET /v1/achievements/me/progress/{achievementId}` - Check achievement progress

**Admin Endpoints:**
- `POST /v1/achievements` - Create achievement
- `PUT /v1/achievements/{id}` - Update achievement
- `DELETE /v1/achievements/{id}` - Delete achievement
- `GET /v1/achievements` - Get all achievements
- `GET /v1/achievements/{id}` - Get achievement by ID

#### Achievement Types

Achievements can be based on various criteria:
- **Streak-based**: Daily login streaks
- **Points-based**: Total points earned
- **Level-based**: User level milestones
- **Lessons-based**: Completed lessons count
- **Exercises-based**: Completed exercises count

#### Example Achievement Criteria

```json
{
  "streak": 7,
  "points": 1000,
  "level": 10,
  "lessons_completed": 20,
  "exercises_completed": 100
}
```

### 2. Gem System

#### API Endpoints

**User Endpoints:**
- `GET /v1/gems/me` - Get gem balance
- `POST /v1/gems/me/spend` - Spend gems
- `GET /v1/gems/me/transactions` - Transaction history
- `GET /v1/gems/me/statistics` - Gem statistics
- `GET /v1/gems/me/check/{amount}` - Check if sufficient gems

**Admin Endpoints:**
- `POST /v1/gems/users/{userId}/add` - Add gems to user
- `POST /v1/gems/users/{userId}/bonus` - Award bonus gems
- `GET /v1/gems/users/{userId}` - Get user's gem balance

#### Earning Gems

Users earn gems through:
1. **Exercise Completion**: 10% of exercise points as gems (minimum 1 gem)
2. **Lesson Completion**:
   - 3 stars: 30 gems
   - 2 stars: 20 gems
   - 1 star: 10 gems
3. **Achievements**: Configured per achievement
4. **Admin Bonuses**: Manual gem awards

#### Spending Gems

Gems can be spent on:
- Power-ups (costs vary by type)
- Future features (unlockable content, cosmetics, etc.)

### 3. Power-Up System

#### API Endpoints

**User Endpoints:**
- `GET /v1/powerups/me` - Get owned power-ups
- `GET /v1/powerups/me/active` - Get active power-ups
- `POST /v1/powerups/me/purchase/{powerUpId}` - Purchase power-up
- `POST /v1/powerups/me/activate/{userPowerUpId}` - Activate power-up
- `GET /v1/powerups/me/check/{type}` - Check if type is active

**Admin Endpoints:**
- `POST /v1/powerups` - Create power-up
- `PUT /v1/powerups/{id}` - Update power-up
- `DELETE /v1/powerups/{id}` - Delete power-up
- `POST /v1/powerups/cleanup` - Cleanup expired power-ups

#### Power-Up Types

1. **XP_BOOST**
   - Effect: +50% XP for all exercise completions
   - Duration: Configurable (e.g., 24 hours)
   - Applied automatically during exercise attempts

2. **HEART_REFILL**
   - Effect: Instantly refills all hearts to maximum
   - Duration: Instant (no duration)
   - Applied immediately upon activation

3. **STREAK_FREEZE**
   - Effect: Protects streak from breaking for one missed day
   - Duration: Configurable (e.g., 24 hours)
   - Applied during streak calculation

4. **UNLIMITED_HEARTS**
   - Effect: No heart deduction for wrong answers
   - Duration: Configurable (e.g., 1 hour)
   - Applied during exercise attempts

#### Power-Up Lifecycle

1. **Purchase**: User spends gems to buy power-up
2. **Storage**: Power-up stored in inventory (inactive)
3. **Activation**: User activates power-up when needed
4. **Active Period**: Power-up effects apply during duration
5. **Expiration**: Power-up marked as used after expiry

### 4. Leaderboard System

#### API Endpoints

All leaderboard endpoints require authentication:
- `GET /v1/leaderboard/points` - Top users by total points
- `GET /v1/leaderboard/level` - Top users by level
- `GET /v1/leaderboard/streak` - Top users by current streak
- `GET /v1/leaderboard/longest-streak` - Top users by longest streak
- `GET /v1/leaderboard/achievements` - Top users by achievement count
- `GET /v1/leaderboard/me` - Current user's position
- `GET /v1/leaderboard/me/rank/{type}` - Current user's rank by type

#### Leaderboard Types

1. **Points Leaderboard**: Based on total points earned
2. **Level Leaderboard**: Based on user level (with points as tiebreaker)
3. **Streak Leaderboard**: Based on current daily streak
4. **Longest Streak Leaderboard**: Based on longest streak achieved
5. **Achievements Leaderboard**: Based on total achievements earned

#### Query Parameters

- `limit`: Number of entries to return (1-100, default: 10)

## Integration with Learning System

### Exercise Completion

When a user completes an exercise correctly:
1. **Points awarded**: Base points + time bonus
2. **XP Boost applied**: If user has active XP_BOOST power-up (+50%)
3. **Gems awarded**: 10% of total points as gems
4. **Level updated**: Points trigger level progression
5. **Achievements checked**: Auto-award matching achievements

### Lesson Completion

When a user completes a lesson:
1. **Gems awarded**: Based on stars earned (10-30 gems)
2. **Achievements checked**: Auto-award matching achievements

### Power-Up Effects

Power-ups are automatically applied during relevant actions:
- **XP_BOOST**: During exercise point calculation
- **HEART_REFILL**: Immediately upon activation
- **STREAK_FREEZE**: During daily streak calculation
- **UNLIMITED_HEARTS**: During exercise attempt heart deduction

## Database Schema

### Key Tables

1. **achievements**: Achievement definitions
2. **user_achievements**: User-achievement mappings
3. **user_gems**: User gem balances
4. **gem_transactions**: Gem transaction history
5. **power_ups**: Power-up definitions
6. **user_power_ups**: User power-up inventory

## Error Handling

Common error scenarios:
- **Insufficient Gems**: User tries to purchase with insufficient balance
- **Power-up Not Active**: User tries to activate used/expired power-up
- **Invalid Achievement**: Achievement criteria JSON is malformed
- **Duplicate Purchase**: User already owns the power-up/achievement

## Best Practices

1. **Check Power-ups Before Actions**: Always verify active power-ups before calculating rewards
2. **Transaction Safety**: Use atomic operations for gem transactions
3. **Achievement Auto-Check**: Trigger achievement checks after major user actions
4. **Cleanup Expired Power-ups**: Run cleanup task periodically (scheduled job recommended)

## Future Enhancements

Potential additions:
- Social features (friends, gifting gems)
- Tournaments and competitions
- Seasonal achievements
- Power-up combinations
- Guild/team system
- Daily quests
- Achievement tiers (bronze, silver, gold)
