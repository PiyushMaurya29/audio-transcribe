package com.audio.transcribe;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/transcribe")
public class TranscriptionController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public TranscriptionController(
            @Value("${spring.ai.openai.api-key}") String apiKey) {

        OpenAiAudioApi openAiAudioApi =
                OpenAiAudioApi.builder()
                        .apiKey(apiKey)
                        .build();

        this.transcriptionModel =
                new OpenAiAudioTranscriptionModel(openAiAudioApi);
    }

    @PostMapping
    public ResponseEntity<String> transcribeAudio(
            @RequestParam("file") MultipartFile file) throws IOException {

        // Create temp file
        File tempFile = File.createTempFile("audio", ".wav");
        file.transferTo(tempFile);

        // Transcription options
        OpenAiAudioTranscriptionOptions transcriptionOptions =
                OpenAiAudioTranscriptionOptions.builder()
                        .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                        .language("en")
                        .temperature(0f)
                        .build();

        // Convert to resource
        FileSystemResource audioFile =
                new FileSystemResource(tempFile);

        // Prompt
        AudioTranscriptionPrompt transcriptionRequest =
                new AudioTranscriptionPrompt(audioFile, transcriptionOptions);

        // Call OpenAI
        AudioTranscriptionResponse response =
                transcriptionModel.call(transcriptionRequest);

        // Delete temp file
        tempFile.delete();

        return ResponseEntity.ok(response.getResult().getOutput());
    }
}