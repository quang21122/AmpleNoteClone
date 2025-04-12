package com.example.amplenoteclone.summary;

import android.content.Context;
import android.widget.Toast;

import com.example.amplenoteclone.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.function.Consumer;

public class GeminiSummary {
    private final GenerativeModelFutures model;
    private final Context context;
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    public GeminiSummary(Context context) {
        this.context = context;
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.0-flash",
                API_KEY
        );
        this.model = GenerativeModelFutures.from(gm);
    }

    public void generateSummary(String noteContent, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        if (noteContent == null || noteContent.trim().isEmpty()) {
            onError.accept(new IllegalArgumentException("Note content is empty"));
            return;
        }

        Content promptContent = createSummaryPrompt(noteContent);
        callGeminiApi(promptContent, onSuccess, onError);
    }

    private Content createSummaryPrompt(String noteContent) {
        // Tạo prompt yêu cầu tóm tắt
        String prompt = "Summarize the following text in a concise summary of fewer than 30 words. Use the exact same language (Vietnamese or English) as the original text:\n\n" + noteContent;
        return new Content.Builder()
                .addText(prompt)
                .build();
    }

    private void callGeminiApi(Content content, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        // Gọi API Gemini bất đồng bộ
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String summary = result.getText();
                if (summary != null && !summary.isEmpty()) {
                    onSuccess.accept(summary);
                } else {
                    onError.accept(new IllegalStateException("Empty summary received"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                onError.accept(t);
            }
        }, context.getMainExecutor());
    }
}