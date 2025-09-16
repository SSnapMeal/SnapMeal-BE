package snapmeal.snapmeal.converter;

import org.json.JSONArray;

import org.json.JSONObject;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;

public class NutritionConverter {
    public static NutritionRequestDto.TotalNutritionRequestDto fromOpenAiJson(String jsonResponse){
        JSONArray array = new JSONArray(jsonResponse);

        int totalCalories = 0;
        double totalProtein = 0,  totalCarbs = 0, totalSugar = 0, totalFat = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject food = array.getJSONObject(i);
            totalCalories += food.optInt("calories", 0);
            totalProtein += food.optDouble("protein", 0);
            totalCarbs += food.optDouble("carbs", 0);
            totalSugar += food.optDouble("sugar", 0);
            totalFat += food.optDouble("fat", 0);
        }

        return new NutritionRequestDto.TotalNutritionRequestDto(totalCalories, totalProtein, totalCarbs, totalSugar, totalFat, null);
    }

}
