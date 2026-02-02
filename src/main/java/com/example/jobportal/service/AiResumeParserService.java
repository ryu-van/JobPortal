package com.example.jobportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class AiResumeParserService {

    private final ChatClient chatClient;

    public AiResumeParserService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String parseResume(String text) {

        String prompt = """
                    You are a resume parser. \s
                    Extract all structured data from the resume text below and return strictly valid JSON that matches this Java entity model:
                
                    {
                      "title": "string",
                      "summary": "string",
                      "educations": [
                        {
                          "institution": "string",
                          "degree": "string",
                          "fieldOfStudy": "string",
                          "startDate": "yyyy-MM-dd",
                          "endDate": "yyyy-MM-dd",
                          "gpa": "decimal (0.00 - 4.00)",
                          "description": "string",
                          "displayOrder": 1
                        }
                      ],
                      "experiences": [
                        {
                          "companyName": "string",
                          "position": "string",
                          "description": "string",
                          "startDate": "yyyy-MM-dd",
                          "endDate": "yyyy-MM-dd",
                          "isCurrent": false,
                          "displayOrder": 1
                        }
                      ],
                      "skills": [
                        {
                          "skillName": "string",
                          "proficiencyLevel": "Beginner / Intermediate / Advanced / Expert",
                          "yearsOfExperience": 0
                        }
                      ]
                    }
                
                    **Important:**
                    - Return strictly valid JSON, not markdown or explanation.
                    - Dates must be in ISO format (yyyy-MM-dd).
                    - Omit null fields.
                    - Guess missing fields reasonably from context.
                    - Output must be pure JSON, nothing else.
                
                    Resume text:
                    %s
                
                """.formatted(text);

        return chatClient.prompt()
                .system("You are a resume parser that outputs only JSON.")
                .user(prompt)
                .options(OpenAiChatOptions.builder()
                        .withModel("gpt-4o-mini")
                        .withTemperature(0.2)
                        .build())
                .call()
                .content();
    }
}
