package com.example.api_v2.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserMonthlyStatsDto {
    private String studyTime;
    private int cardsStudied;
    private String accuracy;
    private int daysActive;
    private List<DayActivity> heatmap;

    @Data
    public static class DayActivity {
        private String day;
        private int intensity;
        private int minutesStudied;
        private int cardsStudied;
        private double accuracy;
        private List<String> achievements;
        private int dayOfMonth;
        private int weekOfMonth;
    }
}
