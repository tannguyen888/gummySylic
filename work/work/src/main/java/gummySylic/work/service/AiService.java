package gummySylic.work.service;

import java.util.List;

public interface AiService {

    /**
     * Generate a Veo3-optimized video prompt from a user request and an
     * optional reference image (base64). promptType selects the system prompt
     * (e.g. "veo3-fashion" for clothing sales videos).
     */
    String generateVeo3Prompt(String userRequest, String imageBase64, String imageMimeType, String promptType);

    /** Suggest ready-to-use Veo3 prompt ideas for a topic. */
    List<String> suggestPrompts(String topic, int count);
}
