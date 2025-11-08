package com.example.jobportal.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiResumeParserService {
    private final OpenAiService openAiService;

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
                    {{resume_text_here}}
                
                """ + text;

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(
                        new ChatMessage("system", "You are a resume parser that outputs only JSON."),
                        new ChatMessage("user", prompt)
                ))
                .temperature(0.2)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);

        return result.getChoices().getFirst().getMessage().getContent();
    }
}
