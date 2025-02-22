package com.google_mlkit_language_id;

import androidx.annotation.NonNull;

import com.google.mlkit.nl.languageid.IdentifiedLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class LanguageDetector implements MethodChannel.MethodCallHandler {
    private static final String START = "nlp#startLanguageIdentifier";
    private static final String CLOSE = "nlp#closeLanguageIdentifier";

    private LanguageIdentifier languageIdentifier;

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        String method = call.method;
        switch (method) {
            case START:
                identifyLanguages(call, result);
                break;
            case CLOSE:
                closeDetector();
                result.success(null);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void identifyLanguages(MethodCall call, final MethodChannel.Result result) {
        double confidence = (double) call.argument("confidence");
        languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold((float) confidence)
                        .build());

        boolean possibleLanguages = (boolean) call.argument("possibleLanguages");
        String text = (String) call.argument("text");
        if (!possibleLanguages) {
            identifyLanguage(text, result);
        } else {
            identifyPossibleLanguages(text, result);
        }
    }

    private void identifyLanguage(String text, final MethodChannel.Result result) {
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(result::success)
                .addOnFailureListener(e -> result.error("Language Identification Error", e.toString(), null));
    }

    private void identifyPossibleLanguages(String text, final MethodChannel.Result result) {
        languageIdentifier.identifyPossibleLanguages(text)
                .addOnSuccessListener(identifiedLanguages -> {
                    List<Map<String, Object>> languageList = new ArrayList<>();
                    for (IdentifiedLanguage language : identifiedLanguages) {
                        Map<String, Object> languageData = new HashMap<>();
                        languageData.put("confidence", language.getConfidence());
                        languageData.put("language", language.getLanguageTag());
                        languageList.add(languageData);
                    }
                    result.success(languageList);
                })
                .addOnFailureListener(e -> result.error("Error identifying possible languages", e.toString(), null));
    }

    private void closeDetector() {
        if (languageIdentifier == null) return;
        languageIdentifier.close();
    }
}
