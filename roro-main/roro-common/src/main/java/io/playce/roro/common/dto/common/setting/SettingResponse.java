package io.playce.roro.common.dto.common.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SettingResponse {


    private String categoryName;
    private List<SettingSubCategory> subCategories;

    @Getter
    @Setter
    @ToString
    public static class SettingSubCategory {

        private String subCategoryName;
        private Tooltip tooltip;
        private List<Setting> settings;

    }

    @Getter
    @Setter
    @ToString
    public static class Tooltip {
        private String descriptionEng;
        private String descriptionKor;
        private List<Field> fields;

    }

    @Getter
    @Setter
    @ToString
    public static class Field {

        private String name;
        private String propertyAliasEng;
        private String propertyAliasKor;
        private String descriptionEng;
        private String descriptionKor;
        private String optionalEng;
        private String optionalKor;

    }

    @Getter
    @Setter
    @ToString
    public static class Setting {

        private Long settingId;
        private String propertyName;
        private String propertyAliasEng;
        private String propertyAliasKor;
        private String propertyValue;
        private String readOnlyYn;
        private String dataType;
        private String dataValues;
        private String placeholderEng;
        private String placeholderKor;
        private Long displayOrder;

    }

}
