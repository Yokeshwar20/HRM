package com.example.hrmtask.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrevoEmailRequestDto {
    private Sender sender;
    private List<Recipient> to;
    private String subject;
    private String htmlContent;
    private String textContent;
    private List<Attachment> attachment;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Sender {
        private String name;
        private String email;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recipient {
        private String email;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attachment {
        private String content;
        private String name;
    }
}
