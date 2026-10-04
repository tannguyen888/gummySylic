package gummySylic.work.mapper;

import gummySylic.work.modal.PromptTemplate;
import gummySylic.work.payload.dto.PromptTemplateDto;

public class PromptTemplateMapper {

    private PromptTemplateMapper() {
    }

    public static PromptTemplateDto toDto(PromptTemplate template) {
        if (template == null) {
            return null;
        }
        return new PromptTemplateDto(
                template.getId(),
                template.getName(),
                template.getCategory(),
                template.getPromptType(),
                template.getTemplateContent(),
                template.getIsActive(),
                template.getCreatedAt());
    }

    public static PromptTemplate toEntity(PromptTemplateDto dto) {
        if (dto == null) {
            return null;
        }
        return PromptTemplate.builder()
                .id(dto.id())
                .name(dto.name())
                .category(dto.category())
                .promptType(dto.promptType())
                .templateContent(dto.templateContent())
                .isActive(dto.isActive() != null ? dto.isActive() : Boolean.TRUE)
                .build();
    }
}
