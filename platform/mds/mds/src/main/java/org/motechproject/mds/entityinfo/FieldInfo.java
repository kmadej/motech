package org.motechproject.mds.entityinfo;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.motechproject.mds.dto.FieldDto;

import java.util.List;

import static org.motechproject.mds.util.Constants.Util.AUTO_GENERATED;
import static org.motechproject.mds.util.Constants.Util.TRUE;

/**
 * The <code>FieldInfo</code> class contains base information about the given entity field like its
 * name or type.
 *
 * @see org.motechproject.mds.service.JarGeneratorService
 */
public class FieldInfo {

    private FieldDto field;

    private TypeInfo typeInfo = new TypeInfo();
    private boolean restExposed;

    public FieldDto getField() {
        return field;
    }

    public void setField(FieldDto field) {
        this.field = field;
    }


    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public boolean isRestExposed() {
        return restExposed;
    }

    public void setRestExposed(boolean restExposed) {
        this.restExposed = restExposed;
    }

    @JsonIgnore
    public String getName() {
        return field.getBasic().getName();
    }

    @JsonIgnore
    public String getDisplayName() {
        return field.getBasic().getDisplayName();
    }

    @JsonIgnore
    public boolean isRequired() {
        return field.getBasic().isRequired();
    }

    @JsonIgnore
    public boolean isAutoGenerated() {
        return TRUE.equalsIgnoreCase(field.getMetadataValue(AUTO_GENERATED));
    }

    @JsonIgnore
    public String getTaskType() {
        return typeInfo.getTaskType();
    }

    @JsonIgnore
    public String getType() {
        return typeInfo.getType();
    }

    @JsonIgnore
    public boolean isVersionField() {
        return field.isVersionField();
    }


    public class TypeInfo {
        private boolean isCombobox;
        private String type;
        private String taskType;
        private List<String> items;
        private boolean allowsMultipleSelection;
        private boolean allowUserSupplied;

        public boolean isAllowsMultipleSelection() {
            return allowsMultipleSelection;
        }

        public void setAllowsMultipleSelection(boolean allowsMultipleSelection) {
            this.allowsMultipleSelection = allowsMultipleSelection;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }

        public String getTaskType() {
            return taskType;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public boolean isCombobox() {
            return isCombobox;
        }

        public void setCombobox(boolean isCombobox) {
            this.isCombobox = isCombobox;
        }

        public boolean isAllowUserSupplied() {
            return allowUserSupplied;
        }

        public void setAllowUserSupplied(boolean allowUserSupplied) {
            this.allowUserSupplied = allowUserSupplied;
        }
    }
}