package com.example.stablediffusion.api;
public class ImageRequest {
    private String key;
    private String model_id;
    private String prompt;
    private String negative_prompt;
    private String width;
    private String height;
    private String samples;
    private String num_inference_steps;
    private String safety_checker;
    private String enhance_prompt;
    private Object seed; // Can be null
    private double guidance_scale;
    private String multi_lingual;
    private String panorama;
    private String self_attention;
    private String upscale;
    private Object embeddings_model; // Can be null
    private Object lora_model; // Can be null
    private String tomesd;
    private String use_karras_sigmas;
    private Object vae; // Can be null
    private Object lora_strength; // Can be null
    private String scheduler;
    private Object webhook; // Can be null
    private Object track_id; // Can be null

    // Constructor, getters, and setters
    public ImageRequest(String key, String model_id, String prompt, String negative_prompt,
                        String width, String height, String samples, String num_inference_steps,
                        String safety_checker, String enhance_prompt, Object seed,
                        double guidance_scale, String multi_lingual, String panorama,
                        String self_attention, String upscale, Object embeddings_model,
                        Object lora_model, String tomesd, String use_karras_sigmas,
                        Object vae, Object lora_strength, String scheduler,
                        Object webhook, Object track_id) {
        this.key = key;
        this.model_id = model_id;
        this.prompt = prompt;
        this.negative_prompt = negative_prompt;
        this.width = width;
        this.height = height;
        this.samples = samples;
        this.num_inference_steps = num_inference_steps;
        this.safety_checker = safety_checker;
        this.enhance_prompt = enhance_prompt;
        this.seed = seed;
        this.guidance_scale = guidance_scale;
        this.multi_lingual = multi_lingual;
        this.panorama = panorama;
        this.self_attention = self_attention;
        this.upscale = upscale;
        this.embeddings_model = embeddings_model;
        this.lora_model = lora_model;
        this.tomesd = tomesd;
        this.use_karras_sigmas = use_karras_sigmas;
        this.vae = vae;
        this.lora_strength = lora_strength;
        this.scheduler = scheduler;
        this.webhook = webhook;
        this.track_id = track_id;
    }
}

